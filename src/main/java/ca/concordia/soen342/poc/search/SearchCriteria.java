package ca.concordia.soen342.poc.search;

import ca.concordia.soen342.poc.model.PriorityLevel;
import ca.concordia.soen342.poc.model.Status;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class SearchCriteria {
    public String keyword;
    public Status status;
    public PriorityLevel priority;
    public LocalDate dueFrom;
    public LocalDate dueTo;
    public String projectName;
    public DayOfWeek dayOfWeek;

    public boolean isEmpty() {
        return (keyword == null || keyword.isBlank())
                && status == null
                && priority == null
                && dueFrom == null
                && dueTo == null
                && (projectName == null || projectName.isBlank())
                && dayOfWeek == null;
    }
}