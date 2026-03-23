package ca.concordia.soen342.poc.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private int taskId;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private PriorityLevel priority;
    private Status status;
    private LocalDate dueDate;
    private Project project;
    private final List<Subtask> subtasks = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();
    private final List<ActivityEntry> activityEntries = new ArrayList<>();

    public Task() {
    }

    public Task(
        int taskId,
        String title,
        String description,
        LocalDateTime creationDate,
        PriorityLevel priority,
        Status status,
        LocalDate dueDate
    ) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<ActivityEntry> getActivityEntries() {
        return activityEntries;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public void addActivityEntry(ActivityEntry entry) {
        activityEntries.add(entry);
    }
}
