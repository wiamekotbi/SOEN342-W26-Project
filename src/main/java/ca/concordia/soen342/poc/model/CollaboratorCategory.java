package ca.concordia.soen342.poc.model;

public enum CollaboratorCategory {
    SENIOR(2),
    INTERMEDIATE(5),
    JUNIOR(10);

    private final int openTaskLimit;

    CollaboratorCategory(int openTaskLimit) {
        if (openTaskLimit <= 0) {
            throw new IllegalArgumentException("openTaskLimit must be positive");
        }
        this.openTaskLimit = openTaskLimit;
    }

    public int getOpenTaskLimit() {
        return openTaskLimit;
    }
}