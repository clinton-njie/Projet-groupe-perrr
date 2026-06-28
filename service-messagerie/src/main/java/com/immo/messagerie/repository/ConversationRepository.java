package com.immo.messagerie.repository;

import com.immo.messagerie.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE (c.senderId = :userId OR c.receiverId = :userId) AND c.isDeleted = false ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE ((c.senderId = :senderId AND c.receiverId = :receiverId) OR (c.senderId = :receiverId AND c.receiverId = :senderId)) AND c.bienId = :bienId AND c.isDeleted = false")
    Optional<Conversation> findExistingConversation(@Param("senderId") Long senderId,
                                                     @Param("receiverId") Long receiverId,
                                                     @Param("bienId") Long bienId);
}
