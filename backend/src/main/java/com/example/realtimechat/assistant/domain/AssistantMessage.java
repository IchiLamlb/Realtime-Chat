package com.example.realtimechat.assistant.domain;

import com.example.realtimechat.common.domain.BaseEntity;
import com.example.realtimechat.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "assistant_messages")
public class AssistantMessage extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text", nullable = false)
    private String question;

    @Column(columnDefinition = "text", nullable = false)
    private String answer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata = Map.of();

    protected AssistantMessage() {
    }

    public AssistantMessage(User user, String question, String answer, Map<String, Object> metadata) {
        this.user = user;
        this.question = question;
        this.answer = answer;
        this.metadata = metadata == null ? Map.of() : metadata;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
