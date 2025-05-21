package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ErrorResponse;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private static final String AUTH_HEADER = "token";

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PutMapping("/mark-read/{senderId}/{recipientId}")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        try {
            messageService.markMessageAsRead(senderId, recipientId);
            return ResponseEntity.noContent().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> hasUnreadMessages(@RequestHeader(AUTH_HEADER) String token) {
        try {
            Map<String, Boolean> response = messageService.hasUnreadMessage(token);
            return ResponseEntity.ok(response);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }


    @GetMapping("/conversation/{senderId}/{recipientId}")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long senderId,
                                                     @PathVariable Long recipientId) {
        try {
            List<Message> conversation = messageService.getConversation(senderId, recipientId);
            List<MessageDTO> messageDTOs = conversation.stream().map(msg -> new MessageDTO(
                    msg.getId(),
                    msg.getSenderId(),
                    msg.getRecipientId(),
                    msg.getContent(),
                    msg.getTimestamp(),
                    msg.isRead()
            )).collect(Collectors.toList());
            return ResponseEntity.ok(messageDTOs);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }

    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getChatContacts(@RequestHeader(AUTH_HEADER) String token) {
        try {
            List<ContactDTO> contacts = messageService.getChatContacts(token);
            return ResponseEntity.ok(contacts);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/poll/{userId}")
    public DeferredResult<String> poll(@PathVariable Long userId) {
        return messageService.poll(userId);
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody MessageDTO messageDTO) {
        try {
            MessageDTO savedMsg = messageService.sendMessage(messageDTO);
            return ResponseEntity.ok(savedMsg);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/new/{userId}")
    public ResponseEntity<List<MessageDTO>> getNewMessages(@PathVariable Long userId) {
        List<Message> newMessages = messageService.getUnreadMessages(userId);
        List<MessageDTO> messageDTOs = newMessages.stream().map(msg -> new MessageDTO(
                msg.getId(),
                msg.getSenderId(),
                msg.getRecipientId(),
                msg.getContent(),
                msg.getTimestamp(),
                msg.isRead()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(messageDTOs);
    }

}
