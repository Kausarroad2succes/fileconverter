package com.fileconverter.model;

public class RecentFileEntry {
    private final int id;
    private final String filePath;
    private final String toolName; // joined from tools table
    private final String openedAt;

    public RecentFileEntry(int id, String filePath, String toolName, String openedAt) {
        this.id = id;
        this.filePath = filePath;
        this.toolName = toolName;
        this.openedAt = openedAt;
    }

    public int getId() { return id; }
    public String getFilePath() { return filePath; }
    public String getToolName() { return toolName; }
    public String getOpenedAt() { return openedAt; }

    public String getFileName() {
        return new java.io.File(filePath).getName();
    }
}