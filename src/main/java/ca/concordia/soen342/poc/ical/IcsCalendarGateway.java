package ca.concordia.soen342.poc.ical;

import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class IcsCalendarGateway implements CalendarGateway {

    private static final DateTimeFormatter UTC_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    @Override
    public void exportTasks(List<Task> tasks, String filePath) throws IOException {
        if (tasks == null) {
            throw new IllegalArgumentException("tasks cannot be null");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath cannot be empty");
        }

        String normalizedPath = normalizeFilePath(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(normalizedPath))) {
            writer.write("BEGIN:VCALENDAR");
            writer.newLine();
            writer.write("VERSION:2.0");
            writer.newLine();
            writer.write("PRODID:-//SOEN342//TaskManager//EN");
            writer.newLine();
            writer.write("CALSCALE:GREGORIAN");
            writer.newLine();

            for (Task task : tasks) {
                if (task == null || task.getDueDate() == null) {
                    continue;
                }
                writeEvent(writer, task);
            }

            writer.write("END:VCALENDAR");
            writer.newLine();
        }
    }

    private void writeEvent(BufferedWriter writer, Task task) throws IOException {
        String uid = UUID.randomUUID() + "@soen342.local";
        String dtStamp = LocalDateTime.now(ZoneOffset.UTC).format(UTC_DATE_TIME);
        String dueDateTime = toUtcDateTime(task.getDueDate());

        writer.write("BEGIN:VEVENT");
        writer.newLine();

        writer.write("UID:" + escape(uid));
        writer.newLine();

        writer.write("DTSTAMP:" + dtStamp);
        writer.newLine();

        writer.write("DTSTART:" + dueDateTime);
        writer.newLine();

        writer.write("DTEND:" + dueDateTime);
        writer.newLine();

        writer.write("SUMMARY:" + escape(safe(task.getTitle())));
        writer.newLine();

        writer.write("DESCRIPTION:" + escape(buildDescription(task)));
        writer.newLine();

        writer.write("STATUS:" + mapStatus(task.getStatus()));
        writer.newLine();

        writer.write("PRIORITY:" + mapPriority(task.getPriority()));
        writer.newLine();

        writer.write("END:VEVENT");
        writer.newLine();
    }

    private String buildDescription(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append("Description: ").append(safe(task.getDescription()));
        sb.append("\\nStatus: ").append(task.getStatus() == null ? "" : task.getStatus().name());
        sb.append("\\nPriority: ").append(task.getPriority() == null ? "" : task.getPriority().name());

        Project project = task.getProject();
        sb.append("\\nProject: ").append(project == null ? "" : safe(project.getName()));

        List<Subtask> subtasks = task.getSubtasks();
        if (subtasks != null && !subtasks.isEmpty()) {
            sb.append("\\nSubtasks:");
            for (Subtask subtask : subtasks) {
                sb.append("\\n- ")
                        .append(safe(subtask.getTitle()))
                        .append(" [")
                        .append(safe(subtask.getCompletionStatus()))
                        .append("]");
            }
        }

        return sb.toString();
    }

    private String mapStatus(Status status) {
        if (status == null) {
            return "CONFIRMED";
        }
        if (status == Status.COMPLETED) {
            return "COMPLETED";
        }
        if (status == Status.CANCELLED) {
            return "CANCELLED";
        }
        return "CONFIRMED";
    }

    private int mapPriority(PriorityLevel priority) {
        if (priority == null) {
            return 0;
        }
        if (priority == PriorityLevel.HIGH) {
            return 1;
        }
        if (priority == PriorityLevel.MEDIUM) {
            return 5;
        }
        if (priority == PriorityLevel.LOW) {
            return 9;
        }
        return 0;
    }

    private String toUtcDateTime(LocalDate date) {
        return date.atTime(LocalTime.of(9, 0))
                .atOffset(ZoneOffset.UTC)
                .format(UTC_DATE_TIME);
    }

    private String normalizeFilePath(String filePath) {
        return filePath.toLowerCase().endsWith(".ics") ? filePath : filePath + ".ics";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}