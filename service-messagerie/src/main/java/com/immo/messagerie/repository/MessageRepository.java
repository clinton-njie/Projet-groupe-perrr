package com.immo.messagerie.repository;

import com.immo.messagerie.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND m.isDeleted = false ORDER BY m.createdAt ASC")
    List<Message> findAllByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.senderId != :userId AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
