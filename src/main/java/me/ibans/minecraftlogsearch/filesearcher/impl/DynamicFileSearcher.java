package me.ibans.minecraftlogsearch.filesearcher.impl;

import me.ibans.minecraftlogsearch.Main;
import me.ibans.minecraftlogsearch.SearchData;
import me.ibans.minecraftlogsearch.filesearcher.FileSearcher;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicFileSearcher extends FileSearcher {

    private class DynamicFileSearcherThread implements Runnable {

        Queue<File> fileQueue;
        SearchData data;
        String searchTerm;
        boolean ignoreCase;
        AtomicInteger counter;
        int filesSize;

        public DynamicFileSearcherThread(Queue<File> fileQueue, AtomicInteger counter, int filesSize, SearchData data, String searchTerm, boolean ignoreCase) {
            this.fileQueue = fileQueue;
            this.data = data;
            this.filesSize = filesSize;
            this.searchTerm = searchTerm;
            this.ignoreCase = ignoreCase;
            this.counter = counter;
        }

        @Override
        public void run() {
            File head;
            while ((head = fileQueue.poll()) != null) {
                System.out.print("\rSearching (" + counter.getAndIncrement() + "/" + filesSize + " logs processed)");
                try {
                    searchFile(head, data, searchTerm, ignoreCase);
                } catch (IOException e) {
                    System.out.println("Got an IO exception when processing file " + head.getAbsolutePath());
                    e.printStackTrace();
                }
                if (Main.isDebug) {
                    System.out.println("Finished processing file " + head.getName());
                }
            }
        }

    }

    @Override
    public SearchData getResults(List<File> files, int threads, String searchTerm, boolean ignoreCase) {
        final SearchData data = new SearchData(false);
        AtomicInteger counter = new AtomicInteger(1);
        ExecutorService es = Executors.newCachedThreadPool();

        Queue<File> fileQueue = new ConcurrentLinkedQueue<>(files);
        int size = fileQueue.size();
        for (int i = 0; i < threads; i++) {
            es.execute(new DynamicFileSearcherThread(fileQueue, counter, size, data, searchTerm, ignoreCase));
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
