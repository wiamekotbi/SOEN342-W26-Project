package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.ical.CalendarGateway;
import ca.concordia.soen342.poc.ical.IcsCalendarGateway;
import ca.concordia.soen342.poc.ical.TaskIcsExportService;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.InMemoryTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import ca.concordia.soen342.poc.search.SearchCriteria;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class IcsExportDemo {

    public static void main(String[] args) throws Exception {
        TaskRepository repo = new InMemoryTaskRepository();

        Project school = new Project(1, "School", "School tasks");
        Project work = new Project(2, "Work", "Work tasks");

        Task t1 = new Task(
                1,
                "Finish assignment",
                "Complete iteration 3",
                LocalDateTime.now(),
                PriorityLevel.HIGH,
                Status.OPEN,
                LocalDate.now().plusDays(2));
        t1.setProject(school);
        t1.addSubtask(new Subtask(1, "Write code", "OPEN"));
        t1.addSubtask(new Subtask(2, "Run tests", "OPEN"));

        Task t2 = new Task(
                2,
                "Prepare meeting",
                "Team sync",
                LocalDateTime.now(),
                PriorityLevel.MEDIUM,
                Status.OPEN,
                LocalDate.now().plusDays(4));
        t2.setProject(work);

        Task t3 = new Task(
                3,
                "Read notes",
                "No due date task",
                LocalDateTime.now(),
                PriorityLevel.LOW,
                Status.OPEN,
                null);
        t3.setProject(school);

        repo.save(t1);
        repo.save(t2);
        repo.save(t3);

        CalendarGateway gateway = new IcsCalendarGateway();
        TaskIcsExportService exportService = new TaskIcsExportService(repo, gateway);

        exportService.exportSingleTask(1, "samples/task-1.ics");
        exportService.exportProjectTasks(1, "samples/project-1.ics");

        SearchCriteria criteria = new SearchCriteria();
        criteria.status = Status.OPEN;

        exportService.exportFilteredTasks(criteria, "samples/filtered.ics");
    }
}