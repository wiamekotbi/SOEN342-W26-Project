package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.model.Collaborator;
import ca.concordia.soen342.poc.model.CollaboratorCategory;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadInfo;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadService;
import ca.concordia.soen342.poc.repository.RepositoryPaths;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class OverloadedCollaboratorsDemo {
    public static void main(String[] args) {
        TaskRepository repo = new SqliteTaskRepository(RepositoryPaths.defaultDatabasePath());
        seedSampleData(repo);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Overloaded Collaborators Menu ===");
        System.out.println("1. List overloaded collaborators");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            listOverloadedCollaborators(repo);
        } else {
            System.out.println("Invalid option.");
        }

        System.out.println("Persisted tasks to " + RepositoryPaths.defaultDatabasePath().toAbsolutePath());
    }

    private static void listOverloadedCollaborators(TaskRepository repo) {
        CollaboratorOverloadService overloadService = new CollaboratorOverloadService(repo);

        if (!overloadService.hasValidPositiveLimits()) {
            System.out.println("Error: collaborator category limits must be positive integers.");
            return;
        }

        List<CollaboratorOverloadInfo> overloaded = overloadService.getOverloadedCollaborators();

        if (overloaded.isEmpty()) {
            System.out.println("No overloaded collaborators found.");
            return;
        }

        System.out.println("Overloaded collaborators:");
        for (CollaboratorOverloadInfo info : overloaded) {
            System.out.println("- " + info);
        }
    }

    private static void seedSampleData(TaskRepository repo) {
        Collaborator junior = new Collaborator(1, "Maya", CollaboratorCategory.JUNIOR);
        Collaborator senior = new Collaborator(2, "Omar", CollaboratorCategory.SENIOR);

        for (int i = 1; i <= 11; i++) {
            Task t = new Task(
                i,
                "Task " + i,
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
    }
}
