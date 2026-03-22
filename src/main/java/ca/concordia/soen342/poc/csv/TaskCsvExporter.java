package ca.concordia.soen342.poc.csv;

import ca.concordia.soen342.poc.model.Tag;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.ActivityEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TaskCsvExporter {
    private static final String HEADER =
        "taskId,title,description,creationDate,priority,status,dueDate,projectName,projectDescription,tags,subtasks,activityEntries";

    public void exportTo(Path csvPath, List<Task> tasks) throws IOException {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append(HEADER).append(System.lineSeparator());

        for (Task task : tasks) {
            csvBuilder
                .append(task.getTaskId()).append(',')
                .append(escape(task.getTitle())).append(',')
                .append(escape(task.getDescription())).append(',')
                .append(task.getCreationDate()).append(',')
                .append(task.getPriority()).append(',')
                .append(task.getStatus()).append(',')
                .append(task.getDueDate()).append(',')
                .append(escape(task.getProject() == null ? "" : task.getProject().getName())).append(',')
                .append(escape(task.getProject() == null ? "" : task.getProject().getDescription())).append(',')
                .append(escape(joinTags(task))).append(',')
                .append(escape(joinSubtasks(task))).append(',')
                .append(escape(joinActivityEntries(task)))
                .append(System.lineSeparator());
        }

        Path parent = csvPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(csvPath, csvBuilder.toString());
    }

    private String joinTags(Task task) {
        return task.getTags().stream()
            .map(Tag::getKeyword)
            .collect(Collectors.joining("|"));
    }

    private String joinSubtasks(Task task) {
        return task.getSubtasks().stream()
            .map(this::serializeSubtask)
            .collect(Collectors.joining("|"));
    }

    private String joinActivityEntries(Task task) {
        return task.getActivityEntries().stream()
            .map(this::serializeActivityEntry)
            .collect(Collectors.joining("|"));
    }

    private String serializeSubtask(Subtask subtask) {
        return subtask.getSubtaskId()
            + "~"
            + escapeInline(subtask.getTitle())
            + "~"
            + escapeInline(subtask.getCompletionStatus());
    }

    private String serializeActivityEntry(ActivityEntry activityEntry) {
        return activityEntry.getActivityEntryId()
            + "~"
            + activityEntry.getTimestamp()
            + "~"
            + escapeInline(activityEntry.getDescription());
    }

    private String escapeInline(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("|", "\\|")
            .replace("~", "\\~");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("|")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
