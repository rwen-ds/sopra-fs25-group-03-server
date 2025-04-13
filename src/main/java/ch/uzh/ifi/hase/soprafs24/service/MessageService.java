package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
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

        if (messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No messages found between sender and recipient.");
        }

        return messages;
    }
}
