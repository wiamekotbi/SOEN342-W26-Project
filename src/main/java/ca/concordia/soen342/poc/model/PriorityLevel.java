package ca.concordia.soen342.poc.model;

public enum PriorityLevel {
    LOW,
    MEDIUM,
    HIGH;

    public static PriorityLevel fromCsvValue(String value) {
        return PriorityLevel.valueOf(value.trim().toUpperCase());
    }
}
