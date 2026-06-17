package com.example.realtimechat.conversation.domain;

import com.example.realtimechat.common.domain.BaseEntity;
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
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "theme")
    private String theme;

    @Column(name = "background_color")
    private String backgroundColor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    protected Conversation() {
    }

    public Conversation(ConversationType type, String name, String avatarUrl, User createdBy) {
        this.type = type;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public ConversationType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getTheme() {
        return theme;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public boolean isGroup() {
        return ConversationType.GROUP.equals(type);
    }

    public void updateGroupProfile(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public void updateSettings(String theme, String backgroundColor) {
        this.theme = theme;
        this.backgroundColor = backgroundColor;
    }
}
