package com.eventsphere.app.service;

/** Date filter options for the landing page; toString() returns the label shown in the ComboBox. */
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
