package me.ibans.minecraftlogsearch;

import java.util.regex.Pattern;

public class SearchOptions {

    private String searchTerm = null;
    private Pattern regex = null;
    private boolean ignoreCase = false;

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Pattern getRegex() {
        return regex;
    }

    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}
