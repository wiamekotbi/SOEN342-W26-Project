package ca.concordia.soen342.poc.model;

import java.time.LocalDateTime;

public class ActivityEntry {
    private int activityEntryId;
    private LocalDateTime timestamp;
    private String description;

    public ActivityEntry() {
    }

    public ActivityEntry(int activityEntryId, LocalDateTime timestamp, String description) {
        this.activityEntryId = activityEntryId;
        this.timestamp = timestamp;
        this.description = description;
    }

    public int getActivityEntryId() {
        return activityEntryId;
    }

    public void setActivityEntryId(int activityEntryId) {
        this.activityEntryId = activityEntryId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
