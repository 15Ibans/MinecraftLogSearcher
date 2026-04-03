package me.ibans.minecraftlogsearch;

import me.ibans.minecraftlogsearch.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SearchOptions {

    private final List<SearchTerm> searchTerms = new ArrayList<>();
    private Pattern regex = null;
    private boolean ignoreCase = false;

    public List<SearchTerm> getSearchTerms() {
        return searchTerms;
    }

    public void addSearchTerm(SearchTerm searchTerm) {
        this.searchTerms.add(searchTerm);
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

    public boolean validate(String line) {
        return searchTerms.stream().allMatch(searchTerm -> StringUtil.contains(line, searchTerm.getContent(), searchTerm.isIgnoreCase()));
    }
}
