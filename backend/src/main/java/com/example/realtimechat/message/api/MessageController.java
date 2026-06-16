package com.example.realtimechat.message.api;


import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.common.api.ApiResponse;
import com.example.realtimechat.message.api.dto.MessageHistoryResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.ReadReceiptResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.api.dto.UpdateMessageRequest;
import com.example.realtimechat.message.application.MessageService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MessageController {

    private final CurrentUser currentUser;
    private final MessageService messageService;

    public MessageController(CurrentUser currentUser, MessageService messageService) {
        this.currentUser = currentUser;
        this.messageService = messageService;
    }

    @PostMapping("/messages")
    ApiResponse<MessageResponse> send(@Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok("Message sent", messageService.send(currentUser.id(), request));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    ApiResponse<MessageHistoryResponse> history(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ApiResponse.ok("Messages found", messageService.history(currentUser.id(), conversationId, cursor, limit));
    }

    @PatchMapping("/messages/{messageId}")
    ApiResponse<MessageResponse> update(
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        return ApiResponse.ok("Message updated", messageService.update(currentUser.id(), messageId, request));
    }

    @DeleteMapping("/messages/{messageId}")
    ApiResponse<MessageResponse> delete(@PathVariable UUID messageId) {
        return ApiResponse.ok("Message deleted", messageService.delete(currentUser.id(), messageId));
    }

    @PostMapping("/messages/{messageId}/read")
    ApiResponse<ReadReceiptResponse> markRead(@PathVariable UUID messageId) {
        return ApiResponse.ok("Message marked as read", messageService.markRead(currentUser.id(), messageId));
    }
}
