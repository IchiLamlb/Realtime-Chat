package com.example.realtimechat.conversation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            select c from Conversation c
            join ConversationMember cm on cm.conversation = c
            where cm.user.id = :userId
            order by c.updatedAt desc
            """)
    List<Conversation> findAllByMember(@Param("userId") UUID userId);
}
