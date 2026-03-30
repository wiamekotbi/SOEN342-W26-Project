package ca.concordia.soen342.poc.overload;

import ca.concordia.soen342.poc.model.CollaboratorCategory;

public class CollaboratorOverloadInfo {
    private final int collaboratorId;
    private final String collaboratorName;
    private final CollaboratorCategory category;
    private int openTaskCount;

    public CollaboratorOverloadInfo(
        int collaboratorId,
        String collaboratorName,
        CollaboratorCategory category,
        int openTaskCount
    ) {
        this.collaboratorId = collaboratorId;
        this.collaboratorName = collaboratorName;
        this.category = category;
        this.openTaskCount = openTaskCount;
    }

    public int getCollaboratorId() {
        return collaboratorId;
    }

    public String getCollaboratorName() {
        return collaboratorName;
    }

    public CollaboratorCategory getCategory() {
        return category;
    }

    public int getOpenTaskCount() {
        return openTaskCount;
    }

    public int getLimit() {
        return category.getOpenTaskLimit();
    }

    public boolean isOverloaded() {
        return openTaskCount > getLimit();
    }

    public void incrementOpenTaskCount() {
        openTaskCount++;
    }

    @Override
    public String toString() {
        return "ID: " + collaboratorId
            + ", Name: " + collaboratorName
            + ", Category: " + category
            + ", Open Tasks: " + openTaskCount
            + ", Limit: " + getLimit();
    }
}