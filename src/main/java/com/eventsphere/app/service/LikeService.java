package com.eventsphere.app.service;

import com.eventsphere.app.dao.ILikeDAO;

/**
 * Likes an event on behalf of a logged-in user. The like button is a toggle, so the UI asks
 * for the current state, flips it, and gets the new state and count back in one call.
 */
public class LikeService {

    private final ILikeDAO likes;

    public LikeService(ILikeDAO likes) {
        this.likes = likes;
    }

    /**
     * State to render for the event. Pass a null userId (nobody logged in) to get
     * liked = false without touching the Likes table; the count still comes from the database.
     */
    public LikeResult stateFor(Integer userId, int eventId) {
        boolean liked = userId != null && likes.isLikedBy(userId, eventId);
        return new LikeResult(liked, likes.countForEvent(eventId));
    }

    /**
     * Like if not liked, unlike if liked, then report the stored state. A duplicate like is
     * impossible: LikeDAO inserts with INSERT OR IGNORE against the UNIQUE (UserID, EventsID)
     * key, so a double click can never bump the count twice.
     */
    public LikeResult toggleLike(int userId, int eventId) {
        if (likes.isLikedBy(userId, eventId)) {
            likes.unlike(userId, eventId);
        } else {
            likes.like(userId, eventId);
        }
        return stateFor(userId, eventId);
    }
}
