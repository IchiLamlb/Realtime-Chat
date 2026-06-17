package com.example.realtimechat.conversation.application;

import com.example.realtimechat.conversation.api.dto.AddConversationMemberRequest;
import com.example.realtimechat.conversation.api.dto.ConversationMemberResponse;
import com.example.realtimechat.conversation.api.dto.ConversationResponse;
import com.example.realtimechat.conversation.api.dto.CreateDirectConversationRequest;
import com.example.realtimechat.conversation.api.dto.CreateGroupConversationRequest;
import com.example.realtimechat.conversation.api.dto.UpdateGroupConversationRequest;
import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.conversation.domain.ConversationType;
import com.example.realtimechat.conversation.domain.MemberRole;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.conversation.infrastructure.ConversationRepository;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.user.application.UserService;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.infrastructure.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {

    private static final UUID ASSISTANT_BOT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            ConversationMemberRepository memberRepository,
            UserService userService,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ConversationResponse createDirect(UUID currentUserId, CreateDirectConversationRequest request) {
        if (currentUserId.equals(request.targetUserId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_DIRECT_CONVERSATION", "Cannot create direct conversation with yourself");
        }

        return memberRepository.findDirectConversationId(currentUserId, request.targetUserId())
                .flatMap(conversationRepository::findById)
                .map(this::toResponse)
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
        return toResponse(conversation);
    }

    @Transactional
    public List<ConversationResponse> list(UUID currentUserId) {
        ensureAssistantConversation(currentUserId);
        return conversationRepository.findAllByMember(currentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConversationResponse assistant(UUID currentUserId) {
        return toResponse(ensureAssistantConversation(currentUserId));
    }

    @Transactional(readOnly = true)
    public ConversationResponse detail(UUID currentUserId, UUID conversationId) {
        return toResponse(getAuthorizedConversation(conversationId, currentUserId));
    }

    @Transactional
    public ConversationResponse updateGroup(UUID currentUserId, UUID conversationId, UpdateGroupConversationRequest request) {
        Conversation conversation = getAuthorizedGroupConversation(conversationId, currentUserId);
        requireManagerRole(conversationId, currentUserId);
        conversation.updateGroupProfile(request.name(), request.avatarUrl());
        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateSettings(UUID currentUserId, UUID conversationId, com.example.realtimechat.conversation.api.dto.UpdateConversationSettingsRequest request) {
        Conversation conversation = getAuthorizedConversation(conversationId, currentUserId);
        if (conversation.isGroup()) {
            requireManagerRole(conversationId, currentUserId);
        }
        conversation.updateSettings(request.theme(), request.backgroundColor());
        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse updateNickname(UUID currentUserId, UUID conversationId, UUID targetUserId, com.example.realtimechat.conversation.api.dto.UpdateNicknameRequest request) {
        Conversation conversation = getAuthorizedConversation(conversationId, currentUserId);
        ConversationMember targetMember = getMember(conversationId, targetUserId);
        targetMember.updateNickname(request.nickname());
        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse addMember(UUID currentUserId, UUID conversationId, AddConversationMemberRequest request) {
        Conversation conversation = getAuthorizedGroupConversation(conversationId, currentUserId);
        requireManagerRole(conversationId, currentUserId);
        if (memberRepository.existsByConversationIdAndUserId(conversationId, request.userId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "MEMBER_ALREADY_EXISTS", "User is already a group member");
        }
        User user = userService.getById(request.userId());
        memberRepository.save(new ConversationMember(conversation, user, MemberRole.MEMBER));
        return toResponse(conversation);
    }

    @Transactional
    public ConversationResponse removeMember(UUID currentUserId, UUID conversationId, UUID memberId) {
        Conversation conversation = getAuthorizedGroupConversation(conversationId, currentUserId);
        requireManagerRole(conversationId, currentUserId);
        if (currentUserId.equals(memberId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_MEMBER_REMOVAL", "Use leave group endpoint to remove yourself");
        }

        ConversationMember targetMember = getMember(conversationId, memberId);
        ConversationMember currentMember = getMember(conversationId, currentUserId);
        if (MemberRole.OWNER.equals(targetMember.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "OWNER_REMOVAL_DENIED", "Owner cannot be removed from group");
        }
        if (MemberRole.ADMIN.equals(targetMember.getRole()) && !MemberRole.OWNER.equals(currentMember.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "ADMIN_REMOVAL_DENIED", "Only owner can remove an admin");
        }

        memberRepository.deleteByConversationIdAndUserId(conversationId, memberId);
        return toResponse(conversation);
    }

    @Transactional
    public void leaveGroup(UUID currentUserId, UUID conversationId) {
        Conversation conversation = getAuthorizedGroupConversation(conversationId, currentUserId);
        ConversationMember currentMember = getMember(conversation.getId(), currentUserId);
        if (MemberRole.OWNER.equals(currentMember.getRole())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OWNER_LEAVE_DENIED", "Owner must dissolve the group instead of leaving it");
        }
        memberRepository.deleteByConversationIdAndUserId(conversationId, currentUserId);
    }

    @Transactional
    public void dissolveGroup(UUID currentUserId, UUID conversationId) {
        Conversation conversation = getAuthorizedGroupConversation(conversationId, currentUserId);
        ConversationMember currentMember = getMember(conversationId, currentUserId);
        if (!MemberRole.OWNER.equals(currentMember.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "GROUP_DISSOLVE_DENIED", "Only owner can dissolve the group");
        }
        conversationRepository.delete(conversation);
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
        return toResponse(conversation);
    }

    private Conversation ensureAssistantConversation(UUID currentUserId) {
        if (ASSISTANT_BOT_USER_ID.equals(currentUserId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ASSISTANT_CONVERSATION", "Bot cannot chat with itself");
        }

        return memberRepository.findDirectConversationId(currentUserId, ASSISTANT_BOT_USER_ID)
                .flatMap(conversationRepository::findById)
                .orElseGet(() -> createAssistantConversation(currentUserId));
    }

    private Conversation createAssistantConversation(UUID currentUserId) {
        User currentUser = userService.getById(currentUserId);
        User assistant = userRepository.findById(ASSISTANT_BOT_USER_ID)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "ASSISTANT_BOT_NOT_FOUND", "Assistant bot user is not provisioned"));
        Conversation conversation = conversationRepository.save(new Conversation(ConversationType.DIRECT, null, null, currentUser));
        memberRepository.save(new ConversationMember(conversation, currentUser, MemberRole.MEMBER));
        memberRepository.save(new ConversationMember(conversation, assistant, MemberRole.MEMBER));
        return conversation;
    }

    private Conversation getAuthorizedGroupConversation(UUID conversationId, UUID userId) {
        Conversation conversation = getAuthorizedConversation(conversationId, userId);
        if (!conversation.isGroup()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "GROUP_CONVERSATION_REQUIRED", "Operation is only allowed for group conversations");
        }
        return conversation;
    }

    private void requireManagerRole(UUID conversationId, UUID userId) {
        ConversationMember member = getMember(conversationId, userId);
        if (!MemberRole.OWNER.equals(member.getRole()) && !MemberRole.ADMIN.equals(member.getRole())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "GROUP_MANAGER_REQUIRED", "Only owner or admin can manage this group");
        }
    }

    private ConversationMember getMember(UUID conversationId, UUID userId) {
        return memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Conversation member not found"));
    }

    private ConversationResponse toResponse(Conversation conversation) {
        List<ConversationMemberResponse> members = memberRepository.findByConversationId(conversation.getId()).stream()
                .map(ConversationMemberResponse::from)
                .toList();
        return ConversationResponse.from(conversation, members);
    }
}
