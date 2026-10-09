package com.eventsphere.app.service;

import com.eventsphere.app.dao.INotificationDAO;
import com.eventsphere.app.model.Notification;
import com.eventsphere.app.model.NotificationType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** In-memory INotificationDAO for service-layer tests. */
class MockNotificationDAO implements INotificationDAO {

    final List<Notification> store = new ArrayList<>();
    private int nextId = 1;

    @Override
    public int insert(int userId, NotificationType type, Integer relatedEventId,
                      Integer relatedCommentId, Integer relatedConversationId, String message) {
        int id = nextId++;
        store.add(new Notification(id, userId, type, relatedEventId, relatedCommentId,
                relatedConversationId, message, Instant.now(), false));
        return id;
    }

    @Override
    public List<Notification> findByUser(int userId) {
        return store.stream()
                .filter(n -> n.getUserId() == userId)
                .sorted(Comparator.comparingInt(Notification::getNotificationId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void markRead(int notificationId) {
        store.stream()
                .filter(n -> n.getNotificationId() == notificationId)
                .findFirst()
                .ifPresent(n -> n.setRead(true));
    }

    @Override
    public void markAllRead(int userId) {
        store.stream()
                .filter(n -> n.getUserId() == userId)
                .forEach(n -> n.setRead(true));
    }

    @Override
    public boolean existsForEvent(int userId, NotificationType type, int eventId) {
        return store.stream().anyMatch(n ->
                n.getUserId() == userId &&
                n.getType() == type &&
                Objects.equals(n.getRelatedEventId(), eventId));
    }
}
