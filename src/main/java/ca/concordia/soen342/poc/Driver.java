package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.csv.TaskCsvExporter;
import ca.concordia.soen342.poc.csv.TaskCsvImporter;
import ca.concordia.soen342.poc.ical.CalendarGateway;
import ca.concordia.soen342.poc.ical.IcsCalendarGateway;
import ca.concordia.soen342.poc.ical.TaskIcsExportService;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Tag;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadInfo;
import ca.concordia.soen342.poc.overload.CollaboratorOverloadService;
import ca.concordia.soen342.poc.repository.RepositoryPaths;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import ca.concordia.soen342.poc.search.SearchCriteria;
import ca.concordia.soen342.poc.search.TaskSearchService;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Driver {
    private static final String DIVIDER = "============================================================";

    private final Scanner scanner;
    private final TaskRepository repo;
    private final TaskSearchService searchService;
    private final TaskCsvImporter csvImporter;
    private final TaskCsvExporter csvExporter;
    private final TaskIcsExportService icsExportService;
    private final CollaboratorOverloadService overloadService;

    private List<Task> lastResults;
    private SearchCriteria lastCriteria;

    public Driver() {
        this.scanner = new Scanner(System.in);
        this.repo = new SqliteTaskRepository(RepositoryPaths.defaultDatabasePath());
        this.searchService = new TaskSearchService(repo);
        this.csvImporter = new TaskCsvImporter();
        this.csvExporter = new TaskCsvExporter();

        CalendarGateway gateway = new IcsCalendarGateway();
        this.icsExportService = new TaskIcsExportService(repo, gateway);
        this.overloadService = new CollaboratorOverloadService(repo);

        this.lastResults = new ArrayList<>();
        this.lastCriteria = null;
    }

    public static void main(String[] args) {
        new Driver().run();
    }

    private void run() {
        printBanner("SOEN 342 Task Management System");

        boolean running = true;
        while (running) {
            printMenu(
                "Main Menu",
                "1. Import tasks from CSV",
                "2. Search and view tasks",
                "3. Export tasks to CSV",
                "4. Export tasks to iCal",
                "5. List overloaded collaborators",
                "6. View all tasks",
                "7. Exit"
            );

            int choice = readInt("Choose an option (1-7): ");
            switch (choice) {
                case 1:
                    importTasksFromCsv();
                    break;
                case 2:
                    searchAndViewTasks();
                    break;
                case 3:
                    exportTasksToCsv();
                    break;
                case 4:
                    exportTasksToIcal();
                    break;
                case 5:
                    listOverloadedCollaborators();
                    break;
                case 6:
                    viewAllTasks();
                    break;
                case 7:
                    running = false;
                    printMessage("Exiting application.");
                    break;
                default:
                    printMessage("Invalid option.");
            }
        }

        scanner.close();
    }

    private void importTasksFromCsv() {
        printSection("Import Tasks from CSV");
        String pathInput = readLine("Enter CSV file path: ");
        Path csvPath = Path.of(pathInput);

        try {
            List<Task> imported = csvImporter.importFrom(csvPath, repo);
            printMessage("Import complete. " + imported.size() + " task(s) imported.");
            printTaskList("Imported Tasks", imported, "No tasks were imported.");
        } catch (Exception e) {
            printMessage("Import failed: " + e.getMessage());
        }
    }

    private void searchAndViewTasks() {
        printMenu(
            "Search and View Options",
            "1. View default open tasks",
            "2. Search by keyword",
            "3. Search by status",
            "4. Search by priority",
            "5. Search by project name",
            "6. Search by due date range",
            "7. Search by day of week"
        );

        int choice = readInt("Choose an option (1-7): ");
        SearchCriteria criteria = new SearchCriteria();
        List<Task> results;

        switch (choice) {
            case 1:
                criteria = new SearchCriteria();
                results = searchService.view(criteria);
                lastCriteria = criteria;
                lastResults = results;
                printTaskList("Open Tasks", results, "No open tasks found.");
                return;

            case 2:
                criteria.keyword = readLine("Enter keyword: ");
                break;

            case 3:
                criteria.status = readStatus();
                if (criteria.status == null) {
                    printMessage("Invalid status.");
                    return;
                }
                break;

            case 4:
                criteria.priority = readPriority();
                if (criteria.priority == null) {
                    printMessage("Invalid priority.");
                    return;
                }
                break;

            case 5:
                criteria.projectName = readLine("Enter project name: ");
                break;

            case 6:
                criteria.dueFrom = readDate("Enter start date (yyyy-MM-dd): ");
                criteria.dueTo = readDate("Enter end date (yyyy-MM-dd): ");
                if (criteria.dueFrom == null || criteria.dueTo == null) {
                    printMessage("Invalid date.");
                    return;
                }
                break;

            case 7:
                criteria.dayOfWeek = readDayOfWeek();
                if (criteria.dayOfWeek == null) {
                    printMessage("Invalid day of week.");
                    return;
                }
                break;

            default:
                printMessage("Invalid option.");
                return;
        }

        results = searchService.search(criteria);
        lastCriteria = criteria;
        lastResults = results;
        printTaskList("Search Results", results, "No matching tasks found.");
    }

    private void exportTasksToCsv() {
        printSection("Export Tasks to CSV");

        List<Task> tasksToExport = chooseTasksForExport();
        if (tasksToExport == null) {
            return;
        }

        String outputPath = readLine("Enter output CSV file path: ");
        if (outputPath.isBlank()) {
            printMessage("Output path cannot be empty.");
            return;
        }

        try {
            csvExporter.exportTo(Path.of(outputPath), tasksToExport);
            printMessage("CSV export complete: " + outputPath);
        } catch (IOException e) {
            printMessage("CSV export failed: " + e.getMessage());
        }
    }

    private void exportTasksToIcal() {
        printMenu(
            "iCal Export Options",
            "1. Export a single task",
            "2. Export all tasks in a project",
            "3. Export filtered tasks from last search"
        );

        int choice = readInt("Choose an option (1-3): ");
        String outputPath = readLine("Enter output .ics file path: ");
        if (outputPath.isBlank()) {
            printMessage("Output path cannot be empty.");
            return;
        }

        try {
            switch (choice) {
                case 1:
                    int taskId = readInt("Enter task id: ");
                    icsExportService.exportSingleTask(taskId, outputPath);
                    printMessage("iCal export complete: " + outputPath);
                    break;

                case 2:
                    int projectId = readInt("Enter project id: ");
                    icsExportService.exportProjectTasks(projectId, outputPath);
                    printMessage("iCal export complete: " + outputPath);
                    break;

                case 3:
                    if (lastCriteria == null) {
                        printMessage("No previous search criteria found. Run a search first.");
                        return;
                    }
                    icsExportService.exportFilteredTasks(lastCriteria, outputPath);
                    printMessage("iCal export complete: " + outputPath);
                    break;

                default:
                    printMessage("Invalid option.");
            }
        } catch (Exception e) {
            printMessage("iCal export failed: " + e.getMessage());
        }
    }

    private void listOverloadedCollaborators() {
        printSection("Overloaded Collaborators");

        if (!overloadService.hasValidPositiveLimits()) {
            printMessage("Collaborator category limits are invalid.");
            return;
        }

        List<CollaboratorOverloadInfo> overloaded = overloadService.getOverloadedCollaborators();
        if (overloaded.isEmpty()) {
            System.out.println("No overloaded collaborators found.");
            return;
        }

        for (int i = 0; i < overloaded.size(); i++) {
            CollaboratorOverloadInfo info = overloaded.get(i);
            System.out.println(
                (i + 1) + ". "
                    + info.getCollaboratorName()
                    + " | category: " + info.getCategory()
                    + " | open tasks: " + info.getOpenTaskCount()
                    + " | limit: " + info.getLimit()
            );
        }
    }

    private void viewAllTasks() {
        List<Task> tasks = repo.findAll();
        lastResults = tasks;
        lastCriteria = null;
        printTaskList("All Tasks", tasks, "No tasks found.");
    }

    private List<Task> chooseTasksForExport() {
        printMenu(
            "CSV Export Scope",
            "1. Export all tasks",
            "2. Export tasks from last search",
            "3. Export a single task by id",
            "4. Cancel"
        );

        int choice = readInt("Choose an option (1-4): ");
        switch (choice) {
            case 1:
                List<Task> allTasks = repo.findAll();
                if (allTasks.isEmpty()) {
                    printMessage("No tasks available.");
                    return null;
                }
                return allTasks;

            case 2:
                if (lastResults == null || lastResults.isEmpty()) {
                    printMessage("No previous search/view results found.");
                    return null;
                }
                return new ArrayList<>(lastResults);

            case 3:
                int taskId = readInt("Enter task id: ");
                Optional<Task> maybeTask = repo.findById(taskId);
                if (maybeTask.isEmpty()) {
                    printMessage("Task not found.");
                    return null;
                }
                List<Task> singleTask = new ArrayList<>();
                singleTask.add(maybeTask.get());
                return singleTask;

            case 4:
                printMessage("Export cancelled.");
                return null;

            default:
                printMessage("Invalid option.");
                return null;
        }
    }

    private Status readStatus() {
        String input = readLine("Enter status (OPEN, COMPLETED, CANCELLED): ").trim().toUpperCase();
        try {
            return Status.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PriorityLevel readPriority() {
        String input = readLine("Enter priority (LOW, MEDIUM, HIGH): ").trim().toUpperCase();
        try {
            return PriorityLevel.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate readDate(String prompt) {
        String input = readLine(prompt).trim();
        try {
            return LocalDate.parse(input);
        } catch (Exception e) {
            return null;
        }
    }

    private DayOfWeek readDayOfWeek() {
        String input = readLine("Enter day of week (MONDAY ... SUNDAY): ").trim().toUpperCase();
        try {
            return DayOfWeek.valueOf(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int readInt(String prompt) {
        String input = readLine(prompt).trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void printBanner(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println(centerText(title));
        System.out.println(DIVIDER);
    }

    private void printSection(String title) {
        System.out.println();
        System.out.println("-- " + title + " " + "-".repeat(Math.max(0, 48 - title.length())));
    }

    private void printMenu(String title, String... options) {
        printSection(title);
        for (String option : options) {
            System.out.println("  " + option);
        }
        System.out.println();
    }

    private void printTaskList(String title, List<Task> taskList, String emptyMessage) {
        printSection(title);
        if (taskList == null || taskList.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + ". " + formatTask(taskList.get(i)));
        }
    }

    private String formatTask(Task task) {
        String dueDate = (task.getDueDate() == null) ? "no due date" : task.getDueDate().toString();

        Project project = task.getProject();
        String projectName = (project == null || project.getName() == null || project.getName().isBlank())
            ? "No project"
            : project.getName();

        String tags = joinTagKeywords(task.getTags());
        String subtasks = joinSubtaskTitles(task.getSubtasks());

        return "ID=" + task.getTaskId()
            + " | " + task.getTitle()
            + " | " + task.getStatus()
            + " | " + task.getPriority()
            + " | due " + dueDate
            + " | " + projectName
            + " | tags: " + tags
            + " | subtasks: " + subtasks;
    }

    private String joinTagKeywords(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "-";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(tags.get(i).getKeyword());
        }
        return result.toString();
    }

    private String joinSubtaskTitles(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            return "-";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < subtasks.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(subtasks.get(i).getTitle());
        }
        return result.toString();
    }

    private void printMessage(String message) {
        System.out.println();
        System.out.println("> " + message);
    }

    private String centerText(String text) {
        int padding = Math.max(0, (DIVIDER.length() - text.length()) / 2);
        return " ".repeat(padding) + text;
    }
}