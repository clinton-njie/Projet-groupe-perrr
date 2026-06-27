package com.immo.messagerie.service;

import com.immo.messagerie.dto.ConversationRequest;
import com.immo.messagerie.dto.MessageRequest;
import com.immo.messagerie.dto.MessageResponse;
import com.immo.messagerie.entity.Conversation;
import com.immo.messagerie.entity.Message;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationService conversationService;

    // Envoyer un message
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        Conversation conversation;

        if (request.getConversationId() != null) {
            conversation = conversationService.getConversationById(request.getConversationId());
        } else {
            ConversationRequest convRequest = ConversationRequest.builder()
                    .receiverId(request.getReceiverId())
                    .bienId(request.getBienId())
                    .build();
            conversation = conversationService.getOrCreateConversation(senderId, convRequest);
        }

        Message message = Message.builder()
                .conversation(conversation)
                .senderId(senderId)
                .contenu(request.getContenu())
                .build();
        messageRepository.save(message);

        conversation.setLastMessage(request.getContenu());
        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);

        return mapToResponse(message);
    }

    // Lire une conversation (liste des messages)
    public List<MessageResponse> getConversationMessages(Long conversationId) {
        List<Message> messages = messageRepository.findAllByConversationId(conversationId);
        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Marquer un message comme lu
    public MessageResponse markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message introuvable"));
        message.setIsRead(true);
        message.setReadAt(Instant.now());
        messageRepository.save(message);
        return mapToResponse(message);
    }

    // Supprimer un message (soft delete)
    public String deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message introuvable"));

        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez supprimer que vos propres messages");
        }

        message.setIsDeleted(true);
        message.setDeletedAt(Instant.now());
        messageRepository.save(message);
        return "Message supprimé avec succès.";
    }

    private MessageResponse mapToResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSenderId())
                .contenu(m.getContenu())
                .isRead(m.getIsRead())
                .readAt(m.getReadAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
