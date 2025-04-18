package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private static final String AUTH_HEADER = "Authorization";

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public Message sendMessage(@Payload Message message) {
        return messageService.sendMessage(message);
    }

    @PutMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getOfflineMessages(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        List<Message> messages = messageService.getAndMarkAsRead(senderId, recipientId);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/conversation/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long senderId,
                                                                 @PathVariable Long recipientId){
        List<Message> conversation = messageService.getConversation(senderId, recipientId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getAllMessages(@PathVariable Long senderId,
                                                        @PathVariable Long recipientId) {
        List<Message> messages =  messageService.getAllMessages(senderId, recipientId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/contacts")
    public ResponseEntity<List<ContactDTO>> getChatContacts(@RequestHeader(AUTH_HEADER) String token){
        User user = userService.getUserByToken(token);
        List<ContactDTO> contacts = messageService.getChatContacts(user.getId());
        return ResponseEntity.ok(contacts);
    }
}
