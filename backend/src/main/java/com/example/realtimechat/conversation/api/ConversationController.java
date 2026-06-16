package com.example.realtimechat.conversation.api;


import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.common.api.ApiResponse;
import com.example.realtimechat.conversation.api.dto.AddConversationMemberRequest;
import com.example.realtimechat.conversation.api.dto.ConversationResponse;
import com.example.realtimechat.conversation.api.dto.CreateDirectConversationRequest;
import com.example.realtimechat.conversation.api.dto.CreateGroupConversationRequest;
import com.example.realtimechat.conversation.api.dto.UpdateGroupConversationRequest;
import com.example.realtimechat.conversation.application.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PatchMapping("/{conversationId}")
    ApiResponse<ConversationResponse> updateGroup(
            @PathVariable UUID conversationId,
            @Valid @RequestBody UpdateGroupConversationRequest request
    ) {
        return ApiResponse.ok("Group conversation updated", conversationService.updateGroup(currentUser.id(), conversationId, request));
    }

    @PostMapping("/{conversationId}/members")
    ApiResponse<ConversationResponse> addMember(
            @PathVariable UUID conversationId,
            @Valid @RequestBody AddConversationMemberRequest request
    ) {
        return ApiResponse.ok("Group member added", conversationService.addMember(currentUser.id(), conversationId, request));
    }

    @DeleteMapping("/{conversationId}/members/{memberId}")
    ApiResponse<ConversationResponse> removeMember(@PathVariable UUID conversationId, @PathVariable UUID memberId) {
        return ApiResponse.ok("Group member removed", conversationService.removeMember(currentUser.id(), conversationId, memberId));
    }

    @DeleteMapping("/{conversationId}/members/me")
    ApiResponse<Void> leaveGroup(@PathVariable UUID conversationId) {
        conversationService.leaveGroup(currentUser.id(), conversationId);
        return ApiResponse.ok("Left group", null);
    }

    @DeleteMapping("/{conversationId}")
    ApiResponse<Void> dissolveGroup(@PathVariable UUID conversationId) {
        conversationService.dissolveGroup(currentUser.id(), conversationId);
        return ApiResponse.ok("Group dissolved", null);
    }
}
