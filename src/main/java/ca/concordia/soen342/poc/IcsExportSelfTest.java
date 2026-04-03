package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.ical.CalendarGateway;
import ca.concordia.soen342.poc.ical.IcsCalendarGateway;
import ca.concordia.soen342.poc.ical.TaskIcsExportService;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import ca.concordia.soen342.poc.search.SearchCriteria;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class IcsExportSelfTest {

    public static void main(String[] args) throws Exception {
        Path databasePath = Files.createTempFile("ics-selftest-", ".db");
        TaskRepository repo = new SqliteTaskRepository(databasePath);

        Project project = new Project(10, "Capstone", "Capstone project");

        Task withDueDate = new Task(
                100,
                "Submit report",
                "Final report",
                LocalDateTime.now(),
                PriorityLevel.HIGH,
                Status.OPEN,
                LocalDate.now().plusDays(1));
        withDueDate.setProject(project);
        withDueDate.addSubtask(new Subtask(1, "Draft intro", "OPEN"));

        Task withoutDueDate = new Task(
                101,
                "Brainstorm ideas",
                "Not exportable",
                LocalDateTime.now(),
                PriorityLevel.LOW,
                Status.OPEN,
                null);
        withoutDueDate.setProject(project);

        repo.save(withDueDate);
        repo.save(withoutDueDate);

        CalendarGateway gateway = new IcsCalendarGateway();
        TaskIcsExportService service = new TaskIcsExportService(repo, gateway);

        Path singleTaskFile = Path.of("samples", "selftest-single.ics");
        Path projectFile = Path.of("samples", "selftest-project.ics");
        Path filteredFile = Path.of("samples", "selftest-filtered.ics");

        service.exportSingleTask(100, singleTaskFile.toString());
        service.exportProjectTasks(10, projectFile.toString());

        SearchCriteria criteria = new SearchCriteria();
        criteria.projectName = "Capstone";
        service.exportFilteredTasks(criteria, filteredFile.toString());

        String singleContent = Files.readString(singleTaskFile);
        String projectContent = Files.readString(projectFile);
        String filteredContent = Files.readString(filteredFile);

        assertTrue(singleContent.contains("SUMMARY:Submit report"), "single export should contain task title");
        assertTrue(singleContent.contains("Project: Capstone"), "single export should contain project name");
        assertTrue(singleContent.contains("Subtasks:"), "single export should include subtask summary");

        assertTrue(projectContent.contains("SUMMARY:Submit report"), "project export should include eligible task");
        assertTrue(!projectContent.contains("Brainstorm ideas"), "project export should ignore task without due date");

        assertTrue(filteredContent.contains("SUMMARY:Submit report"), "filtered export should contain matching task");
        assertTrue(!filteredContent.contains("Brainstorm ideas"),
                "filtered export should ignore task without due date");

        System.out.println("All iCal self-tests passed.");
        System.out.println("Database file: " + databasePath);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
