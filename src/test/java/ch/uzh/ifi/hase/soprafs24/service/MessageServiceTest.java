package ch.uzh.ifi.hase.soprafs24.service;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    public void testGetConversation() {
        Long senderId = 1L;
        Long recipientId = 2L;

        User sender = new User();
        sender.setId(senderId);

        User recipient = new User();
        recipient.setId(recipientId);

        Message message1 = new Message();
        message1.setSender(sender);
        message1.setRecipient(recipient);
        message1.setContent("Hello");

        Message message2 = new Message();
        message2.setSender(recipient);
        message2.setRecipient(sender);
        message2.setContent("Hi");

        when(messageRepository.findConversation(senderId, recipientId)).thenReturn(Arrays.asList(message1, message2));

        var result = messageService.getConversation(senderId, recipientId);

        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("Hi", result.get(1).getContent());
    }

}