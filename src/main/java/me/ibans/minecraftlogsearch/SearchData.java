package me.ibans.minecraftlogsearch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchData {

    private int numFound = 0;
    private final List<SearchResult> results = new ArrayList<>();
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
        return numFound;
    }

    public void addToNumFound(int amount) {
        numFound += amount;
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
        return results.get(index).getFile().getPath();
    }

    public String getDumpData() {
        return dumpData.toString();
    }

}
