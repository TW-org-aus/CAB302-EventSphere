package com.eventsphere.app.model;

import java.util.*;

// this object contains the details for the 2 tables of city and catagories that the user prefferences
public class Preference {

    private final int userId;
    private String city;
    private final EnumSet<Category> categories = EnumSet.noneOf(Category.class);

    public Preference(int userId, String city, Collection<Category> categories) {
        this.userId = userId;
        this.city = city;
        setCategories(categories);
    }

    public int getUserId() { return userId; }
    public String getCity() { return city; }


    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(EnumSet.copyOf(categories));
    }

    public void setCity(String city) { this.city = city; }


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
