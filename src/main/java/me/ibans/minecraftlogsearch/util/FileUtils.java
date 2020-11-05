package me.ibans.minecraftlogsearch.util;

import java.io.File;

public class FileUtils {

    public static File[] getFolders(String directory) {
        return new File(directory).listFiles(File::isDirectory);
    }

}
