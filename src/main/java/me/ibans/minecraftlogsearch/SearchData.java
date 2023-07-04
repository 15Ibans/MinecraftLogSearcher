package me.ibans.minecraftlogsearch;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SearchData {

    private AtomicInteger numFound = new AtomicInteger(0);
    private final List<SearchResult> results = Collections.synchronizedList(new ArrayList<>());
    private final StringBuilder dumpData = new StringBuilder();

    private boolean isDumping;

    public SearchData(boolean isDumping) {
        this.isDumping = isDumping;
    }

    private Set<String> getUniqueFilePaths() {
        return results.stream()
                .map(result -> result.getFile().getPath())
                .collect(Collectors.toSet());
    }

    public int getNumFound() {
        return numFound.get();
    }

    public void addToNumFound(int amount) {
        numFound.addAndGet(amount);
    }

    public void addSearchResult(File file, String line, int lineNumber) {
        if (isDumping && !getUniqueFilePaths().contains(file.getPath())) {  // append to stringbuilder only if we're dumping
            dumpData.append(file.getPath()).append("\n\n");
            dumpData.append(line).append("\n");
        } else if (isDumping && getUniqueFilePaths().contains(file.getPath())) {
            dumpData.append(line).append("\n");
            dumpData.append("\n");
        }
        results.add(new SearchResult(file, line, lineNumber));
    }

    public String getSearchResult(int index) {
        return results.get(index).getContent();
    }

    public int getLengthOfResults() {
        return results.size();
    }

    public int getLineNumber(int index) {
        return results.get(index).getLineNumber();
    }

    public String getFileNames(int index) {
        return results.get(index).getFile().getAbsolutePath();
    }

    public String getDumpData() {
        return dumpData.toString();
    }

    public void sortData() {
        results.sort(Comparator.comparing((SearchResult r) -> r.getFile().getName()).thenComparing(SearchResult::getLineNumber));
    }

    public void sortResults(Comparator<? super SearchResult> comparator) {
        results.sort(comparator);
    }

}
