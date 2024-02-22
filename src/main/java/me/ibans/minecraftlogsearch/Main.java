package me.ibans.minecraftlogsearch;

import me.ibans.minecraftlogsearch.util.PropertiesUtil;
import me.ibans.minecraftlogsearch.util.StringUtil;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    private static String directory = null;
    private static boolean showMenu = true;

    // needed to run setDefaultLogsDirectory method only on startup
    private static boolean defaultDirectorySetAttempt = false;

    private static DateRange dateRange = new DateRange();

    public static boolean isDebug = false;

    /*
        args:
             -f         --file              set the log directory
             -s         --string            search for string in files
             -is        --istring           search for string in files (case-insensitive)
             -r         --regex             search in files with regex
             -lb        --lowerbound        lower date bound
             -ub        --upperbound        upper date bound

        example: searcher -string "bruh moment" -lowerbound 2023-05-03 -upperbound
     */
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        Searcher s = null;

        try {
            Options options = setupArgs();
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (!cmd.hasOption("f")) {
                System.out.println("Missing required argument: -f");
                return;
            }

            File directory = new File(cmd.getOptionValue("f"));
            SearcherBuilder builder = new SearcherBuilder();
            builder.setLogDirectory(directory);

            SearchOptions searchOptions = new SearchOptions();

            if (cmd.hasOption("s")) {
                String searchTerm = cmd.getOptionValue("s");
                searchOptions.setSearchTerm(searchTerm);
            }
            if (cmd.hasOption("r")) {
                String regexString = cmd.getOptionValue("r");
                if (!StringUtil.isRegexValid(regexString)) {
                    System.out.println("Invalid regex pattern: " + regexString);
                    return;
                }
                Pattern pattern = Pattern.compile(regexString);
                searchOptions.setRegex(pattern);
            }
            if (cmd.hasOption("is")) {
                String searchTerm = cmd.getOptionValue("s");
                searchOptions.setSearchTerm(searchTerm);
                searchOptions.setIgnoreCase(true);
            }
            if (cmd.hasOption("lb")) {
                LocalDate lowerBound = parseDate(cmd.getOptionValue("lb"));
                builder.setLowerBound(lowerBound);
            }
            if (cmd.hasOption("ub")) {
                LocalDate upperBound = parseDate(cmd.getOptionValue("ub"));
                builder.setUpperBound(upperBound);
            }
            s = builder.build();
            s.searchFiles(searchOptions);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static LocalDate parseDate(String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
        return date;
    }

    private static Options setupArgs() {
        Options options = new Options();
        options.addOption("s", "string", true, "Search for string in files");
        options.addOption("is", "istring", true, "Search for string, case-insensitive");
        options.addOption("lb", "lowerbound", true, "Lower bound date in format YYYY-MM-DD");
        options.addOption("ub", "upperbound", true, "Upper bound date in format YYYY-MM-DD");
        options.addOption("r", "regex", true, "Search for string using a regex");
        options.addOption("d", "debug", false, "Enables debug mode");
        options.addOption("f", "file", true, "Set logs directory");

        return options;
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
                "6. Quit.\n" +
                "7. Test list amounts");
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
            case "7":
                runThreadBenchmark();
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
//        Searcher s = new Searcher(directory, ".gz", dateRange, 15);
        Searcher s = new SearcherBuilder()
                .setLogDirectory(directory)
                .setLowerBound(dateRange.getLowerBound())
                .setUpperBound(dateRange.getUpperBound())
                .setThreads(15)
                .build();
        SearchOptions searchOptions = new SearchOptions();

        if (s.canSearch()) {
            System.out.print("Enter something to search for: ");
            String searchTerm = input.nextLine();
            searchOptions.setSearchTerm(searchTerm);
            s.searchFiles(searchOptions);
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
//        Searcher s = new Searcher(directory, ".gz", dateRange);
        Searcher s = new SearcherBuilder()
                .setLogDirectory(directory)
                .setLowerBound(dateRange.getLowerBound())
                .setUpperBound(dateRange.getUpperBound())
                .build();
        SearchOptions searchOptions = new SearchOptions();

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
            searchOptions.setSearchTerm(searchTerm);
            System.out.print("Enter desired file name for saved dump: ");
            String dumpName = input.nextLine();
            System.out.println("Getting search results...");
            String dump = s.getDumpData(searchOptions);
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

    private static void runThreadBenchmark() {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter max # of threads to use: ");
        int threads = 1;
        if (input.hasNextInt()) {
            threads = input.nextInt();
        }
        DecimalFormat decimalFormat = new DecimalFormat("###.##");
        List<Double> times = new ArrayList<>();
        for (int i = 1; i <= threads; i++) {
            System.out.println("\nCurrently running at " + i + " threads");
//            Searcher searcher = new Searcher(directory, ".gz", dateRange, i);
            Searcher searcher = new SearcherBuilder()
                    .setLogDirectory(directory)
                    .setLowerBound(dateRange.getLowerBound())
                    .setUpperBound(dateRange.getUpperBound())
                    .setThreads(i)
                    .build();

            SearchOptions searchOptions = new SearchOptions();
            searchOptions.setSearchTerm("the");

            long start = System.currentTimeMillis();
            searcher.getResults(searchOptions);
            long duration = System.currentTimeMillis() - start;
            times.add(duration / 1000.0);
        }

        System.out.println();

        for (int i = 0; i < times.size(); i++) {
            System.out.println("For " + i + " thread(s): " + decimalFormat.format(times.get(i)) + " seconds.");
        }
    }
}
