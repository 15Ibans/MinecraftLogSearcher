package me.ibans.minecraftlogsearch;

import java.io.File;
import java.time.LocalDate;

public class SearcherBuilder {

    private File logDirectory;
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

    public SearcherBuilder setLowerBound(LocalDate lowerBound) {
        dateRange.setLowerBound(lowerBound);
        return this;
    }

    public SearcherBuilder setUpperBound(LocalDate upperBound) {
        dateRange.setUpperBound(upperBound);
        return this;
    }

    public SearcherBuilder setThreads(int threads) {
        if (threads > 0) {
            this.threads = threads;
        }
        return this;
    }

    public Searcher build() {
        return new Searcher(logDirectory, ".gz" , dateRange, threads);
    }

}
