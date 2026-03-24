package ca.concordia.soen342.poc;

import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Project;
import ca.concordia.soen342.poc.model.Status;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.InMemoryTaskRepository;
import ca.concordia.soen342.poc.search.SearchCriteria;
import ca.concordia.soen342.poc.search.TaskSearchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskSearchViewSelfTest {

    public static void main(String[] args) {
        InMemoryTaskRepository repo = new InMemoryTaskRepository();

        Project school = new Project();
        school.setName("School");

        Task t1 = new Task(1, "Buy milk", "groceries", LocalDateTime.now(),
                PriorityLevel.LOW, Status.OPEN, LocalDate.of(2026, 3, 25));

        Task t2 = new Task(2, "SOEN342 report", "iteration 2", LocalDateTime.now(),
                PriorityLevel.HIGH, Status.OPEN, LocalDate.of(2026, 3, 24));
        t2.setProject(school);

        Task t3 = new Task(3, "Gym", "leg day", LocalDateTime.now(),
                PriorityLevel.MEDIUM, Status.COMPLETED, LocalDate.of(2026, 3, 27));

        repo.save(t1);
        repo.save(t2);
        repo.save(t3);

        TaskSearchService service = new TaskSearchService(repo);

        SearchCriteria empty = new SearchCriteria();
        List<Task> view = service.view(empty);
        assertTrue(view.size() == 2, "Expected 2 open tasks");
        assertTrue(view.get(0).getTaskId() == 2, "Expected earliest due date first (taskId=2)");

        SearchCriteria kw = new SearchCriteria();
        kw.keyword = "milk";
        List<Task> kwRes = service.search(kw);
        assertTrue(kwRes.size() == 1, "Expected 1 match for keyword milk");
        assertTrue(kwRes.get(0).getTaskId() == 1, "Expected taskId=1 for milk");

        SearchCriteria st = new SearchCriteria();
        st.status = Status.COMPLETED;
        List<Task> stRes = service.search(st);
        assertTrue(stRes.size() == 1, "Expected 1 completed task");
        assertTrue(stRes.get(0).getTaskId() == 3, "Expected taskId=3 completed");

        SearchCriteria pr = new SearchCriteria();
        pr.projectName = "school";
        List<Task> prRes = service.search(pr);
        assertTrue(prRes.size() == 1, "Expected 1 task in project School");
        assertTrue(prRes.get(0).getTaskId() == 2, "Expected taskId=2 in School project");

        System.out.println("Task search/view self-test passed.");
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond)
            throw new RuntimeException("FAIL: " + msg);
    }
}