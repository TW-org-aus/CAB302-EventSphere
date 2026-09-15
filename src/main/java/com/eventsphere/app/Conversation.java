// Conversation.java
import java.time.LocalDateTime;

public class Conversation {
    private int conversationId;
    private int userAId;
    private int userBId;
    private LocalDateTime createdAt;

    public Conversation(int conversationId, int userAId, int userBId, LocalDateTime createdAt) {
        this.conversationId = conversationId;
        this.userAId = userAId;
        this.userBId = userBId;
        this.createdAt = createdAt;
    }

    public int getConversationId() { return conversationId; }
    public int getUserAId() { return userAId; }
    public int getUserBId() { return userBId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}