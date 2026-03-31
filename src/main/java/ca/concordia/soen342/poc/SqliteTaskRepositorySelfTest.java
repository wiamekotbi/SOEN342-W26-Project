package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.model.ActivityEntry;
import ca.concordia.soen342.poc.model.Collaborator;
import ca.concordia.soen342.poc.model.CollaboratorCategory;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Tag;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class SqliteTaskRepositorySelfTest {
    public static void main(String[] args) throws Exception {
        Path databasePath = Files.createTempFile("sqlite-task-repo-", ".db");
        TaskRepository repository = new SqliteTaskRepository(databasePath);

        Task original = new Task(
            42,
            "Persist task",
            "Round-trip through SQLite",
            LocalDateTime.of(2026, 3, 31, 10, 15),
            PriorityLevel.HIGH,
            Status.OPEN,
            LocalDate.of(2026, 4, 5)
        );
        original.setProject(new Project(7, "Iteration 3", "Persistence implementation"));
        original.addSubtask(new Subtask(1, "Create schema", "DONE"));
        original.addSubtask(new Subtask(2, "Save task graph", "OPEN"));
        original.addTag(new Tag(1, "database"));
        original.addTag(new Tag(2, "sqlite"));
        original.addActivityEntry(new ActivityEntry(1, LocalDateTime.of(2026, 3, 31, 10, 20), "Created task"));
        original.addCollaborator(new Collaborator(3, "Sara", CollaboratorCategory.INTERMEDIATE));

        repository.save(original);

        Optional<Task> reloaded = repository.findById(42);
        assertTrue(reloaded.isPresent(), "Expected persisted task to be found");

        Task loadedTask = reloaded.get();
        assertEquals("Persist task", loadedTask.getTitle(), "Title should round-trip");
        assertEquals("Iteration 3", loadedTask.getProject().getName(), "Project should round-trip");
        assertEquals(2, loadedTask.getSubtasks().size(), "Subtasks should round-trip");
        assertEquals(2, loadedTask.getTags().size(), "Tags should round-trip");
        assertEquals(1, loadedTask.getActivityEntries().size(), "Activity entries should round-trip");
        assertEquals(1, loadedTask.getCollaborators().size(), "Collaborators should round-trip");
        assertEquals("Sara", loadedTask.getCollaborators().get(0).getName(), "Collaborator should round-trip");

        System.out.println("SQLite repository self-test passed.");
        System.out.println("Database file: " + databasePath);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(message + " | expected=" + expected + ", actual=" + actual);
        }
    }
}
