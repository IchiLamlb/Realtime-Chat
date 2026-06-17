package com.example.realtimechat.message.api;


import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.common.api.ApiResponse;
import com.example.realtimechat.message.api.dto.ReactMessageRequest;
import com.example.realtimechat.message.api.dto.MessageHistoryResponse;
import com.example.realtimechat.message.api.dto.MessageReceiptResponse;
import com.example.realtimechat.message.api.dto.MessageResponse;
import com.example.realtimechat.message.api.dto.SendMessageRequest;
import com.example.realtimechat.message.api.dto.UpdateMessageRequest;
import com.example.realtimechat.message.application.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Validated
public class MessageController {

    private final CurrentUser currentUser;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(
            CurrentUser currentUser,
            MessageService messageService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.currentUser = currentUser;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/messages")
    ApiResponse<MessageResponse> send(@Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok("Message sent", messageService.send(currentUser.id(), request));
    }

    @PostMapping(value = "/messages/attachments", consumes = "multipart/form-data")
    ApiResponse<MessageResponse> sendAttachment(
            @RequestParam UUID conversationId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) @Size(max = 5000) String content
    ) {
        MessageResponse response = messageService.sendAttachment(currentUser.id(), conversationId, file, content);
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
        return ApiResponse.ok("Attachment sent", response);
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

    @PostMapping("/messages/{messageId}/delivered")
    ApiResponse<MessageReceiptResponse> markDelivered(@PathVariable UUID messageId) {
        return ApiResponse.ok("Message marked as delivered", messageService.markDelivered(currentUser.id(), messageId));
    }

    @PostMapping("/messages/{messageId}/read")
    ApiResponse<MessageReceiptResponse> markRead(@PathVariable UUID messageId) {
        return ApiResponse.ok("Message marked as read", messageService.markRead(currentUser.id(), messageId));
    }

    @PostMapping("/messages/{messageId}/react")
    ApiResponse<MessageResponse> react(
            @PathVariable UUID messageId,
            @Valid @RequestBody ReactMessageRequest request
    ) {
        MessageResponse response = messageService.react(currentUser.id(), messageId, request.emoji());
        messagingTemplate.convertAndSend("/topic/conversations/" + response.conversationId(), response);
        return ApiResponse.ok("Message reaction updated", response);
    }
}
