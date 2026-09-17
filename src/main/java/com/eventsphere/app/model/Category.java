package com.eventsphere.app.model;

// Fixed Event categories. DBController builds the Category CHECK lists from this enum.
public enum Category {
    MUSIC("Music"),
    SPORTS("Sports"),
    ARTS_THEATRE("Arts & Theatre"),
    FILM("Film"),
    COMEDY("Comedy"),
    FAMILY("Family"),
    COMMUNITY("Community"),
    FOOD_DRINK("Food & Drink"),
    UNI_EVENTS("Uni Events"),
    NIGHTLIFE("Nightlife"),
    FESTIVALS("Festivals"),
    OUTDOORS("Outdoors"),
    FITNESS_WELLNESS("Fitness & Wellness"),
    MARKETS("Markets"),
    CHARITY("Charity"),
    WORKSHOPS("Workshops"),
    TECH("Tech"),
    GAMING("Gaming"),
    OTHER("Other");

    private final String dbValue;

    Category(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() { return dbValue; }

    public static Category fromDbValue(String dbValue) {
        for (Category category : values()) {
            if (category.dbValue.equals(dbValue)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + dbValue);
    }

    // for the UI to show the catagory value as a string
    @Override
    public String toString() {
        return dbValue;
    }
}
