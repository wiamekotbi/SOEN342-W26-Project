package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.csv.TaskCsvExporter;
import ca.concordia.soen342.poc.csv.TaskCsvImporter;
import ca.concordia.soen342.poc.repository.RepositoryPaths;
import ca.concordia.soen342.poc.repository.SqliteTaskRepository;
import ca.concordia.soen342.poc.repository.TaskRepository;
import java.io.IOException;
import java.nio.file.Path;

public class CsvImportExportDemo {
    public static void main(String[] args) throws IOException {
        Path inputPath = Path.of("samples", "tasks-import.csv");
        Path outputPath = Path.of("samples", "tasks-export.csv");

        TaskRepository taskRepository = new SqliteTaskRepository(RepositoryPaths.defaultDatabasePath());
        TaskCsvImporter importer = new TaskCsvImporter();
        TaskCsvExporter exporter = new TaskCsvExporter();

        importer.importFrom(inputPath, taskRepository);
        exporter.exportTo(outputPath, taskRepository.findAll());

        System.out.println("Imported " + taskRepository.findAll().size() + " tasks.");
        System.out.println("Exported CSV to " + outputPath.toAbsolutePath());
        System.out.println("Persisted tasks to " + RepositoryPaths.defaultDatabasePath().toAbsolutePath());
    }
}
