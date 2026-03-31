package ca.concordia.soen342.poc.repository;

import ca.concordia.soen342.poc.model.ActivityEntry;
import ca.concordia.soen342.poc.model.Collaborator;
import ca.concordia.soen342.poc.model.CollaboratorCategory;
import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Subtask;
import ca.concordia.soen342.poc.model.Tag;
import ca.concordia.soen342.poc.model.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTaskRepository implements TaskRepository {
    private final Path databasePath;
    private final String jdbcUrl;

    public SqliteTaskRepository(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + this.databasePath.toString();
        ensureParentDirectoryExists();
        initializeSchema();
    }

    @Override
    public void save(Task task) {
        String upsertTaskSql = """
            INSERT INTO tasks (
                task_id, title, description, creation_date, priority, status,
                due_date, project_id, project_name, project_description
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(task_id) DO UPDATE SET
                title = excluded.title,
                description = excluded.description,
                creation_date = excluded.creation_date,
                priority = excluded.priority,
                status = excluded.status,
                due_date = excluded.due_date,
                project_id = excluded.project_id,
                project_name = excluded.project_name,
                project_description = excluded.project_description
            """;

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                saveTaskRow(connection, upsertTaskSql, task);
                replaceSubtasks(connection, task);
                replaceTags(connection, task);
                replaceActivityEntries(connection, task);
                replaceCollaborators(connection, task);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save task " + task.getTaskId() + " to SQLite", exception);
        }
    }

    @Override
    public List<Task> findAll() {
        String sql = """
            SELECT task_id, title, description, creation_date, priority, status,
                   due_date, project_id, project_name, project_description
            FROM tasks
            ORDER BY task_id
            """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Task> tasks = new ArrayList<>();
            while (resultSet.next()) {
                tasks.add(mapTask(connection, resultSet));
            }
            return tasks;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load tasks from SQLite", exception);
        }
    }

    @Override
    public Optional<Task> findById(int taskId) {
        String sql = """
            SELECT task_id, title, description, creation_date, priority, status,
                   due_date, project_id, project_name, project_description
            FROM tasks
            WHERE task_id = ?
            """;

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapTask(connection, resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load task " + taskId + " from SQLite", exception);
        }
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    private void ensureParentDirectoryExists() {
        Path parent = databasePath.getParent();
        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create database directory for " + databasePath, exception);
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private void initializeSchema() {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    task_id INTEGER PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT,
                    creation_date TEXT NOT NULL,
                    priority TEXT NOT NULL,
                    status TEXT NOT NULL,
                    due_date TEXT,
                    project_id INTEGER,
                    project_name TEXT,
                    project_description TEXT
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS subtasks (
                    task_id INTEGER NOT NULL,
                    subtask_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    completion_status TEXT NOT NULL,
                    PRIMARY KEY (task_id, subtask_id),
                    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tags (
                    task_id INTEGER NOT NULL,
                    tag_id INTEGER NOT NULL,
                    keyword TEXT NOT NULL,
                    PRIMARY KEY (task_id, tag_id),
                    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS activity_entries (
                    task_id INTEGER NOT NULL,
                    activity_entry_id INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    description TEXT NOT NULL,
                    PRIMARY KEY (task_id, activity_entry_id),
                    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
                )
                """);

            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS task_collaborators (
                    task_id INTEGER NOT NULL,
                    collaborator_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    PRIMARY KEY (task_id, collaborator_id),
                    FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE
                )
                """);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize SQLite schema", exception);
        }
    }

    private void saveTaskRow(Connection connection, String sql, Task task) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, task.getTaskId());
            statement.setString(2, task.getTitle());
            statement.setString(3, task.getDescription());
            statement.setString(4, task.getCreationDate().toString());
            statement.setString(5, task.getPriority().name());
            statement.setString(6, task.getStatus().name());
            statement.setString(7, task.getDueDate() == null ? null : task.getDueDate().toString());

            Project project = task.getProject();
            if (project == null) {
                statement.setObject(8, null);
                statement.setString(9, null);
                statement.setString(10, null);
            } else {
                statement.setInt(8, project.getProjectId());
                statement.setString(9, project.getName());
                statement.setString(10, project.getDescription());
            }

            statement.executeUpdate();
        }
    }

    private void replaceSubtasks(Connection connection, Task task) throws SQLException {
        deleteChildren(connection, "DELETE FROM subtasks WHERE task_id = ?", task.getTaskId());

        String insertSql = "INSERT INTO subtasks (task_id, subtask_id, title, completion_status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Subtask subtask : task.getSubtasks()) {
                statement.setInt(1, task.getTaskId());
                statement.setInt(2, subtask.getSubtaskId());
                statement.setString(3, subtask.getTitle());
                statement.setString(4, subtask.getCompletionStatus());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceTags(Connection connection, Task task) throws SQLException {
        deleteChildren(connection, "DELETE FROM tags WHERE task_id = ?", task.getTaskId());

        String insertSql = "INSERT INTO tags (task_id, tag_id, keyword) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Tag tag : task.getTags()) {
                statement.setInt(1, task.getTaskId());
                statement.setInt(2, tag.getTagId());
                statement.setString(3, tag.getKeyword());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceActivityEntries(Connection connection, Task task) throws SQLException {
        deleteChildren(connection, "DELETE FROM activity_entries WHERE task_id = ?", task.getTaskId());

        String insertSql = """
            INSERT INTO activity_entries (task_id, activity_entry_id, timestamp, description)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (ActivityEntry entry : task.getActivityEntries()) {
                statement.setInt(1, task.getTaskId());
                statement.setInt(2, entry.getActivityEntryId());
                statement.setString(3, entry.getTimestamp().toString());
                statement.setString(4, entry.getDescription());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceCollaborators(Connection connection, Task task) throws SQLException {
        deleteChildren(connection, "DELETE FROM task_collaborators WHERE task_id = ?", task.getTaskId());

        String insertSql = """
            INSERT INTO task_collaborators (task_id, collaborator_id, name, category)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Collaborator collaborator : task.getCollaborators()) {
                statement.setInt(1, task.getTaskId());
                statement.setInt(2, collaborator.getCollaboratorId());
                statement.setString(3, collaborator.getName());
                statement.setString(4, collaborator.getCategory().name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteChildren(Connection connection, String sql, int taskId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            statement.executeUpdate();
        }
    }

    private Task mapTask(Connection connection, ResultSet resultSet) throws SQLException {
        Task task = new Task(
            resultSet.getInt("task_id"),
            resultSet.getString("title"),
            resultSet.getString("description"),
            LocalDateTime.parse(resultSet.getString("creation_date")),
            PriorityLevel.valueOf(resultSet.getString("priority")),
            Status.valueOf(resultSet.getString("status")),
            parseLocalDate(resultSet.getString("due_date"))
        );

        String projectName = resultSet.getString("project_name");
        if (projectName != null && !projectName.isBlank()) {
            Project project = new Project(
                resultSet.getInt("project_id"),
                projectName,
                resultSet.getString("project_description")
            );
            task.setProject(project);
        }

        loadSubtasks(connection, task);
        loadTags(connection, task);
        loadActivityEntries(connection, task);
        loadCollaborators(connection, task);
        return task;
    }

    private void loadSubtasks(Connection connection, Task task) throws SQLException {
        String sql = """
            SELECT subtask_id, title, completion_status
            FROM subtasks
            WHERE task_id = ?
            ORDER BY subtask_id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, task.getTaskId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    task.addSubtask(new Subtask(
                        resultSet.getInt("subtask_id"),
                        resultSet.getString("title"),
                        resultSet.getString("completion_status")
                    ));
                }
            }
        }
    }

    private void loadTags(Connection connection, Task task) throws SQLException {
        String sql = """
            SELECT tag_id, keyword
            FROM tags
            WHERE task_id = ?
            ORDER BY tag_id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, task.getTaskId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    task.addTag(new Tag(
                        resultSet.getInt("tag_id"),
                        resultSet.getString("keyword")
                    ));
                }
            }
        }
    }

    private void loadActivityEntries(Connection connection, Task task) throws SQLException {
        String sql = """
            SELECT activity_entry_id, timestamp, description
            FROM activity_entries
            WHERE task_id = ?
            ORDER BY activity_entry_id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, task.getTaskId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    task.addActivityEntry(new ActivityEntry(
                        resultSet.getInt("activity_entry_id"),
                        LocalDateTime.parse(resultSet.getString("timestamp")),
                        resultSet.getString("description")
                    ));
                }
            }
        }
    }

    private void loadCollaborators(Connection connection, Task task) throws SQLException {
        String sql = """
            SELECT collaborator_id, name, category
            FROM task_collaborators
            WHERE task_id = ?
            ORDER BY collaborator_id
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, task.getTaskId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    task.addCollaborator(new Collaborator(
                        resultSet.getInt("collaborator_id"),
                        resultSet.getString("name"),
                        CollaboratorCategory.valueOf(resultSet.getString("category"))
                    ));
                }
            }
        }
    }

    private LocalDate parseLocalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }
}
