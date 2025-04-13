package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
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

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getAllMessages(@PathVariable Long senderId,
                                        @PathVariable Long recipientId) {
        List<Message> messages =  messageService.getAllMessages(senderId, recipientId);
        return ResponseEntity.ok(messages);
    }
}
