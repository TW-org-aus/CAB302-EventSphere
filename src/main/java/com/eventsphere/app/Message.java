// Message.java
import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private int conversationId;
    private int senderId;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;

    public Message(int messageId, int conversationId, int senderId, String content,
                   LocalDateTime sentAt, boolean read) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
        this.read = read;
    }

    public int getMessageId() { return messageId; }
    public int getConversationId() { return conversationId; }
    public int getSenderId() { return senderId; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public boolean isRead() { return read; }
}