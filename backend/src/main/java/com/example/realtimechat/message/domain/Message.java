package com.example.realtimechat.message.domain;

import com.example.realtimechat.common.domain.BaseEntity;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata = Map.of();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    protected Message() {
    }

    public Message(Conversation conversation, User sender, MessageType type, String content, Map<String, Object> metadata) {
        this.conversation = conversation;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    public void edit(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    public void markDeleted() {
        this.content = "";
        this.metadata = Map.of();
        this.status = MessageStatus.DELETED;
    }

    public void markDelivered() {
        if (MessageStatus.SENT.equals(status)) {
            this.status = MessageStatus.DELIVERED;
        }
    }

    public void markRead() {
        if (!MessageStatus.DELETED.equals(status)) {
            this.status = MessageStatus.READ;
        }
    }

    public UUID getId() {
        return id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public User getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public MessageStatus getStatus() {
        return status;
    }
}
