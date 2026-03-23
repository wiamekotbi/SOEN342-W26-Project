package ca.concordia.soen342.poc.search;

import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.TaskRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskSearchService {

    private final TaskRepository repo;

    public TaskSearchService(TaskRepository repo) {
        this.repo = repo;
    }

    public List<Task> view(SearchCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) {
            List<Task> openTasks = new ArrayList<>();
            for (Task t : repo.findAll()) {
                if (t.getStatus() == Status.OPEN) {
                    openTasks.add(t);
                }
            }
            openTasks.sort(Comparator.comparing(TaskSearchService::safeDueDate));
            return openTasks;
        }
        return search(criteria);
    }

    public List<Task> search(SearchCriteria criteria) {
        List<Task> results = new ArrayList<>();
        for (Task t : repo.findAll()) {
            if (matches(t, criteria)) {
                results.add(t);
            }
        }
        results.sort(Comparator.comparing(TaskSearchService::safeDueDate));
        return results;
    }

    private boolean matches(Task t, SearchCriteria c) {
        if (c == null)
            return true;

        if (c.status != null && t.getStatus() != c.status)
            return false;
        if (c.priority != null && t.getPriority() != c.priority)
            return false;

        if (c.keyword != null && !c.keyword.isBlank()) {
            String k = c.keyword.trim().toLowerCase();
            String title = (t.getTitle() == null) ? "" : t.getTitle().toLowerCase();
            String desc = (t.getDescription() == null) ? "" : t.getDescription().toLowerCase();
            if (!title.contains(k) && !desc.contains(k))
                return false;
        }

        if (c.projectName != null && !c.projectName.isBlank()) {
            Project p = t.getProject();
            String taskProjectName = (p == null || p.getName() == null) ? "" : p.getName();
            if (!taskProjectName.equalsIgnoreCase(c.projectName.trim()))
                return false;
        }

        LocalDate due = t.getDueDate();
        if (c.dueFrom != null) {
            if (due == null || due.isBefore(c.dueFrom))
                return false;
        }
        if (c.dueTo != null) {
            if (due == null || due.isAfter(c.dueTo))
                return false;
        }

        return true;
    }

    private static LocalDate safeDueDate(Task t) {
        LocalDate d = t.getDueDate();
        return (d == null) ? LocalDate.of(9999, 12, 31) : d;
    }
}