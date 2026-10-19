package com.eventsphere.app.model;

/** Fixed event categories; DBController derives the CHECK constraints from this enum's values. */
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

    /** Returns the Category whose database value matches the given string, or throws IllegalArgumentException. */
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
