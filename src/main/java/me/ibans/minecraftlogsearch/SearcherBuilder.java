package me.ibans.minecraftlogsearch;

import java.io.File;
import java.time.LocalDate;

public class SearcherBuilder {

    private File logDirectory;
    private String searchTerm;
    private boolean ignoreCase = true;
    private String regex;
    private final DateRange dateRange = new DateRange();
    private int threads = 1;

    public SearcherBuilder setLogDirectory(File directory) {
        logDirectory = directory;
        return this;
    }

    public SearcherBuilder setLogDirectory(String directory) {
        this.logDirectory = new File(directory);
        return this;
    }

    public SearcherBuilder searchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
        return this;
    }

    public SearcherBuilder setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

    public SearcherBuilder setLowerBound(LocalDate lowerBound) {
        dateRange.setLowerBound(lowerBound);
        return this;
    }

    public SearcherBuilder setUpperBound(LocalDate upperBound) {
        dateRange.setUpperBound(upperBound);
        return this;
    }

    public SearcherBuilder setRegex(String regex) {
        this.regex = regex;
        return this;
    }

    public SearcherBuilder setThreads(int threads) {
        if (threads > 0) {
            this.threads = threads;
        }
        return this;
    }

    public Searcher build() {
        return new Searcher(logDirectory, ".gz" , dateRange, 15);
    }

}
