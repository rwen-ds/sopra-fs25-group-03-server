package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

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
        if (senderId.equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot get conversation with self");
        }
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

    public void chat(MessageDTO messageDTO) {
        if (messageDTO.getSenderId().equals(messageDTO.getRecipientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender and recipient cannot be the same user");
        }
        User sender = userRepository.findById(messageDTO.getSenderId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));
        User recipient = userRepository.findById(messageDTO.getRecipientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient not found"));
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(messageDTO.isRead());
        messageRepository.save(message);

        DeferredResult<String> waiting = waitingUsers.get(recipient.getId());
        if (waiting != null) {
            waiting.setResult(sender.getId() + ":" + message.getContent());
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
