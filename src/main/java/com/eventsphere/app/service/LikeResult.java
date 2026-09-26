package com.eventsphere.app.service;

/**
 * Outcome of a like/unlike: whether the user likes the event now, and the event's
 * total like count after the change (read from the trigger-maintained Events.LikesCount).
 */
public record LikeResult(boolean liked, int count) { }
