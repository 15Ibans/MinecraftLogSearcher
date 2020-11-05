package me.ibans.minecraftlogsearch;

import me.ibans.minecraftlogsearch.util.FileUtils;
import me.ibans.minecraftlogsearch.util.TerminalColorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.util.Scanner;

public class TerminalFileChooser {

    private static File[] getDirectoryContents(String directory) {
        return FileUtils.getFolders(directory);
    }

    public static File selectFile(File currentDirectory) {

        File selectedDirectory = null;
        boolean isSelected = false;
        while (!isSelected) {
            System.out.println(TerminalColorUtils.ANSI_YELLOW + "Current directory: " + TerminalColorUtils.ANSI_RESET + currentDirectory.getAbsolutePath());
            System.out.println("Enter number corresponding to the file.\nType \"u\" to go up a level.\nType cd <#> to list more subdirectories\n");
            File[] subfolders = getDirectoryContents(currentDirectory.getAbsolutePath());
            if (subfolders.length < 1) {
                System.out.println("This directory contains no subfolders\n");
            } else {
                int i = 1;
                for (File subfolder: subfolders) {
                    System.out.println(i + ". " + subfolder.getPath());
                    i++;
                }
                System.out.println("");
            }

            Scanner input = new Scanner(System.in);
            System.out.print("Select file: ");

            String selection = input.nextLine();
            try {
                int value = Integer.parseInt(selection);
                if (subfolders.length >= 1 && value >= 1 && value <= subfolders.length) {
                    selectedDirectory = subfolders[value - 1];
                    isSelected = true;
                }
            } catch (NumberFormatException e) {

                if (selection.equalsIgnoreCase("u")) {
                    if (currentDirectory.getParentFile() == null) continue;
                    currentDirectory = currentDirectory.getParentFile();
                } else if (subfolders.length >= 1 && selection.toLowerCase().startsWith("cd ")
                        && NumberUtils.isParsable(StringUtils.removeStart(selection, "cd "))
                        && NumberUtils.toInt(StringUtils.removeStart(selection, "cd ")) >= 1
                        && NumberUtils.toInt(StringUtils.removeStart(selection, "cd ")) <= subfolders.length) {
                    int index = NumberUtils.toInt(StringUtils.removeStart(selection, "cd "));
                    currentDirectory = subfolders[index - 1];
                }
            }

        }

        return selectedDirectory;

    }

}
