package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private static final String AUTH_HEADER = "token";
    private final MessageRepository messageRepository;

    public MessageController(MessageService messageService, UserService userService, UserRepository userRepository, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PutMapping("/mark-read/{senderId}/{recipientId}")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        messageService.markMessageAsRead(senderId, recipientId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Boolean>> hasUnreadMessages(@RequestHeader(AUTH_HEADER) String token) {
        User user = userRepository.findByToken(token);

        boolean hasUnread = messageRepository.existsUnreadByRecipientId(user.getId());

        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUnread", hasUnread);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/conversation/{senderId}/{recipientId}")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long senderId,
                                                                 @PathVariable Long recipientId){
        List<Message> conversation = messageService.getConversation(senderId, recipientId);

        List<MessageDTO> messageDTOs = conversation.stream().map(msg -> {
            return new MessageDTO(
                    msg.getSender().getId(),
                    msg.getRecipient().getId(),
                    msg.getContent(),
                    msg.getTimestamp(),
                    msg.isRead()
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDTO>> getChatContacts(@RequestHeader(AUTH_HEADER) String token){
        User user = userService.getUserByToken(token);
        List<ContactDTO> contacts = messageService.getChatContacts(user.getId());
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/poll/{userId}")
    public DeferredResult<String> poll(@PathVariable Long userId) {
        return messageService.poll(userId);
    }

    @PostMapping("/send")
    public String chat(@RequestBody MessageDTO messageDTO) {
        messageService.chat(messageDTO);
        return "sent";
    }


}
