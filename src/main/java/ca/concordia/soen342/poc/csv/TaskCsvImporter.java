package ca.concordia.soen342.poc.csv;

import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Tag;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.model.ActivityEntry;
import ca.concordia.soen342.poc.repository.TaskRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskCsvImporter {
    public List<Task> importFrom(Path csvPath, TaskRepository taskRepository) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        List<Task> importedTasks = new ArrayList<>();
        if (lines.isEmpty()) {
            return importedTasks;
        }

        List<String> headerColumns = parseCsvLine(lines.get(0));
        Map<String, Integer> headerIndex = buildHeaderIndex(headerColumns);

        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }

            List<String> columns = parseCsvLine(line);
            Task task = mapTask(columns, headerIndex);
            taskRepository.save(task);
            importedTasks.add(task);
        }

        return importedTasks;
    }

    private Map<String, Integer> buildHeaderIndex(List<String> headerColumns) {
        Map<String, Integer> indexByName = new HashMap<>();
        for (int index = 0; index < headerColumns.size(); index++) {
            indexByName.put(headerColumns.get(index).trim().toLowerCase(), index);
        }
        return indexByName;
    }

    private Task mapTask(List<String> columns, Map<String, Integer> headerIndex) {
        Task task = new Task(
            Integer.parseInt(readColumn(columns, headerIndex, "taskid")),
            readColumn(columns, headerIndex, "title"),
            readColumn(columns, headerIndex, "description"),
            LocalDateTime.parse(readColumn(columns, headerIndex, "creationdate")),
            PriorityLevel.fromCsvValue(readColumn(columns, headerIndex, "priority")),
            Status.fromCsvValue(readColumn(columns, headerIndex, "status")),
            LocalDate.parse(readColumn(columns, headerIndex, "duedate"))
        );

        String projectName = readColumn(columns, headerIndex, "projectname");
        if (!projectName.isBlank()) {
            task.setProject(new Project(0, projectName, readColumn(columns, headerIndex, "projectdescription")));
        }

        String tags = readOptionalColumn(columns, headerIndex, "tags");
        if (!tags.isBlank()) {
            String[] tagKeywords = tags.split("\\|");
            int nextTagId = 1;
            for (String keyword : tagKeywords) {
                task.addTag(new Tag(nextTagId++, keyword.trim()));
            }
        }

        String subtasks = readOptionalColumn(columns, headerIndex, "subtasks");
        if (!subtasks.isBlank()) {
            List<String> subtaskEntries = splitEscaped(subtasks, '|');
            for (String entry : subtaskEntries) {
                List<String> fields = splitEscaped(entry, '~');
                if (fields.size() >= 3) {
                    task.addSubtask(new Subtask(
                        Integer.parseInt(unescapeInline(fields.get(0))),
                        unescapeInline(fields.get(1)),
                        unescapeInline(fields.get(2))
                    ));
                }
            }
        }

        String activityEntries = readOptionalColumn(columns, headerIndex, "activityentries");
        if (!activityEntries.isBlank()) {
            List<String> activityItems = splitEscaped(activityEntries, '|');
            for (String entry : activityItems) {
                List<String> fields = splitEscaped(entry, '~');
                if (fields.size() >= 3) {
                    task.addActivityEntry(new ActivityEntry(
                        Integer.parseInt(unescapeInline(fields.get(0))),
                        LocalDateTime.parse(unescapeInline(fields.get(1))),
                        unescapeInline(fields.get(2))
                    ));
                }
            }
        }

        return task;
    }

    private String readColumn(List<String> columns, Map<String, Integer> headerIndex, String headerName) {
        Integer index = headerIndex.get(headerName);
        if (index == null || index >= columns.size()) {
            throw new IllegalArgumentException("Missing required CSV column: " + headerName);
        }
        return columns.get(index).trim();
    }

    private String readOptionalColumn(List<String> columns, Map<String, Integer> headerIndex, String headerName) {
        Integer index = headerIndex.get(headerName);
        if (index == null || index >= columns.size()) {
            return "";
        }
        return columns.get(index).trim();
    }

    private List<String> splitEscaped(String input, char delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (int index = 0; index < input.length(); index++) {
            char currentChar = input.charAt(index);
            if (escaped) {
                if (currentChar == delimiter || currentChar == '\\') {
                    current.append(currentChar);
                } else {
                    current.append('\\').append(currentChar);
                }
                escaped = false;
            } else if (currentChar == '\\') {
                escaped = true;
            } else if (currentChar == delimiter) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        if (escaped) {
            current.append('\\');
        }

        tokens.add(current.toString());
        return tokens;
    }

    private String unescapeInline(String value) {
        StringBuilder unescaped = new StringBuilder();
        boolean escaped = false;

        for (int index = 0; index < value.length(); index++) {
            char currentChar = value.charAt(index);
            if (escaped) {
                unescaped.append(currentChar);
                escaped = false;
            } else if (currentChar == '\\') {
                escaped = true;
            } else {
                unescaped.append(currentChar);
            }
        }

        return unescaped.toString();
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);

            if (currentChar == '"') {
                if (insideQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (currentChar == ',' && !insideQuotes) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        columns.add(current.toString());
        return columns;
    }
}
