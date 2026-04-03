package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.csv.TaskCsvExporter;
import ca.concordia.soen342.poc.csv.TaskCsvImporter;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvImportExportSelfTest {
    public static void main(String[] args) throws IOException {
        Path tempDir = Files.createTempDirectory("csv-poc-test-");
        Path inputPath = tempDir.resolve("tasks-input.csv");
        Path outputPath = tempDir.resolve("tasks-output.csv");

        String inputCsv = String.join(
            System.lineSeparator(),
            "taskId,title,description,creationDate,priority,status,dueDate,projectName,projectDescription,tags,subtasks,activityEntries",
            "10,Task One,Desc One,2026-03-21T09:30:00,HIGH,OPEN,2026-03-24,Iteration 2,Testing import export,tagA|tagB,1~Sub\\~title~OPEN|2~Second sub~DONE,1~2026-03-21T09:35:00~Created|2~2026-03-21T09:36:00~Edited",
            "11,Task Two,Desc Two,2026-03-21T10:00:00,MEDIUM,COMPLETED,2026-03-25,Iteration 2,Testing escaped chars,demo|csv,1~Run dry\\|run~OPEN,1~2026-03-21T10:05:00~Contains escaped\\|pipe"
        );
        Files.writeString(inputPath, inputCsv + System.lineSeparator());

        Path databasePath = Files.createTempFile("csv-poc-test-", ".db");
        TaskRepository repository = new SqliteTaskRepository(databasePath);
        TaskCsvImporter importer = new TaskCsvImporter();
        TaskCsvExporter exporter = new TaskCsvExporter();

        List<Task> importedTasks = importer.importFrom(inputPath, repository);

        assertEquals(2, importedTasks.size(), "Expected 2 imported tasks");

        Task firstTask = importedTasks.get(0);
        assertEquals(2, firstTask.getSubtasks().size(), "Expected 2 subtasks on first task");
        assertEquals("Sub~title", firstTask.getSubtasks().get(0).getTitle(), "Expected unescaped '~' in subtask title");
        assertEquals(2, firstTask.getActivityEntries().size(), "Expected 2 activity entries on first task");

        Task secondTask = importedTasks.get(1);
        assertEquals(1, secondTask.getSubtasks().size(), "Expected 1 subtask on second task");
        assertEquals("Run dry|run", secondTask.getSubtasks().get(0).getTitle(), "Expected unescaped '|' in subtask title");
        assertEquals(1, secondTask.getActivityEntries().size(), "Expected 1 activity entry on second task");

        exporter.exportTo(outputPath, repository.findAll());

        TaskRepository secondRepository = new SqliteTaskRepository(Files.createTempFile("csv-poc-roundtrip-", ".db"));
        List<Task> roundTripTasks = importer.importFrom(outputPath, secondRepository);

        assertEquals(2, roundTripTasks.size(), "Round-trip should keep 2 tasks");

        Task roundTripSecond = roundTripTasks.get(1);
        assertEquals("Run dry|run", roundTripSecond.getSubtasks().get(0).getTitle(), "Round-trip should preserve escaped subtask title");
        assertEquals(
            "Contains escaped|pipe",
            roundTripSecond.getActivityEntries().get(0).getDescription(),
            "Round-trip should preserve escaped activity entry description"
        );

        System.out.println("CSV self-test passed.");
        System.out.println("Input file: " + inputPath);
        System.out.println("Output file: " + outputPath);
        System.out.println("Database file: " + databasePath);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(message + " | expected=" + expected + ", actual=" + actual);
        }
    }
}
