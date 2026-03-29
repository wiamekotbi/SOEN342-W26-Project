package ca.concordia.soen342.poc.ical;

import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.TaskRepository;
import ca.concordia.soen342.poc.search.SearchCriteria;
import ca.concordia.soen342.poc.search.TaskSearchService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskIcsExportService {

    private final TaskRepository repo;
    private final TaskSearchService searchService;
    private final CalendarGateway calendarGateway;

    public TaskIcsExportService(TaskRepository repo, CalendarGateway calendarGateway) {
        this.repo = repo;
        this.searchService = new TaskSearchService(repo);
        this.calendarGateway = calendarGateway;
    }

    public void exportSingleTask(int taskId, String filePath) throws IOException {
        Optional<Task> maybeTask = repo.findById(taskId);
        if (maybeTask.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }

        List<Task> tasks = new ArrayList<>();
        tasks.add(maybeTask.get());
        calendarGateway.exportTasks(tasks, filePath);
    }

    public void exportProjectTasks(int projectId, String filePath) throws IOException {
        List<Task> projectTasks = new ArrayList<>();

        for (Task task : repo.findAll()) {
            Project project = task.getProject();
            if (project != null && project.getProjectId() == projectId) {
                projectTasks.add(task);
            }
        }

        calendarGateway.exportTasks(projectTasks, filePath);
    }

    public void exportFilteredTasks(SearchCriteria criteria, String filePath) throws IOException {
        List<Task> tasks = searchService.search(criteria);
        calendarGateway.exportTasks(tasks, filePath);
    }

    public void exportView(SearchCriteria criteria, String filePath) throws IOException {
        List<Task> tasks = searchService.view(criteria);
        calendarGateway.exportTasks(tasks, filePath);
    }
}