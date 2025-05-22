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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageService {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public MessageService(UserRepository userRepository, MessageRepository messageRepository, UserService userService) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    private final Map<Long, DeferredResult<String>> waitingUsers = new ConcurrentHashMap<>();

    public Map<String, Boolean> hasUnreadMessage(String token) {
        User user = userService.getUserByToken(token);
        boolean hasUnread = messageRepository.existsUnreadByRecipientId(user.getId());
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUnread", hasUnread);
        return response;
    }

    public void markMessageAsRead(Long senderId, Long recipientId) {
        List<Message> messages = messageRepository
                .findBySenderIdAndRecipientIdAndIsReadFalse(senderId, recipientId);

        if (!messages.isEmpty()) {
            messages.forEach(msg -> msg.setRead(true));
            messageRepository.saveAll(messages);
        }
    }

    public List<Message> getConversation(Long senderId, Long recipientId) {
        if (senderId.equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot get conversation with self");
        }
        return messageRepository.findConversation(senderId, recipientId);
    }

    public List<ContactDTO> getChatContacts(String token) {
        User currentUser = userService.getUserByToken(token);
        List<Long> partnerIds = messageRepository.findDistinctChatPartnerIds(currentUser.getId());
        List<ContactDTO> contactDTOs = new ArrayList<>();

        for (Long partnerId : partnerIds) {
            User user = userRepository.findById(partnerId).orElse(null);
            if (user == null) continue;
            boolean hasUnread = messageRepository.hasUnreadMessages(partnerId, currentUser.getId());

            // get the last message
//            List<Message> messages = messageRepository.findTopByUserPairOrderByTimestampDesc(currentUser.getId(), partnerId);
//            Message lastMessage = messages.isEmpty() ? null : messages.get(0);
//            String preview = lastMessage != null ? lastMessage.getContent() : null;

            contactDTOs.add(new ContactDTO(user.getId(), user.getUsername(), hasUnread));
        }

        return contactDTOs;
    }

    public void chat(MessageDTO messageDTO) {
        Long senderId = messageDTO.getSenderId();
        Long recipientId = messageDTO.getRecipientId();
        if (senderId.equals(recipientId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender and recipient cannot be the same user");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(messageDTO.isRead());
        messageRepository.save(message);

        DeferredResult<String> waiting = waitingUsers.get(recipientId);
        if (waiting != null) {
            waiting.setResult(senderId + ":" + message.getContent());
        }
    }

    public DeferredResult<String> poll(Long userId) {
        DeferredResult<String> result = new DeferredResult<>(30000L, "timeout");

        waitingUsers.put(userId, result);

        result.onCompletion(() -> waitingUsers.remove(userId));
        result.onTimeout(() -> waitingUsers.remove(userId));

        return result;
    }

    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByRecipientIdAndIsReadFalse(userId);
    }

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setRecipientId(messageDTO.getRecipientId());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        Message savedMessage = messageRepository.save(message);

        return new MessageDTO(savedMessage.getId(), savedMessage.getSenderId(),
                savedMessage.getRecipientId(), savedMessage.getContent(),
                savedMessage.getTimestamp(), savedMessage.isRead());
    }
}
