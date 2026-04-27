package com.library.service;

import com.library.exception.BusinessRuleException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMaintenanceService {

    private volatile Instant lastAutoMaintenanceAt;

    private final String datasourceUrl;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String backupDirectory;
    private final String backupFilename;
    private final String cleanupPathsRaw;

    public AdminMaintenanceService(
            @Value("${spring.datasource.url}") String datasourceUrl,
            @Value("${spring.datasource.username}") String datasourceUsername,
            @Value("${spring.datasource.password}") String datasourcePassword,
            @Value("${app.maintenance.backup.dir:${user.dir}/../backups}") String backupDirectory,
            @Value("${app.maintenance.backup.filename:library_db_backup.sql}") String backupFilename,
            @Value("${app.maintenance.cleanup.paths:${app.maintenance.temp.dir:${java.io.tmpdir}/library-management}}")
                    String cleanupPathsRaw) {
        this.datasourceUrl = datasourceUrl;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.backupDirectory = backupDirectory;
        this.backupFilename = backupFilename;
        this.cleanupPathsRaw = cleanupPathsRaw;
    }

    @Transactional
    public MaintenanceResult backupAndCleanup() {
        Path backupPath = runDatabaseBackup();
        CleanupStats stats = cleanupTemporaryFiles();

        // Ask JVM for a memory cleanup cycle after disk cleanup and backup.
        System.gc();

        return new MaintenanceResult(
                backupPath.toString(),
                stats.deletedFiles(),
                stats.freedBytes(),
                Instant.now());
    }

    /** Silent mode for automatic post-commit maintenance (no user-facing message flow). */
    @Transactional
    public void backupAndCleanupSilently() {
        try {
            backupAndCleanup();
            lastAutoMaintenanceAt = Instant.now();
        } catch (RuntimeException ignored) {
            // Intentionally swallow failures for automatic maintenance.
            // Manual admin-triggered actions still surface explicit errors.
        }
    }

    public Instant getLastAutoMaintenanceAt() {
        return lastAutoMaintenanceAt;
    }

    @Transactional
    public RestoreResult restoreFromLatestBackup() {
        String dbName = extractDatabaseName(datasourceUrl);
        Path backupFilePath = resolveBackupFilePath();
        if (!Files.exists(backupFilePath)) {
            throw new BusinessRuleException(
                    "No backup file was found to restore. Please create a backup first at: " + backupFilePath);
        }

        try {
            String normalizedPath = backupFilePath.toAbsolutePath().toString().replace("\\", "/");
            List<String> command = new ArrayList<>();
            command.add("mysql");
            command.add("--user=" + datasourceUsername);
            command.add("--password=" + datasourcePassword);
            command.add(dbName);
            command.add("-e");
            command.add("source " + normalizedPath);

            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessRuleException(
                        "Restore failed. Please ensure MySQL client tools are installed and backup file is valid.");
            }
            return new RestoreResult(backupFilePath.toString(), Instant.now());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessRuleException("Restore was interrupted. Please try again.");
        } catch (IOException ex) {
            throw new BusinessRuleException(
                    "Unable to run restore command. Check MySQL client installation and file permissions.");
        }
    }

    private Path runDatabaseBackup() {
        String dbName = extractDatabaseName(datasourceUrl);

        try {
            return createBackupAtDirectory(Paths.get(backupDirectory), dbName);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessRuleException("Maintenance was interrupted. Please try again.");
        } catch (IOException ex) {
            throw new BusinessRuleException(
                    "Unable to create database backup. Check backup folder permissions and MySQL tool availability.");
        }
    }

    private CleanupStats cleanupTemporaryFiles() {
        long deletedFiles = 0L;
        long freedBytes = 0L;
        for (Path path : resolveCleanupPaths()) {
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                continue;
            }
            CleanupStats stats = deleteDirectoryChildren(path);
            deletedFiles += stats.deletedFiles();
            freedBytes += stats.freedBytes();
        }
        return new CleanupStats(deletedFiles, freedBytes);
    }

    private CleanupStats deleteDirectoryChildren(Path directory) {
        long deletedFiles = 0L;
        long freedBytes = 0L;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path child : stream) {
                CleanupStats childStats = deleteRecursively(child);
                deletedFiles += childStats.deletedFiles();
                freedBytes += childStats.freedBytes();
            }
        } catch (IOException ex) {
            throw new BusinessRuleException("Temporary cleanup failed due to a file access error.");
        }
        return new CleanupStats(deletedFiles, freedBytes);
    }

    private CleanupStats deleteRecursively(Path target) {
        long deletedFiles = 0L;
        long freedBytes = 0L;

        try {
            if (Files.isDirectory(target)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(target)) {
                    for (Path child : stream) {
                        CleanupStats childStats = deleteRecursively(child);
                        deletedFiles += childStats.deletedFiles();
                        freedBytes += childStats.freedBytes();
                    }
                }
                Files.deleteIfExists(target);
                return new CleanupStats(deletedFiles, freedBytes);
            }

            long size = Files.size(target);
            if (Files.deleteIfExists(target)) {
                return new CleanupStats(deletedFiles + 1, freedBytes + size);
            }
            return new CleanupStats(deletedFiles, freedBytes);
        } catch (IOException ex) {
            throw new BusinessRuleException("Failed while deleting temporary files.");
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        // Example: jdbc:mysql://localhost:3306/library_db?useSSL=false
        int slashIndex = jdbcUrl.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex + 1 >= jdbcUrl.length()) {
            throw new BusinessRuleException("Invalid datasource URL. Cannot determine database name for backup.");
        }
        String dbAndQuery = jdbcUrl.substring(slashIndex + 1);
        int queryIndex = dbAndQuery.indexOf('?');
        return queryIndex >= 0 ? dbAndQuery.substring(0, queryIndex) : dbAndQuery;
    }

    private Path resolveBackupFilePath() {
        return Paths.get(backupDirectory).resolve(backupFilename);
    }

    private Path createBackupAtDirectory(Path backupDir, String dbName) throws IOException, InterruptedException {
        Files.createDirectories(backupDir);
        Path finalBackupPath = backupDir.resolve(backupFilename);
        Path tempBackupPath = backupDir.resolve(backupFilename + ".tmp");
        Files.deleteIfExists(tempBackupPath);

        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("--single-transaction");
        command.add("--quick");
        command.add("--routines");
        command.add("--events");
        command.add("--user=" + datasourceUsername);
        command.add("--password=" + datasourcePassword);
        command.add("--result-file=" + tempBackupPath);
        command.add(dbName);

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new BusinessRuleException(
                    "Database backup failed. Please ensure MySQL tools (mysqldump) are installed and available in PATH.");
        }

        Files.move(tempBackupPath, finalBackupPath, StandardCopyOption.REPLACE_EXISTING);
        return finalBackupPath;
    }

    private List<Path> resolveCleanupPaths() {
        if (cleanupPathsRaw == null || cleanupPathsRaw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(cleanupPathsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Paths::get)
                .toList();
    }

    private record CleanupStats(long deletedFiles, long freedBytes) {}

    public record MaintenanceResult(String backupPath, long deletedTempFiles, long freedBytes, Instant executedAt) {}

    public record RestoreResult(String backupPath, Instant executedAt) {}
}
