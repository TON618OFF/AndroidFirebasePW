package com.example.firebasepw;

public class LogEntry {
    private String id;
    private String adminId;
    private String action;
    private long timestamp;

    public LogEntry() {}

    public LogEntry(String id, String adminId, String action, long timestamp) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.timestamp = timestamp;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}