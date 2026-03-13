package com.fileorganizer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

/**
 * Main class for the File Organizer Tool.
 * Scans a directory, categorizes files by extension, and moves them to organized subfolders.
 * Supports dry-run mode, date-based subfolders, duplicate handling, and logging.
 */
public class FileOrganizer {
    private static final Logger LOGGER = Logger.getLogger(FileOrganizer.class.getName());
    private static final Map<String, String> FILE_CATEGORIES = new HashMap<>();
    private Path sourceDir;
    private boolean dryRun;
    private Path logFile;
    private int filesScanned = 0;
    private int filesMoved = 0;
    private int filesSkipped = 0;
    private List<String> logEntries = new ArrayList<>();

    static {
        // Define file categories and their extensions
        FILE_CATEGORIES.put("jpg", "Images");
        FILE_CATEGORIES.put("jpeg", "Images");
        FILE_CATEGORIES.put("png", "Images");
        FILE_CATEGORIES.put("gif", "Images");
        FILE_CATEGORIES.put("bmp", "Images");
        FILE_CATEGORIES.put("tiff", "Images");
        FILE_CATEGORIES.put("mp4", "Videos");
        FILE_CATEGORIES.put("avi", "Videos");
        FILE_CATEGORIES.put("mkv", "Videos");
        FILE_CATEGORIES.put("mov", "Videos");
        FILE_CATEGORIES.put("wmv", "Videos");
        FILE_CATEGORIES.put("pdf", "PDFs");
        FILE_CATEGORIES.put("doc", "Documents");
        FILE_CATEGORIES.put("docx", "Documents");
        FILE_CATEGORIES.put("xls", "Documents");
        FILE_CATEGORIES.put("xlsx", "Documents");
        FILE_CATEGORIES.put("ppt", "Documents");
        FILE_CATEGORIES.put("pptx", "Documents");
        FILE_CATEGORIES.put("txt", "Documents");
        FILE_CATEGORIES.put("mp3", "Audio");
        FILE_CATEGORIES.put("wav", "Audio");
        FILE_CATEGORIES.put("flac", "Audio");
        FILE_CATEGORIES.put("aac", "Audio");
    }

    public FileOrganizer(Path sourceDir, boolean dryRun) {
        this.sourceDir = sourceDir;
        this.dryRun = dryRun;
        this.logFile = sourceDir.resolve("organized_files_log.txt");
        setupLogger();
    }

    /**
     * Sets up file logging.
     */
    private void setupLogger() {
        try {
            Files.deleteIfExists(logFile); // Clear previous log
            FileHandler fileHandler = new FileHandler(logFile.toString(), true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            System.err.println("Failed to setup logger: " + e.getMessage());
        }
    }

    /**
     * Scans the source directory and organizes files.
     */
    public void organizeFiles() {
        try {
            Files.walkFileTree(sourceDir, new FileVisitorImpl());
            printSummary();
            logSummary();
        } catch (IOException e) {
            System.err.println("Error scanning directory: " + e.getMessage());
            LOGGER.severe("Error scanning directory: " + e.getMessage());
        }
    }

    /**
     * Gets the category for a file extension.
     * @param extension File extension (lowercase)
     * @return Category name or "Others"
     */
    private String getFileCategory(String extension) {
        return FILE_CATEGORIES.getOrDefault(extension.toLowerCase(), "Others");
    }

    /**
     * Generates unique target path handling duplicates.
     * @param source Source Path
     * @param targetDir Target directory
     * @return Unique target Path
     */
    private Path getUniqueTargetPath(Path source, Path targetDir) throws IOException {
        String fileName = source.getFileName().toString();
        Path targetPath = targetDir.resolve(fileName);

        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        // Handle duplicate by appending (1), (2), etc.
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        int counter = 1;

        do {
            String newFileName = String.format("%s(%d).%s", nameWithoutExt, counter, extension);
            targetPath = targetDir.resolve(newFileName);
            counter++;
        } while (Files.exists(targetPath));

        return targetPath;
    }

    /**
     * Gets year folder based on file's last modified date.
     * @param path File path
     * @return Year folder name
     */
    private String getYearFolder(Path path) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        LocalDateTime modifiedTime = attrs.lastModifiedTime()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return modifiedTime.format(DateTimeFormatter.ofPattern("yyyy"));
    }

