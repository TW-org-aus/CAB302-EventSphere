package com.eventsphere.app.dao;

import com.eventsphere.app.model.Comment;

import java.util.List;

/** DAO for user comments on events, including threaded replies. */
public interface ICommentDAO {

    /** Returns the generated CommentID; replyToCommentId may be null for a top-level comment. */
    int insert(int userId, int eventId, String content, Integer replyToCommentId);

    /** Returns all comments on the event oldest-first, including comments by deactivated users. */
    List<Comment> findByEvent(int eventId);

    /** Saves the comment's content and stamps UpdatedAt with the current DB time. */
    void update(Comment comment);

    void delete(int commentId);
}
