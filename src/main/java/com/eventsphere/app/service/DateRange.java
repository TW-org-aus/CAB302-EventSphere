package com.eventsphere.app.service;

// Date options for the landing page filter bar. toString() is the label the ComboBox shows.
public enum DateRange {
    ANY("Any time"),
    TODAY("Today"),
    THIS_WEEKEND("This weekend"),
    NEXT_7_DAYS("Next 7 days"),
    NEXT_30_DAYS("Next 30 days");

    private final String label;

    DateRange(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
