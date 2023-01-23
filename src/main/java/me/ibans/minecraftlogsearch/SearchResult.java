package me.ibans.minecraftlogsearch;

import java.io.File;

public class SearchResult {

    private File file;
    private int lineNumber;
    private String content;

    public SearchResult(File file, String content, int lineNumber) {
        this.file = file;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }
}
