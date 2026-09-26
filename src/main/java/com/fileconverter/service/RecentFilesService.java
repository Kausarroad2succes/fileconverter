package com.fileconverter.service;

import com.fileconverter.model.RecentFileEntry;
import com.fileconverter.model.Tool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RecentFilesService {

    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".fileconverter";
    private static final String DB_PATH = DB_DIR + File.separator + "recent_files.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    /** Creates the database, both tables, and their relationship. Call once at startup. */
    public static void init() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String createToolsTable = """
            CREATE TABLE IF NOT EXISTS tools (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE
            )
            """;

        String createRecentFilesTable = """
            CREATE TABLE IF NOT EXISTS recent_files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                file_path TEXT NOT NULL,
                tool_id INTEGER NOT NULL,
                opened_at TEXT NOT NULL DEFAULT (datetime('now')),
                FOREIGN KEY (tool_id) REFERENCES tools(id)
            )
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(createToolsTable);
            stmt.execute(createRecentFilesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Finds a tool by name, or creates it if it doesn't exist yet. Returns its id. */
    private static int getOrCreateToolId(Connection conn, String toolName) throws SQLException {
        String selectSql = "SELECT id FROM tools WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, toolName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO tools (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, toolName);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    // ---- CREATE ----
    public static void addRecentFile(String filePath, String toolName) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int toolId = getOrCreateToolId(conn, toolName);

            String insertSql = "INSERT INTO recent_files (file_path, tool_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, filePath);
                stmt.setInt(2, toolId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- READ ----
    public static List<RecentFileEntry> getRecentFiles(int limit) {
        List<RecentFileEntry> results = new ArrayList<>();
        String selectSql = """
            SELECT recent_files.id, recent_files.file_path, tools.name AS tool_name, recent_files.opened_at
            FROM recent_files
            JOIN tools ON recent_files.tool_id = tools.id
            ORDER BY recent_files.id DESC
            LIMIT ?
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new RecentFileEntry(
                            rs.getInt("id"),
                            rs.getString("file_path"),
                            rs.getString("tool_name"),
                            rs.getString("opened_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<Tool> getAllTools() {
        List<Tool> tools = new ArrayList<>();
        String selectSql = "SELECT id, name FROM tools ORDER BY name";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                tools.add(new Tool(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tools;
    }

    // ---- UPDATE ----
    /** Updates the opened_at timestamp to now, e.g. when a file is reopened. */
    public static void touchRecentFile(int id) {
        String updateSql = "UPDATE recent_files SET opened_at = datetime('now') WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---- DELETE ----
    public static void deleteRecentFile(int id) {
        String deleteSql = "DELETE FROM recent_files WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void clearAllRecentFiles() {
        String deleteSql = "DELETE FROM recent_files";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(deleteSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}