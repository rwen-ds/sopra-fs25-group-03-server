package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {
    @Autowired
    private UserRepository userRepository;

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Message sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        messageRepository.save(message);

        // Send to recipient if online
        messagingTemplate.convertAndSend(
                "/topic/messages/" + message.getRecipientId(),
                message
        );

        return message;
    }

    public List<Message> getAndMarkAsRead(Long senderId, Long recipientId) {
        List<Message> messages = messageRepository
                .findBySenderIdAndRecipientIdAndIsReadFalse(senderId, recipientId);

        // Mark messages as read
        messages.forEach(msg -> {
            msg.setRead(true);
            messageRepository.save(msg);
        });
        return messages;
    }

    public List<Message> getAllMessages(Long senderId, Long recipientId) {
        List<Message> messages =  messageRepository.findBySenderIdAndRecipientId(senderId, recipientId);

        return messages;
    }

    public List<Message> getConversation(Long senderId, Long recipientId) {
        return messageRepository.findConversation(senderId, recipientId);
    }

    public List<ContactDTO> getChatContacts(Long userId) {
        List<Long> partnerIds = messageRepository.findDistinctChatPartnerIds(userId);
        List<ContactDTO> contactDTOs = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            User user = userRepository.findById(partnerId).orElse(null);
            if (user == null) continue;

            // get the last message
            Message lastMessage = messageRepository.findTopByUserPairOrderByTimestampDesc(userId, partnerId);
            String preview = lastMessage != null ? lastMessage.getContent() : null;

            contactDTOs.add(new ContactDTO(user.getId(), user.getUsername(), preview));
        }

        return contactDTOs;
    }

}
