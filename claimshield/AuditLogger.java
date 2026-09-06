package claimshield;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Nguyen Ngoc Quang Dang - S4113887
 *
 * Utility service responsible for append-only audit trail logging to
 * data/logs.txt.
 * Records all system administrative, management, and processing actions with
 * timestamps and actor IDs.
 */
public class AuditLogger {
    /** Single source of truth for the log location, shared with every other file path. */
    private static final String LOG_FILE_PATH = ConsoleSupport.LOGS_FILE;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Appends an audit trail entry to data/logs.txt.
     * Format: timestamp,userId,actionPerformed,targetEntityId
     *
     * @param userId          the ID of the actor performing the action (e.g.
     *                        u-0000001)
     * @param actionPerformed the operation executed (e.g. REGISTER_POLICYHOLDER,
     *                        UPDATE_CLAIM_STATUS)
     * @param targetEntityId  the ID of the entity affected (e.g. f-1000000001,
     *                        c-1000001)
     */
    public static synchronized void log(String userId, String actionPerformed, String targetEntityId) {
        String actor = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "SYSTEM";
        String action = (actionPerformed != null) ? actionPerformed.trim() : "UNKNOWN_ACTION";
        String target = (targetEntityId != null) ? targetEntityId.trim() : "N/A";
        String timestamp = LocalDateTime.now().format(FORMATTER);

        String logLine = timestamp + "," + actor + "," + action + "," + target;

        File logFile = new File(LOG_FILE_PATH);
        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(logLine);
        } catch (IOException e) {
            System.err.println("AuditLogger Error: Unable to write audit log - " + e.getMessage());
        }
    }

    /**
     * Reads all audit log entries from data/logs.txt, returned in
     * reverse-chronological order (most recent first).
     *
     * @return a List of log entry records, newest first
     */
    public static List<AuditEntry> readLogsMostRecentFirst() {
        return readLogsMostRecentFirst(LOG_FILE_PATH);
    }

    /**
     * Reads all audit log entries from the given file, returned in
     * reverse-chronological order (most recent first).
     *
     * @param filePath the path to the audit log file
     * @return a List of log entry records, newest first
     */
    public static List<AuditEntry> readLogsMostRecentFirst(String filePath) {
        List<AuditEntry> entries = new ArrayList<>();
        File logFile = new File(filePath);
        if (!logFile.exists()) {
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    entries.add(new AuditEntry(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("AuditLogger Error: Unable to read audit log - " + e.getMessage());
        }

        Collections.reverse(entries);
        return entries;
    }

    /**
     * Represents a single parsed audit log line record.
     */
    public static class AuditEntry {
        private final String timestamp;
        private final String actorId;
        private final String action;
        private final String targetId;

        public AuditEntry(String timestamp, String actorId, String action, String targetId) {
            this.timestamp = timestamp;
            this.actorId = actorId;
            this.action = action;
            this.targetId = targetId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getActorId() {
            return actorId;
        }

        public String getAction() {
            return action;
        }

        public String getTargetId() {
            return targetId;
        }

        @Override
        public String toString() {
            return String.format("[%s] Actor: %-10s | Action: %-25s | Target ID: %s",
                    timestamp, actorId, action, targetId);
        }
    }
}
