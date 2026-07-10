package com.example.realtimechat.notification;

import com.example.realtimechat.notification.domain.Notification;
import com.example.realtimechat.notification.infrastructure.NotificationRepository;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.infrastructure.UserRepository;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.infrastructure.ConversationRepository;
import com.example.realtimechat.auth.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple testing
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", "Test User");
        userRepository.save(testUser);

        Conversation conversation = Conversation.createDirect();
        conversationRepository.save(conversation);

        testNotification = new Notification(testUser, conversation, null, "NEW_MESSAGE", "Bạn có tin nhắn mới");
        notificationRepository.save(testNotification);

        // Mock authentication
        AuthenticatedUser authUser = new AuthenticatedUser(testUser.getId(), testUser.getUsername(), "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())
        );
    }

    @Test
    void shouldGetNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content", is("Bạn có tin nhắn mới")));
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        mockMvc.perform(put("/api/v1/notifications/{id}/read", testNotification.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Notification marked as read")));

        Notification updated = notificationRepository.findById(testNotification.getId()).orElseThrow();
        assert updated.getReadAt() != null;
    }
}
