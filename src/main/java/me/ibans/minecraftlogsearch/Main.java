package me.ibans.minecraftlogsearch;

import me.ibans.minecraftlogsearch.util.PropertiesUtil;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    private static String directory = null;
    private static boolean showMenu = true;

    // needed to run setDefaultLogsDirectory method only on startup
    private static boolean defaultDirectorySetAttempt = false;

    private static DateRange dateRange = new DateRange();

    public static void main(String[] args) {
        while (showMenu) {
            menu();
        }
    }

    private static void menu() {
        try {
            // so the directory chooser matches the os file chooser
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |  UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        if (!defaultDirectorySetAttempt) setDefaultLogsDirectory();
        if (directory != null) {
            System.out.println("Minecraft log directory currently set to " + directory);
        }
        else {
            System.out.println("Unable to find a .minecraft directory. Be sure to specify one using option 2.");
            directory = System.getProperty("user.home");
        }
        System.out.println("Select an option: \n");
        System.out.println("1. Search your logs.\n" +
                "2. Change logs directory.\n" +
                "3. Change logs directory (via terminal).\n" +
                "4. Change date bounds.\n" +
                "5. Dump search results to text file.\n" +
                "6. Quit.\n");
        Scanner selector = new Scanner(System.in);
        System.out.print("\nOption: ");
        String option = selector.nextLine();

        switch (option) {
            case "1":
                searchFiles(directory);
                break;
            case "2":
                changeLogDirectory();
                break;
            case "3":
                changeLogDirectoryTerminal();
                break;
            case "4":
                setDateRangeBounds();
                break;
            case "5":
                dumpSearchResults();
                break;
            case "6":
                showMenu = false;
                break;
            default:
                System.out.println("Invalid option selected.");
        }
    }

    // gets the default log directory of where the typical .minecraft installation
    // is depending on the os
    private static void setDefaultLogsDirectory() {
        if (Files.exists(PropertiesUtil.saveDirectory)) {
            String savedDir = PropertiesUtil.readFile();
            if (savedDir != null && Files.exists(Paths.get(savedDir))) {
                directory = savedDir;
                return;
            }
        }

        String dir = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            dir = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", ".minecraft", "logs").toString();
        }
        // mac now works, linux still needs testing
        else if (SystemUtils.IS_OS_MAC) {
            dir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "minecraft", "logs").toString();
        }
        else if (SystemUtils.IS_OS_LINUX) {
            dir = Paths.get(System.getProperty("user.home"), ".minecraft").toString();
        }
        File f = new File(dir);
        if (f.exists()) {
            directory = dir;
            PropertiesUtil.saveFile(directory);
        }
        defaultDirectorySetAttempt = true;
    }

    private static void searchFiles(String directory) {
        Scanner input = new Scanner(System.in);
        Searcher s = new Searcher(directory, ".gz", dateRange);
        if (s.canSearch()) {
            System.out.print("Enter something to search for: ");
            String searchTerm = input.nextLine();
            s.searchFiles(searchTerm);
        } else {
            System.out.println("No logs detected! Did you select a valid directory?");
        }
    }

    private static void changeLogDirectory() {
        System.out.println("Select a logs directory in the file dialog.");
        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.setApproveButtonText("Select");
        int result = f.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            directory = f.getSelectedFile().getAbsolutePath();
            PropertiesUtil.saveFile(directory);
            System.out.println("Directory set to " + directory + "\n");
        }
        else if (result == JFileChooser.CANCEL_OPTION) {
            System.out.println("Decided not to change the log directory.");
        }
    }

    private static void changeLogDirectoryTerminal() {
        directory = TerminalFileChooser.selectFile(new File(directory)).getAbsolutePath();
        PropertiesUtil.saveFile(directory);
    }

    private static void setDateRangeBounds() {
        Scanner input = new Scanner(System.in);
        System.out.println("Lower bound is currently set to " + dateRange.getLowerBound().toString());
        System.out.println("Upper bound is currently set to " + dateRange.getUpperBound().toString() + "\n");
        System.out.print("Enter lower bound date (leave blank for no lower bound): ");
        if (input.hasNext()) {
            String str = input.nextLine();
            try {
                LocalDate newLowerBound = LocalDate.parse(str);
                dateRange.setLowerBound(newLowerBound);
                System.out.println("New lower bound has been set to " + newLowerBound);
            } catch (DateTimeParseException ex) {
                System.out.println("Unable to parse string " + str + " to date.");
            }
        }
        System.out.print("Enter upper bound date (leave blank for no upper bound)");
        if (input.hasNext()) {
            String str = input.nextLine();
            try {
                LocalDate newUpperBound = LocalDate.parse(input.nextLine());
                dateRange.setUpperBound(newUpperBound);
                System.out.println("New upper bound has been set to " + newUpperBound);
            } catch (DateTimeParseException ex) {
                System.out.println("Unable to parse string " + str + " to date.");
            }
        }
    }

    private static void dumpSearchResults() {
        Scanner input = new Scanner(System.in);
        Searcher s = new Searcher(directory, ".gz", dateRange);
        final Path dumpsDir = Paths.get(System.getProperty("user.dir"), "dumps");
        if (!Files.exists(dumpsDir)) {
            System.out.println("Dumps directory doesn't exist, creating it now.");
            try {
                Files.createDirectory(dumpsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (s.canSearch()) {
            System.out.print("Enter something to search for: ");
            String searchTerm = input.nextLine();
            System.out.print("Enter desired file name for saved dump: ");
            String dumpName = input.nextLine();
            System.out.println("Getting search results...");
            String dump = s.getDumpData(searchTerm);
            System.out.println("Writing to file...\n");

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(dumpsDir.resolve(dumpName).toFile()));
                writer.write(dump);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
