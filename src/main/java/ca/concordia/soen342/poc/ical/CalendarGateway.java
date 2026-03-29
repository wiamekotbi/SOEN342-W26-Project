package ca.concordia.soen342.poc.ical;

import ca.concordia.soen342.poc.model.Task;
import java.io.IOException;
import java.util.List;

public interface CalendarGateway {
    void exportTasks(List<Task> tasks, String filePath) throws IOException;
}