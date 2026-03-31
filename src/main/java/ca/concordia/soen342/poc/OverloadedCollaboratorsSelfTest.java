package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.model.Collaborator;
import ca.concordia.soen342.poc.model.CollaboratorCategory;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadInfo;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadService;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OverloadedCollaboratorsSelfTest {
    public static void main(String[] args) throws Exception {
        TaskRepository repo = new SqliteTaskRepository(Files.createTempFile("overload-selftest-", ".db"));

        Collaborator junior = new Collaborator(1, "Maya", CollaboratorCategory.JUNIOR);
        Collaborator senior = new Collaborator(2, "Omar", CollaboratorCategory.SENIOR);
        Collaborator intermediate = new Collaborator(3, "Sara", CollaboratorCategory.INTERMEDIATE);

        for (int i = 1; i <= 11; i++) {
            Task t = new Task(
                i,
                "Junior Task " + i,
                "Sample task",
                LocalDateTime.now(),
                PriorityLevel.MEDIUM,
                Status.OPEN,
                LocalDate.now().plusDays(i)
            );
            t.addCollaborator(junior);
            repo.save(t);
        }

        for (int i = 20; i <= 21; i++) {
            Task t = new Task(
                i,
                "Senior Task " + i,
                "Sample task",
                LocalDateTime.now(),
                PriorityLevel.HIGH,
                Status.OPEN,
                LocalDate.now().plusDays(i)
            );
            t.addCollaborator(senior);
            repo.save(t);
        }

        for (int i = 30; i <= 34; i++) {
            Task t = new Task(
                i,
                "Intermediate Task " + i,
                "Sample task",
                LocalDateTime.now(),
                PriorityLevel.LOW,
                Status.OPEN,
                LocalDate.now().plusDays(i)
            );
            t.addCollaborator(intermediate);
            repo.save(t);
        }

        CollaboratorOverloadService service = new CollaboratorOverloadService(repo);
        List<CollaboratorOverloadInfo> overloaded = service.getOverloadedCollaborators();

        assertTrue(service.hasValidPositiveLimits(), "Limits should be positive");
        assertTrue(overloaded.size() == 1, "Expected exactly 1 overloaded collaborator");
        assertTrue("Maya".equals(overloaded.get(0).getCollaboratorName()), "Expected Maya to be overloaded");
        assertTrue(overloaded.get(0).getCategory() == CollaboratorCategory.JUNIOR,
            "Expected Maya to be JUNIOR");
        assertTrue(overloaded.get(0).getOpenTaskCount() == 11,
            "Expected Maya to have 11 open tasks");
        assertTrue(overloaded.get(0).getLimit() == 10,
            "Expected JUNIOR limit to be 10");

        System.out.println("All overloaded collaborator self-tests passed.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
