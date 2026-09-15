package com.eventsphere.app.dao;

import com.eventsphere.app.model.Comment;

import java.util.List;

public interface ICommentDAO {

    // Returns the generated CommentID. replyToCommentId may be null for a parent comment.
    int insert(int userId, int eventId, String content, Integer replyToCommentId);

    // All comments on the event, oldest first. NOTE!!! --> Includes comments by deactivated users.
    List<Comment> findByEvent(int eventId);

    // Saves the content and stamps UpdatedAt with DB time.
    void update(Comment comment);

    void delete(int commentId);
}
