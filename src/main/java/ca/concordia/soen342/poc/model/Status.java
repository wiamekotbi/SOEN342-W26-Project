package ca.concordia.soen342.poc.model;

public enum Status {
    OPEN,
    COMPLETED,
    CANCELLED;

    public static Status fromCsvValue(String value) {
        return Status.valueOf(value.trim().toUpperCase());
    }
}
