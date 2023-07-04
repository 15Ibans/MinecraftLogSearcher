package me.ibans.minecraftlogsearch.filesearcher.impl;

import me.ibans.minecraftlogsearch.SearchData;
import me.ibans.minecraftlogsearch.filesearcher.FileSearcher;
import org.apache.commons.collections4.ListUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StandardFileSearcher extends FileSearcher {

    @Override
    public SearchData getResults(List<File> files, int threads, String searchTerm, boolean ignoreCase) {
        final SearchData data = new SearchData(false);
        List<List<File>> partitioned = ListUtils.partition(files, threads);
        AtomicInteger counter = new AtomicInteger(1);
        ExecutorService es = Executors.newCachedThreadPool();

        for (List<File> sublist : partitioned) {
            es.execute(() -> {
                for (File file : sublist) {
                    System.out.print("\rSearching (" + counter.getAndIncrement() + "/" + files.size() + " logs processed)");
                    try {
                        searchFile(file, data, searchTerm, ignoreCase);
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
        return data;
    }
}
