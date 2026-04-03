package me.ibans.minecraftlogsearch;

public class SearchTerm {

    private final String content;
    private final boolean ignoreCase;

    public SearchTerm(String content, boolean ignoreCase) {
        this.content = content;
        this.ignoreCase = ignoreCase;
    }

    public String getContent() {
        return content;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
