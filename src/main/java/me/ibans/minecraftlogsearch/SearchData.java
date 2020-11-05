package me.ibans.minecraftlogsearch;

import java.util.ArrayList;

public class SearchData {

    private int numFound = 0;
    private ArrayList<String> results = new ArrayList<>();
    private ArrayList<Integer> lineNumbers = new ArrayList<>();
    private ArrayList<String> fileNames = new ArrayList<>();

    public int getNumFound() {
        return numFound;
    }

    public void addToNumFound(int amount) {
        numFound += amount;
    }

    public void addSearchResult(String line, int lineNumber) {
        results.add(line);
        lineNumbers.add(lineNumber);
    }

    public String getSearchResult(int index) {
        return results.get(index);
    }

    public int getLengthOfResults() {
        return fileNames.size();
    }

    public int getLineNumber(int index) {
        return lineNumbers.get(index);
    }

    public void addToFileNames(String fileName) {
        fileNames.add(fileName);
    }

    public String getFileNames(int index) {
        return fileNames.get(index);
    }

}
