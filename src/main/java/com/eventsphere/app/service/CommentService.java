package com.eventsphere.app.service;

import com.eventsphere.app.dao.ICommentDAO;
import com.eventsphere.app.dao.IUserDAO;
import com.eventsphere.app.model.Comment;
import com.eventsphere.app.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comments and replies on an event page.
 *
 * Reply rules (business rules, not database constraints):
 *  - a reply must point at a top-level comment on the same event,
 *  - a reply cannot itself be replied to,
 *  - a comment can have at most ONE reply.
 *
 * {@link com.eventsphere.app.dao.CommentDAO#insert} stays a dumb row writer; the rules live
 * here so every caller (currently EventPageController) gets them.
 */
public class CommentService {

    // Thrown as IllegalArgumentException messages, so the UI can show why a post was refused.
    static final String EMPTY_CONTENT = "Comment cannot be empty.";
    static final String REPLY_TARGET_MISSING = "That comment no longer exists.";
    static final String REPLY_TO_REPLY = "Replies cannot be replied to.";
    static final String REPLY_ALREADY_EXISTS = "This comment already has a reply.";

    private final ICommentDAO comments;
    private final IUserDAO users;

    public CommentService(ICommentDAO comments, IUserDAO users) {
        this.comments = comments;
        this.users = users;
    }

    /** All comments on the event, oldest first (the UI groups them into threads). */
    public List<Comment> commentsForEvent(int eventId) {
        return comments.findByEvent(eventId);
    }

    /**
     * Display names for the authors of the given comments, keyed by UserID, for the UI to
     * render. Ids with no matching user row are left out, and the UI falls back to
     * "User #&lt;id&gt;". Deactivated users are still resolved so old comments keep their author.
     */
    public Map<Integer, String> authorNamesFor(List<Comment> commentsToRender) {
        Map<Integer, String> names = new HashMap<>();
        for (Comment comment : commentsToRender) {
            int userId = comment.getUserId();
            if (names.containsKey(userId)) {
                continue;
            }
            users.findById(userId).map(User::getFullName)
                    .ifPresent(fullName -> names.put(userId, fullName));
        }
        return names;
    }

    /**
     * Posts a top-level comment (replyToCommentId == null) or a reply to one top-level
     * comment. Rejects blank content and anything that breaks the reply rules with an
     * IllegalArgumentException. Returns the generated CommentID.
     */
    public int postComment(int userId, int eventId, String content, Integer replyToCommentId) {
        String text = content == null ? "" : content.strip();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_CONTENT);
        }
        if (replyToCommentId != null) {
            validateReplyTarget(eventId, replyToCommentId);
        }
        return comments.insert(userId, eventId, text, replyToCommentId);
    }

    // One read of the event's comments is enough to check every reply rule.
    private void validateReplyTarget(int eventId, int replyToCommentId) {
        List<Comment> onEvent = comments.findByEvent(eventId);

        Comment target = onEvent.stream()
                .filter(comment -> comment.getCommentId() == replyToCommentId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(REPLY_TARGET_MISSING));

        // A reply's own ReplyToCommentID is non-null, so this is a reply-to-a-reply.
        if (target.getReplyToCommentId() != null) {
            throw new IllegalArgumentException(REPLY_TO_REPLY);
        }

        boolean alreadyAnswered = onEvent.stream()
                .anyMatch(comment -> comment.getReplyToCommentId() != null
                        && comment.getReplyToCommentId() == replyToCommentId);
        if (alreadyAnswered) {
            throw new IllegalArgumentException(REPLY_ALREADY_EXISTS);
        }
    }
}
