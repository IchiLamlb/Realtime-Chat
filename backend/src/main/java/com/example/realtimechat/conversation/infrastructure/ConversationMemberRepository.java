package com.example.realtimechat.conversation.infrastructure;


import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<ConversationMember> findByConversationId(UUID conversationId);

    @Query("""
            select cm.conversation.id from ConversationMember cm
            where cm.conversation.type = 'DIRECT' and cm.user.id in (:firstUserId, :secondUserId)
            group by cm.conversation.id
            having count(distinct cm.user.id) = 2
            """)
    Optional<UUID> findDirectConversationId(@Param("firstUserId") UUID firstUserId, @Param("secondUserId") UUID secondUserId);
}
