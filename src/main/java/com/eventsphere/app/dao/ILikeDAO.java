package com.eventsphere.app.dao;

public interface ILikeDAO {

    // true if a like was added, false if the user already liked this event.
    boolean like(int userId, int eventId);

    boolean unlike(int userId, int eventId);

    boolean isLikedBy(int userId, int eventId);

    // Reads the trigger-maintained Events.LikesCount.
    int countForEvent(int eventId);
}
