package com.eventsphere.app;

import com.eventsphere.app.model.Comment;
import com.eventsphere.app.model.Event;
import com.eventsphere.app.model.User;
import com.eventsphere.app.service.CommentService;
import com.eventsphere.app.service.EventService;
import com.eventsphere.app.service.LikeResult;
import com.eventsphere.app.service.LikeService;
import com.eventsphere.app.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventPageController {

    private static final double IMAGE_W = 464.0;
    private static final double IMAGE_H = 180.0;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter COMMENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy").withZone(ZoneId.systemDefault());

    @FXML private VBox eventFieldsBox;
    @FXML private ImageView eventImage;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private HBox venueRow;
    @FXML private Label venueLabel;
    @FXML private Label notFoundLabel;
    @FXML private HBox actionBar;
    @FXML private ToggleButton likeButton;
    @FXML private ToggleButton goingButton;
    @FXML private Button ticketsButton;

    @FXML private Label commentsCountLabel;
    @FXML private ToggleButton sortTopButton;
    @FXML private ToggleButton sortNewestButton;
    @FXML private VBox commentsList;
    @FXML private HBox replyChip;
    @FXML private Label replyChipLabel;
    @FXML private Button replyChipClose;
    @FXML private TextField commentInput;
    @FXML private Button postButton;
    @FXML private Label commentErrorLabel;

    private final EventService eventService;
    private final LikeService likeService;
    private final CommentService commentService;
    private final SessionManager session;

    private Event currentEvent;
    private List<Comment> currentComments = List.of();
    private Map<Integer, String> currentNamesByUserId = Map.of();
    // Comment the "Reply" link was clicked on, or null when composing a fresh top-level comment.
    private Comment replyTarget;

    // Router's controller factory supplies these. There is deliberately no no-arg
    // constructor, so the controller cannot reach for a database or session on its own.
    public EventPageController(EventService eventService, LikeService likeService,
                               CommentService commentService, SessionManager session) {
        this.eventService = eventService;
        this.likeService = likeService;
        this.commentService = commentService;
        this.session = session;
    }

    // Called by Router right after it loads this FXML, once the caller knows which event
    // id it navigated here with. Loads synchronously: there is no background thread to wait on.
    public void showEvent(int eventId) {
        try {
            Event found = eventService.findById(eventId);
            if (found == null) {
                showNotFound();
                return;
            }
            fillEvent(found);

        } catch (Exception e) {
            System.err.println("Could not load event " + eventId + ": " + e.getMessage());
            showNotFound();
        }
    }

    private void fillEvent(Event event) {
        currentEvent = event;

        eventFieldsBox.setVisible(true);
        eventFieldsBox.setManaged(true);
        notFoundLabel.setVisible(false);
        notFoundLabel.setManaged(false);

        String url = EventImages.resolveImageUrlWithPlaceholder(event);
        eventImage.setImage(url == null ? null : new Image(url, IMAGE_W, IMAGE_H, false, true, true));

        titleLabel.setText(event.getTitle());

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            descriptionLabel.setVisible(false);
            descriptionLabel.setManaged(false);
        } else {
            descriptionLabel.setText(event.getDescription());
            descriptionLabel.setVisible(true);
            descriptionLabel.setManaged(true);
        }

        dateLabel.setText(DATE_FORMAT.format(event.getStartTime()));
        timeLabel.setText(event.getEndTime() == null
                ? TIME_FORMAT.format(event.getStartTime())
                : TIME_FORMAT.format(event.getStartTime()) + " – " + TIME_FORMAT.format(event.getEndTime()));

        String venue = joinVenue(event);
        if (venue == null) {
            venueRow.setVisible(false);
            venueRow.setManaged(false);
        } else {
            venueLabel.setText(venue);
            venueRow.setVisible(true);
            venueRow.setManaged(true);
        }

        boolean hasTicketUrl = event.getTicketUrl() != null && !event.getTicketUrl().isBlank();
        ticketsButton.setVisible(hasTicketUrl);
        ticketsButton.setManaged(hasTicketUrl);

        // Likes and comments come straight from the database, so reopening the page shows the
        // state the logged-in user (and everyone else) already saved. showGoingState stays a
        // display-only toggle until the going feature is wired up.
        showGoingState(false);
        loadLikeState(event);
        loadComments(event.getEventId());
        clearCommentError();

        updateComposerEnabled();
    }

    private Integer currentUserId() {
        return session.getCurrentUser().map(User::getUserId).orElse(null);
    }

    // Logged out: liked is false, but the count still comes from the database so visitors
    // see the real popularity. A read failure keeps the stored count on screen.
    private void loadLikeState(Event event) {
        try {
            LikeResult state = likeService.stateFor(currentUserId(), event.getEventId());
            showLikeState(state.liked(), state.count());
        } catch (Exception e) {
            System.err.println("Could not load likes for event " + event.getEventId() + ": " + e.getMessage());
            showLikeState(false, event.getLikesCount());
        }
    }

    // Loads the event's stored comments and the author names they need, then re-renders.
    // Called on page load and again after a successful post.
    private void loadComments(int eventId) {
        try {
            List<Comment> comments = commentService.commentsForEvent(eventId);
            showComments(comments, commentService.authorNamesFor(comments));
        } catch (Exception e) {
            System.err.println("Could not load comments for event " + eventId + ": " + e.getMessage());
            showComments(List.of(), Map.of());
        }
    }

    // Joins the venue name and address with ", ", leaving out whichever side is missing.
    private String joinVenue(Event event) {
        if (event.getVenueName() != null && event.getAddress() != null) {
            return event.getVenueName() + ", " + event.getAddress();
        }
        if (event.getVenueName() != null) {
            return event.getVenueName();
        }
        return event.getAddress();
    }

    private void showNotFound() {
        eventFieldsBox.setVisible(false);
        eventFieldsBox.setManaged(false);
        actionBar.setVisible(false);
        actionBar.setManaged(false);
        notFoundLabel.setText("Event not found.");
        notFoundLabel.setVisible(true);
        notFoundLabel.setManaged(true);
    }

    @FXML
    protected void onBackClick() {
        Router.navigateTo("landing-page.fxml");
    }

    // ----- like / going / tickets -----

    // Updates the like button's label and selected state. count is the total read back from
    // LikeService (the trigger-maintained Events.LikesCount).
    public void showLikeState(boolean liked, int count) {
        likeButton.setSelected(liked);
        likeButton.setText((liked ? "♥ " : "♡ ") + count);
    }

    public void showGoingState(boolean going) {
        goingButton.setSelected(going);
        goingButton.setText(going ? "✓ Going" : "Going");
    }

    @FXML
    protected void onLikeClick() {
        if (!session.isLoggedIn()) {
            likeButton.setSelected(false); // undo the toggle the click already applied
            Router.navigateTo("login-view.fxml");
            return;
        }
        if (currentEvent == null) {
            return;
        }
        // The service flips the stored like and returns the state to show, so the button and
        // count always mirror the database. Duplicates are impossible (UNIQUE key +
        // INSERT OR IGNORE), so a double click cannot count twice.
        try {
            LikeResult state = likeService.toggleLike(currentUserId(), currentEvent.getEventId());
            showLikeState(state.liked(), state.count());
        } catch (Exception e) {
            System.err.println("Could not update like for event " + currentEvent.getEventId() + ": " + e.getMessage());
            // Put the button back to whatever the database says, undoing the visual toggle.
            loadLikeState(currentEvent);
        }
    }

    @FXML
    protected void onGoingClick() {
        if (!session.isLoggedIn()) {
            goingButton.setSelected(false);
            Router.navigateTo("login-view.fxml");
            return;
        }
        // TODO: call the attendance/going service to persist the toggle for the current user,
        // then refresh showGoingState from its returned state. For now this just flips the
        // button so the UI is demonstrable; nothing is saved.
        showGoingState(goingButton.isSelected());
    }

    @FXML
    protected void onGetTicketsClick() {
        if (currentEvent == null || currentEvent.getTicketUrl() == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(currentEvent.getTicketUrl()));
        } catch (Exception e) {
            System.err.println("Could not open ticket URL: " + e.getMessage());
        }
    }

    // ----- comments -----

    // Stores the given comments and names, then renders them as threads using the current
    // sort. Missing names fall back to "User #<id>". The badge always shows the number of
    // comments actually loaded, which is what the database holds.
    public void showComments(List<Comment> comments, Map<Integer, String> namesByUserId) {
        currentComments = comments;
        currentNamesByUserId = namesByUserId;
        commentsCountLabel.setText(String.valueOf(comments.size()));
        renderComments();
    }

    @FXML
    protected void onSortChanged(ActionEvent event) {
        // A ToggleGroup on its own lets the user deselect both pills by clicking the selected
        // one again; reselect the clicked pill so the highlight stays where the user clicked.
        ToggleButton clicked = (ToggleButton) event.getSource();
        if (!clicked.isSelected()) {
            clicked.setSelected(true);
        }
        renderComments();
    }

    // Groups replies one level deep under their top-level comment, then renders the threads
    // in the selected order: Top (most replies, ties newest first) or Newest.
    private void renderComments() {
        commentsList.getChildren().clear();

        if (currentComments.isEmpty()) {
            Label empty = new Label("No comments yet. Be the first to comment.");
            empty.getStyleClass().add("empty-comments-label");
            commentsList.getChildren().add(empty);
            return;
        }

        Map<Integer, Comment> byId = new HashMap<>();
        for (Comment c : currentComments) {
            byId.put(c.getCommentId(), c);
        }

        Map<Comment, List<Comment>> repliesByTopLevel = new HashMap<>();
        for (Comment c : currentComments) {
            Comment topLevel = topLevelAncestor(c, byId);
            List<Comment> replies = repliesByTopLevel.computeIfAbsent(topLevel, k -> new ArrayList<>());
            if (topLevel != c) {
                replies.add(c);
            }
        }

        Comparator<Comment> newestFirst = Comparator.comparing(Comment::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder()));
        Comparator<Comment> threadOrder = sortTopButton.isSelected()
                ? Comparator.<Comment>comparingInt(c -> repliesByTopLevel.get(c).size()).reversed()
                        .thenComparing(newestFirst)
                : newestFirst;

        List<Comment> topLevels = new ArrayList<>(repliesByTopLevel.keySet());
        topLevels.sort(threadOrder);

        for (Comment topLevel : topLevels) {
            VBox threadBox = new VBox(8);

            List<Comment> replies = repliesByTopLevel.get(topLevel);
            // One reply per comment: the Reply link disappears once the comment has its reply.
            threadBox.getChildren().add(buildCommentRow(topLevel, null, false, replies.isEmpty()));

            replies.sort(Comparator.comparing(Comment::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            for (Comment reply : replies) {
                // A reply to another reply gets an "@name" prefix, since it renders flat.
                Comment directParent = byId.get(reply.getReplyToCommentId());
                Integer mentionUserId = directParent != topLevel ? directParent.getUserId() : null;

                VBox indent = new VBox(6);
                indent.setStyle("-fx-padding: 0 0 0 24;");
                // Replies cannot be replied to, so they never get a Reply link.
                indent.getChildren().add(buildCommentRow(reply, mentionUserId, true, false));
                threadBox.getChildren().add(indent);
            }
            commentsList.getChildren().add(threadBox);
        }
    }

    // Walks replyToCommentId up to the top-level comment. If a parent was deleted, the
    // highest comment still present counts as top-level. A loop in the chain can't come
    // from the UI, but if one exists the comment shows as top-level instead of hanging.
    private Comment topLevelAncestor(Comment comment, Map<Integer, Comment> byId) {
        Set<Integer> seen = new HashSet<>();
        Comment current = comment;
        while (current.getReplyToCommentId() != null) {
            if (!seen.add(current.getCommentId())) {
                return comment;
            }
            Comment parent = byId.get(current.getReplyToCommentId());
            if (parent == null) {
                return current;
            }
            current = parent;
        }
        return current;
    }

    // "just now" / "N minutes ago" / "N hours ago" / "N days ago", then a plain date.
    private static String relativeTime(Instant when) {
        if (when == null) {
            return "";
        }
        Duration elapsed = Duration.between(when, Instant.now());
        if (elapsed.isNegative()) {
            elapsed = Duration.ZERO;
        }
        long minutes = elapsed.toMinutes();
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        long hours = elapsed.toHours();
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = elapsed.toDays();
        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }
        return COMMENT_DATE_FORMAT.format(when);
    }

    // canReply is false for replies and for comments that already have their one reply, so the
    // UI never offers an action CommentService would refuse.
    private VBox buildCommentRow(Comment comment, Integer directParentUserId, boolean isReply, boolean canReply) {
        VBox row = new VBox(4);
        row.getStyleClass().add(isReply ? "comment-reply" : "comment-row");

        Label author = new Label(nameFor(comment.getUserId()));
        author.getStyleClass().add("comment-author");

        String prefix = "";
        if (isReply && directParentUserId != null) {
            prefix = "@" + nameFor(directParentUserId) + " ";
        }
        Label content = new Label(prefix + comment.getContent());
        content.setWrapText(true);

        String metaText = relativeTime(comment.getCreatedAt());
        if (comment.getUpdatedAt() != null) {
            metaText = metaText + " (edited)";
        }
        Label meta = new Label(metaText);
        meta.getStyleClass().add("comment-meta");

        Button replyLink = new Button("Reply");
        replyLink.getStyleClass().add("reply-link");
        replyLink.setVisible(canReply);
        replyLink.setManaged(canReply);
        replyLink.setOnAction(e -> onReplyClick(comment));
        replyLink.setDisable(!session.isLoggedIn());

        HBox metaRow = new HBox(10, meta, replyLink);
        metaRow.setStyle("-fx-alignment: center-left;");

        row.getChildren().addAll(author, content, metaRow);
        return row;
    }

    private String nameFor(int userId) {
        String name = currentNamesByUserId.get(userId);
        return name != null ? name : "User #" + userId;
    }

    private void onReplyClick(Comment comment) {
        replyTarget = comment;
        String name = nameFor(comment.getUserId());
        replyChipLabel.setText("Replying to " + name);
        replyChip.setVisible(true);
        replyChip.setManaged(true);
        clearCommentError();
        commentInput.setText("@" + name + " ");
        commentInput.requestFocus();
        commentInput.positionCaret(commentInput.getText().length());
    }

    @FXML
    protected void onCancelReplyClick() {
        clearReplyTarget();
        clearCommentError();
        commentInput.clear();
    }

    // Stores the comment (or reply) through CommentService, then reloads the list from the
    // database so the new row is rendered with its real id, timestamp and author. The service
    // owns the reply rules and throws IllegalArgumentException with a reason when one is hit.
    @FXML
    protected void onPostCommentClick() {
        if (!session.isLoggedIn() || currentEvent == null) {
            return;
        }
        String content = commentInput.getText();
        if (content == null || content.isBlank()) {
            return;
        }
        clearCommentError();
        Integer replyToCommentId = replyTarget == null ? null : replyTarget.getCommentId();
        try {
            commentService.postComment(currentUserId(), currentEvent.getEventId(),
                    content, replyToCommentId);
        } catch (IllegalArgumentException e) {
            // Refused by a reply rule (target deleted, already replied to, ...). Keep the typed
            // text so it is not lost and tell the user why.
            System.err.println("Could not post comment: " + e.getMessage());
            showCommentError(e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("Could not post comment: " + e.getMessage());
            showCommentError("Could not post this comment. Please try again.");
            return;
        }
        clearReplyTarget();
        commentInput.clear();
        loadComments(currentEvent.getEventId());
    }

    // Hides the reply chip and forgets the target, without touching the input text.
    private void clearReplyTarget() {
        replyTarget = null;
        replyChip.setVisible(false);
        replyChip.setManaged(false);
    }

    private void showCommentError(String message) {
        commentErrorLabel.setText(message);
        commentErrorLabel.setVisible(true);
        commentErrorLabel.setManaged(true);
    }

    private void clearCommentError() {
        commentErrorLabel.setText("");
        commentErrorLabel.setVisible(false);
        commentErrorLabel.setManaged(false);
    }

    // Disables the composer and reply links when logged out, and swaps the prompt text.
    private void updateComposerEnabled() {
        boolean loggedIn = session.isLoggedIn();
        commentInput.setDisable(!loggedIn);
        postButton.setDisable(!loggedIn);
        commentInput.setPromptText(loggedIn ? "Write a comment....." : "Log in to comment");
    }
}
