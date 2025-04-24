package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private static final String AUTH_HEADER = "Authorization";

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PutMapping("/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getOfflineMessages(
            @PathVariable Long senderId,
            @PathVariable Long recipientId) {
        List<Message> messages = messageService.getAndMarkAsRead(senderId, recipientId);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long senderId,
                                                                 @PathVariable Long recipientId){
        List<Message> conversation = messageService.getConversation(senderId, recipientId);
        return ResponseEntity.ok(conversation);
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
    public String chat(@RequestBody MessagePostDTO messagePostDTO) {
        Message message = DTOMapper.INSTANCE.convertMessagePostDTOtoEntity(messagePostDTO);
        messageService.chat(message);

        return "sent";
    }


}
