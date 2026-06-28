package com.immo.messagerie.service;

import com.immo.messagerie.dto.ConversationRequest;
import com.immo.messagerie.dto.ConversationResponse;
import com.immo.messagerie.entity.Conversation;
import com.immo.messagerie.exception.ResourceNotFoundException;
import com.immo.messagerie.repository.ConversationRepository;
import com.immo.messagerie.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    // Récupérer ou créer une conversation (client <-> agent, à propos d'un bien)
    public Conversation getOrCreateConversation(Long senderId, ConversationRequest request) {
        return conversationRepository
                .findExistingConversation(senderId, request.getReceiverId(), request.getBienId())
                .orElseGet(() -> createConversation(senderId, request));
    }

    private Conversation createConversation(Long senderId, ConversationRequest request) {
        Conversation conversation = Conversation.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .bienId(request.getBienId())
                .build();
        return conversationRepository.save(conversation);
    }

    // Boîte de réception
    public List<ConversationResponse> getInbox(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);
        return conversations.stream()
                .map(c -> mapToResponse(c, userId))
                .collect(Collectors.toList());
    }

    // Récupérer une conversation par id
    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation introuvable"));
    }

    // Supprimer une conversation (soft delete)
    public String deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = getConversationById(conversationId);

        if (!conversation.getSenderId().equals(userId) && !conversation.getReceiverId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette conversation");
        }

        conversation.setIsDeleted(true);
        conversation.setDeletedAt(Instant.now());
        conversationRepository.save(conversation);
        return "Conversation supprimée avec succès.";
    }

    private ConversationResponse mapToResponse(Conversation c, Long userId) {
        long unreadCount = messageRepository.countUnreadMessages(c.getId(), userId);
        return ConversationResponse.builder()
                .id(c.getId())
                .senderId(c.getSenderId())
                .receiverId(c.getReceiverId())
                .bienId(c.getBienId())
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .createdAt(c.getCreatedAt())
                .unreadCount(unreadCount)
                .build();
    }
}