    /**
     * Logs a message to console and file.
     */
    private void logMove(Path source, Path target) {
        String message = String.format("Moved: %s -> %s", source.getFileName(), target.getFileName());
        System.out.println(message);
        logEntries.add(message);
        LOGGER.info(message);
    }

    /**
     * Prints the summary of operations.
     */
    private void printSummary() {
        System.out.println("\n=== SUMMARY ===");
        System.out.printf("Files scanned: %d%n", filesScanned);
        System.out.printf("Files moved: %d%n", filesMoved);
        System.out.printf("Files skipped: %d%n", filesSkipped);
        System.out.println("Log saved to: " + logFile.toAbsolutePath());
    }

    /**
     * Logs the summary to file.
     */
    private void logSummary() {
        String summary = String.format(
            "\nSUMMARY: Scanned=%d, Moved=%d, Skipped=%d",
            filesScanned, filesMoved, filesSkipped
        );
        logEntries.add(summary);
        LOGGER.info(summary);
    }

    /**
     * FileVisitor implementation for walking the directory tree.
     */
    private class FileVisitorImpl extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitor.Result visitFile(Path file, BasicFileAttributes attrs) {
            if (attrs.isRegularFile()) {
                filesScanned++;
                String fileName = file.getFileName().toString();
                if (fileName.contains("organized_files_log.txt")) {
                    filesSkipped++;
                    return FileVisitor.Result.CONTINUE;
                }

                try {
                    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                    String category = getFileCategory(extension);
                    String year = getYearFolder(file);

                    Path categoryDir = sourceDir.resolve(category).resolve(year);
                    Files.createDirectories(categoryDir);

                    Path targetPath = getUniqueTargetPath(file, categoryDir);

                    if (dryRun) {
                        System.out.printf("[DRY RUN] Would move: %s -> %s/%s/%s%n",
                            file.getFileName(), category, year, targetPath.getFileName());
                        filesMoved++; // Count as moved for summary
                    } else {
                        Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        logMove(file, targetPath);
                        filesMoved++;
                    }
                } catch (IOException e) {
                    System.err.printf("Error processing %s: %s%n", file.getFileName(), e.getMessage());
                    filesSkipped++;
                    LOGGER.severe("Error processing " + file.getFileName() + ": " + e.getMessage());
                }
            }
            return FileVisitor.Result.CONTINUE;
        }

        @Override
        public FileVisitor.Result visitFileFailed(Path file, IOException exc) {
            System.err.printf("Failed to access: %s (%s)%n", file, exc.getMessage());
            filesSkipped++;
            return FileVisitor.Result.CONTINUE;
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: java FileOrganizer <source_directory> [dry-run]");
            System.out.println("Example: java FileOrganizer C:\\Users\\Downloads true");
            return;
        }

        Path sourceDir;
        boolean dryRun = false;

        try {
            sourceDir = Paths.get(args[0]).toAbsolutePath().normalize();
            if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
                System.err.println("Error: Source directory does not exist or is not a directory.");
                return;
            }
        } catch (InvalidPathException e) {
            System.err.println("Error: Invalid path " + args[0]);
            return;
        }

        if (args.length == 2 && "true".equalsIgnoreCase(args[1])) {
            dryRun = true;
            System.out.println("*** DRY-RUN MODE ENABLED *** (No files will be moved)");
        }

        System.out.println("Starting file organization for: " + sourceDir);
        System.out.printf("Mode: %s%n%n", dryRun ? "DRY-RUN" : "REAL");

        FileOrganizer organizer = new FileOrganizer(sourceDir, dryRun);
        organizer.organizeFiles();
    }
}

