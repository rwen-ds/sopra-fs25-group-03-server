package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    private final Map<Long, DeferredResult<String>> waitingUsers = new ConcurrentHashMap<>();

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
            List<Message> messages = messageRepository.findTopByUserPairOrderByTimestampDesc(userId, partnerId);
            Message lastMessage = messages.isEmpty() ? null : messages.get(0);
            String preview = lastMessage != null ? lastMessage.getContent() : null;

            contactDTOs.add(new ContactDTO(user.getId(), user.getUsername(), preview));
        }

        return contactDTOs;
    }

    public void chat(Message message) {
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        messageRepository.save(message);

        DeferredResult<String> waiting = waitingUsers.get(message.getRecipientId());
        if (waiting != null) {
            waiting.setResult(message.getSenderId() + ":" + message.getContent());
        }
    }

    public DeferredResult<String> poll(Long userId) {
        DeferredResult<String> result = new DeferredResult<>(30000L, "timeout");

        waitingUsers.put(userId, result);

        result.onCompletion(() -> waitingUsers.remove(userId));
        result.onTimeout(() -> waitingUsers.remove(userId));

        return result;
    }
}
