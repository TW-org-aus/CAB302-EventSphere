package com.eventsphere.app.dao;

import com.eventsphere.app.model.Event;
import java.util.List;

/** DAO for event likes; countForEvent reads a trigger-maintained denormalized column. */
public interface ILikeDAO {

    /** Returns true if a like was added; false if the user already liked this event. */
    boolean like(int userId, int eventId);

    boolean unlike(int userId, int eventId);

    boolean isLikedBy(int userId, int eventId);

    /** Reads the trigger-maintained Events.LikesCount; avoids a COUNT(*) at query time. */
    int countForEvent(int eventId);

    /** Returns events the user has liked, ordered by StartTime ascending. */
    List<Event> findEventsLikedByUser(int userId);
}
