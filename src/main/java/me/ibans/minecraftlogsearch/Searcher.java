package me.ibans.minecraftlogsearch;

import me.ibans.minecraftlogsearch.util.StringUtil;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

// todo: refactor this garbage
public class Searcher {

    public List<File> files = new ArrayList<>();

    private int threads = 0;

    private boolean stripEscapeSequences;

    public Searcher(File directory, String extension, DateRange dateRange) {
        try (Stream<Path> walk = Files.walk(directory.toPath())) {
            List<File> result = walk.map(Path::toFile)
                    .filter(f -> {
                        try {
                            String nameWithoutExtension = FilenameUtils.removeExtension(f.getName());
                            String logDate = StringUtils.substringBeforeLast(nameWithoutExtension, "-");
                            LocalDate parsedDate = LocalDate.parse(logDate);
                            return f.getName().endsWith(extension) && dateRange.isInRange(parsedDate);
                        } catch (DateTimeException ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            if (!result.isEmpty()) {
                files.addAll(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        files.sort(Comparator.naturalOrder());

        // check the latest log too
        File latest = new File(directory, "latest.log");
        LocalDate latestLastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(latest.lastModified()), ZoneId.systemDefault()).toLocalDate();
        if (latest.exists() && dateRange.isInRange(latestLastModified)) {
            files.add(latest);
        }
    }

    public Searcher(File directory, String extension, DateRange range, int threads, boolean stripEscapeSequences) {
        this(directory, extension, range);
        this.threads = threads;
        this.stripEscapeSequences = stripEscapeSequences;
    }

    public boolean canSearch() {
        return !files.isEmpty();
    }

    public void searchFiles(SearchOptions searchOptions) {
        long start = System.currentTimeMillis();
        SearchData data = getResults(searchOptions);
        long duration = System.currentTimeMillis() - start;
        for (int i = 0; i < data.getLengthOfResults(); i++) {
            System.out.println("\n\nFound in " + data.getFileNames(i) + " at line " + data.getLineNumber(i) + ".\n");
            System.out.println(data.getSearchResult(i) + "\n\n--------------------------------------");
        }
        String searchInput = searchOptions.getSearchTerm() != null ? searchOptions.getSearchTerm() : searchOptions.getRegex().toString();
        System.out.println("\n" + data.getNumFound() + " results found for \"" + searchInput + "\".\n");

        if (Main.isDebug) {
            DecimalFormat format = new DecimalFormat("##.###");
            System.out.println("[DEBUG] Search operation took " + format.format(duration / 1000.0) + " seconds.");
        }
    }

    private void searchFile(File file, SearchData data, SearchOptions searchOptions) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        // searches compressed log, if it's latest.log it will be null since it's not compressed
        GZIPInputStream gis = file.getName().endsWith(".gz") ? new GZIPInputStream(fis) : null;
        BufferedReader buffered = new BufferedReader(new InputStreamReader(gis != null ? gis : fis));
        int lineNumber = 0;
        for (String line; (line = buffered.readLine()) != null; ) {
            if (stripEscapeSequences) {
                final String escapeSequence = "\\e\\[([0-9;]*)m";
                line = line.replaceAll(escapeSequence, "");
            }
            if (searchOptions.getSearchTerm() != null) {
                int matches = StringUtil.countMatches(line, searchOptions.getSearchTerm(), searchOptions.isIgnoreCase());
                if (matches > 0) {
                    data.addToNumFound(matches);
                    data.addSearchResult(file, line, lineNumber);
                }
            } else if (searchOptions.getRegex() != null) {
                int numFound = 0;
                Matcher matcher = searchOptions.getRegex().matcher(line);
                if (matcher.find()) {
                    data.addSearchResult(file, line, lineNumber);
                    matcher.reset();
                }
                while (matcher.find()) {
                    numFound++;
                }
                data.addToNumFound(numFound);
            }
            lineNumber++;
        }
        buffered.close();
        if (gis != null) gis.close();
        fis.close();
    }

    public SearchData getResults(SearchOptions searchOptions) {
        final SearchData data = new SearchData(false);

        if (threads <= 1) {
            int counter = 1;
            for (File file : files) {
                System.out.print("\rSearching (" + counter++ + "/" + files.size() + " logs processed)");
                try {
                    searchFile(file, data, searchOptions);
                } catch (IOException e) {
                    System.out.println("Got an IO exception when processing file " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        } else {
            List<List<File>> partitioned = ListUtils.partition(files, threads);
            AtomicInteger counter = new AtomicInteger(1);
            ExecutorService es = Executors.newCachedThreadPool();
            for (List<File> sublist : partitioned) {
                es.execute(() -> {
                    for (File file : sublist) {
                        System.out.print("\rSearching (" + counter.getAndIncrement() + "/" + files.size() + " logs processed)");
                        try {
                            searchFile(file, data, searchOptions);
                        } catch (IOException e) {
                            System.out.println("Got an IO exception when processing file " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
                });
            }
            try {
                es.shutdown();
                es.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            data.sortData();
        }
        return data;
    }

    // this is basically only meant for dumping the log search data
    public String getDumpData(SearchOptions options) {
        final SearchData data = new SearchData(true);

        for (File file : files) {
            try {
                searchFile(file, data, options);
            } catch (IOException e) {
                System.out.println("Got an IO exception when processing file " + file.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return data.getDumpData();
    }

}