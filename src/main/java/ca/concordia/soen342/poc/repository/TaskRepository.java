package ca.concordia.soen342.poc.repository;

import ca.concordia.soen342.poc.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    void save(Task task);

    List<Task> findAll();

    Optional<Task> findById(int taskId);
}
