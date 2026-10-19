package com.eventsphere.app.model;

import java.util.*;

/** User preference record, aggregating home address, bio, and chosen event categories. */
public class Preference {

    private final int userId;
    private String address;
    private String bio;
    private final EnumSet<Category> categories = EnumSet.noneOf(Category.class);

    /** Creates a Preference record from database values; categories may be empty but not null. */
    public Preference(int userId, String address, String bio, Collection<Category> categories) {
        this.userId = userId;
        this.address = address;
        this.bio = bio;
        setCategories(categories);
    }

    public int getUserId() { return userId; }
    public String getAddress() { return address; }
    public String getBio() { return bio; }


    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(EnumSet.copyOf(categories));
    }

    public void setAddress(String address) { this.address = address; }
    public void setBio(String bio) { this.bio = bio; }


    public void setCategories(Collection<Category> categories) {
        Objects.requireNonNull(categories, "categories");
        this.categories.clear();
        this.categories.addAll(categories);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Preference preference && userId == preference.userId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userId);
    }
}
