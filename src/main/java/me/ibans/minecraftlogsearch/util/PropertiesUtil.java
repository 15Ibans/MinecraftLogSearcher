package me.ibans.minecraftlogsearch.util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesUtil {

    public static final Path saveDirectory = Paths.get(System.getProperty("user.dir"), "settings.properties");

    public static void saveFile(String lastDir) {

        try (OutputStream output = new FileOutputStream(saveDirectory.toFile())) {

            final Properties prop = new Properties();
            prop.setProperty("lastDirectory", lastDir);

            prop.store(output, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile() {

        String directory = null;

        try (InputStream input = new FileInputStream(saveDirectory.toFile())) {
            final Properties prop = new Properties();
            prop.load(input);
            directory = prop.getProperty("lastDirectory");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return directory;

    }

}
