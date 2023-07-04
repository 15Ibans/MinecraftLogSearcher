package me.ibans.minecraftlogsearch.filesearcher;

import me.ibans.minecraftlogsearch.SearchData;
import me.ibans.minecraftlogsearch.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

public abstract class FileSearcher {

    public abstract SearchData getResults(List<File> files, int threads, String searchTerm, boolean ignoreCase);

    protected void searchFile(File file, SearchData data, String searchTerm, boolean ignoreCase) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        // searches compressed log, if it's latest.log it will be null since it's not compressed
        GZIPInputStream gis = file.getName().endsWith(".gz") ? new GZIPInputStream(fis) : null;
        BufferedReader buffered = new BufferedReader(new InputStreamReader(gis != null ? gis : fis));
        int lineNumber = 0;
        for (String line; (line = buffered.readLine()) != null; ) {
            if (StringUtil.contains(line, searchTerm, ignoreCase)) {
                data.addToNumFound(StringUtils.countMatches(line, searchTerm));
                data.addSearchResult(file, line, lineNumber);
            }
            lineNumber++;
        }
        if (gis != null) gis.close();
        buffered.close();
    }

}
