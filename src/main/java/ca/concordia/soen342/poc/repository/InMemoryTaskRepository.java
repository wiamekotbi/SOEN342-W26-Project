package ca.concordia.soen342.poc.repository;

import ca.concordia.soen342.poc.model.Task;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Integer, Task> tasksById = new LinkedHashMap<>();

    @Override
    public void save(Task task) {
        tasksById.put(task.getTaskId(), task);
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>(tasksById.values());
        tasks.sort(Comparator.comparingInt(Task::getTaskId));
        return tasks;
    }

    @Override
    public Optional<Task> findById(int taskId) {
        return Optional.ofNullable(tasksById.get(taskId));
    }
}
