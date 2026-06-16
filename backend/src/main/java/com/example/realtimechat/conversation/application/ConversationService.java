package com.example.realtimechat.conversation.application;


import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.conversation.api.dto.ConversationResponse;
import com.example.realtimechat.conversation.api.dto.CreateDirectConversationRequest;
import com.example.realtimechat.conversation.api.dto.CreateGroupConversationRequest;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.conversation.domain.ConversationType;
import com.example.realtimechat.conversation.domain.MemberRole;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.conversation.infrastructure.ConversationRepository;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.application.UserService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserService userService;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationMemberRepository memberRepository,
            UserService userService
    ) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
    }

    @Transactional
    public ConversationResponse createDirect(UUID currentUserId, CreateDirectConversationRequest request) {
        if (currentUserId.equals(request.targetUserId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DIRECT_CONVERSATION", "Cannot create direct conversation with yourself");
        }

        return memberRepository.findDirectConversationId(currentUserId, request.targetUserId())
                .flatMap(conversationRepository::findById)
                .map(ConversationResponse::from)
                .orElseGet(() -> createNewDirect(currentUserId, request.targetUserId()));
    }

    @Transactional
    public ConversationResponse createGroup(UUID currentUserId, CreateGroupConversationRequest request) {
        User creator = userService.getById(currentUserId);
        Conversation conversation = conversationRepository.save(
                new Conversation(ConversationType.GROUP, request.name(), request.avatarUrl(), creator)
        );

        Set<UUID> allMemberIds = new HashSet<>(request.memberIds());
        allMemberIds.add(currentUserId);
        for (UUID memberId : allMemberIds) {
            User user = userService.getById(memberId);
            MemberRole role = memberId.equals(currentUserId) ? MemberRole.OWNER : MemberRole.MEMBER;
            memberRepository.save(new ConversationMember(conversation, user, role));
        }
        return ConversationResponse.from(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> list(UUID currentUserId) {
        return conversationRepository.findAllByMember(currentUserId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse detail(UUID currentUserId, UUID conversationId) {
        return ConversationResponse.from(getAuthorizedConversation(conversationId, currentUserId));
    }

    @Transactional(readOnly = true)
    public Conversation getAuthorizedConversation(UUID conversationId, UUID userId) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "CONVERSATION_ACCESS_DENIED", "User is not a conversation member");
        }
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CONVERSATION_NOT_FOUND", "Conversation not found"));
    }

    private ConversationResponse createNewDirect(UUID currentUserId, UUID targetUserId) {
        User currentUser = userService.getById(currentUserId);
        User targetUser = userService.getById(targetUserId);
        Conversation conversation = conversationRepository.save(new Conversation(ConversationType.DIRECT, null, null, currentUser));
        memberRepository.save(new ConversationMember(conversation, currentUser, MemberRole.MEMBER));
        memberRepository.save(new ConversationMember(conversation, targetUser, MemberRole.MEMBER));
        return ConversationResponse.from(conversation);
    }
}
