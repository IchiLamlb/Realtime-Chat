package com.example.realtimechat.conversation.api;


import com.example.realtimechat.conversation.api.dto.ConversationResponse;
import com.example.realtimechat.conversation.api.dto.CreateDirectConversationRequest;
import com.example.realtimechat.conversation.api.dto.CreateGroupConversationRequest;
import com.example.realtimechat.conversation.application.ConversationService;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final CurrentUser currentUser;
    private final ConversationService conversationService;

    public ConversationController(CurrentUser currentUser, ConversationService conversationService) {
        this.currentUser = currentUser;
        this.conversationService = conversationService;
    }

    @PostMapping("/direct")
    ApiResponse<ConversationResponse> createDirect(@Valid @RequestBody CreateDirectConversationRequest request) {
        return ApiResponse.ok("Direct conversation ready", conversationService.createDirect(currentUser.id(), request));
    }

    @PostMapping("/group")
    ApiResponse<ConversationResponse> createGroup(@Valid @RequestBody CreateGroupConversationRequest request) {
        return ApiResponse.ok("Group conversation created", conversationService.createGroup(currentUser.id(), request));
    }

    @GetMapping
    ApiResponse<List<ConversationResponse>> list() {
        return ApiResponse.ok("Conversations found", conversationService.list(currentUser.id()));
    }

    @GetMapping("/{conversationId}")
    ApiResponse<ConversationResponse> detail(@PathVariable UUID conversationId) {
        return ApiResponse.ok("Conversation found", conversationService.detail(currentUser.id(), conversationId));
    }
}
