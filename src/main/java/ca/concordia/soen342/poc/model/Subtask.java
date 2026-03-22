package ca.concordia.soen342.poc.model;

public class Subtask {
    private int subtaskId;
    private String title;
    private String completionStatus;

    public Subtask() {
    }

    public Subtask(int subtaskId, String title, String completionStatus) {
        this.subtaskId = subtaskId;
        this.title = title;
        this.completionStatus = completionStatus;
    }

    public int getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(int subtaskId) {
        this.subtaskId = subtaskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }
}
