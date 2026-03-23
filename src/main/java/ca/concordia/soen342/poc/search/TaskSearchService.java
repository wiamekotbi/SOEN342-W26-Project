package ca.concordia.soen342.poc.search;

import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.TaskRepository;

import java.util.List;

public class TaskSearchService {

    private final TaskRepository repo;

    public TaskSearchService(TaskRepository repo) {
        this.repo = repo;
    }

    // View entry point (real logic next commit)
    public List<Task> view(SearchCriteria criteria) {
        return repo.findAll();
    }

    // Search entry point (real logic next commit)
    public List<Task> search(SearchCriteria criteria) {
        return repo.findAll();
    }
}