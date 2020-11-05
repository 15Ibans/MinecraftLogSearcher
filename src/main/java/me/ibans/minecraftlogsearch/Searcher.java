package me.ibans.minecraftlogsearch;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class Searcher {

    public ArrayList<String> fileNames = new ArrayList<>();

    public Searcher(String directory, String extension) {
        try (Stream<Path> walk = Files.walk(Paths.get(directory))) {
            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(extension)).collect(Collectors.toList());

            if (result.size() > 0) {
                result.forEach(file -> fileNames.add(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileNames.sort(Comparator.naturalOrder());

        // check the latest log too
        File latest = Paths.get(directory, "latest.log").toFile();
        if (latest.exists()) fileNames.add(latest.toString());
    }

    public boolean canSearch() {
        if (fileNames.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void searchFiles(String searchTerm) {
        SearchData data = getResults(searchTerm);
        for (int i = 0; i < data.getLengthOfResults(); i++) {
            System.out.println("\nFound in " + data.getFileNames(i) + " at line " + data.getLineNumber(i) + ".\n");
            System.out.println(data.getSearchResult(i) + "\n\n--------------------------------------");
        }
        System.out.println(data.getNumFound() + " results found for \"" + searchTerm + "\".\n");
    }

    private SearchData getResults(String searchTerm) {
        SearchData data = new SearchData();
        for (String fileName : fileNames) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                // searches compressed log
                if (fileName.endsWith(".gz")) {
                    GZIPInputStream gis = new GZIPInputStream(fis);
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(gis));
                    int lineNumber = 0;
                    for (String line; (line = buffered.readLine()) != null;) {
                        if (line.contains(searchTerm)) {
                            data.addToNumFound(StringUtils.countMatches(line, searchTerm));
                            data.addSearchResult(line, lineNumber);
                            data.addToFileNames(fileName);
                        }
                        lineNumber++;
                    }
                    gis.close();
                    buffered.close();
                // searches latest.log
                } else {
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(fis));
                    int lineNumber = 0;
                    for (String line; (line = buffered.readLine()) != null;) {
                        if (line.contains(searchTerm)) {
                            data.addToNumFound(StringUtils.countMatches(line, searchTerm));
                            data.addSearchResult(line, lineNumber);
                            data.addToFileNames(fileName);
                        }
                        lineNumber++;
                    }
                    buffered.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    // this is basically only meant for dumping the log search data
    public String getDumpData(String searchTerm) {
        final StringBuilder dumpData = new StringBuilder();

        for (String fileName : fileNames) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                // searches compressed log
                if (fileName.endsWith(".gz")) {
                    GZIPInputStream gis = new GZIPInputStream(fis);
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(gis));
                    boolean foundInFile = false;

                    for (String line; (line = buffered.readLine()) != null;) {
                        if (line.contains(searchTerm)) {
                            if (!foundInFile) {
                                foundInFile = true;
                                dumpData.append(fileName).append("\n\n");
                            }
                            dumpData.append(line).append("\n");
                        }
                    }
                    if (foundInFile) {
                        dumpData.append("\n");
                    }
                    gis.close();
                    buffered.close();

                // searches latest.log
                } else {
                    BufferedReader buffered = new BufferedReader(new InputStreamReader(fis));
                    boolean foundInFile = false;

                    for (String line; (line = buffered.readLine()) != null;) {
                        if (line.contains(searchTerm)) {
                            if (!foundInFile) {
                                foundInFile = true;
                                dumpData.append(fileName).append("\n\n");
                            }
                            dumpData.append(line).append("\n");
                        }
                    }
                    if (foundInFile) {
                        dumpData.append("\n");
                    }

                    buffered.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dumpData.toString();
    }
}