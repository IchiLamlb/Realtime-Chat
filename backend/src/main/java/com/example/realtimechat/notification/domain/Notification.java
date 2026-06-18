package com.example.realtimechat.notification.domain;

import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.message.domain.Message;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String content;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Notification() {
    }

    public Notification(User user, Conversation conversation, Message message, String type, String content) {
        this.user = user;
        this.conversation = conversation;
        this.message = message;
        this.type = type;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Message getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void markRead() {
        this.readAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
