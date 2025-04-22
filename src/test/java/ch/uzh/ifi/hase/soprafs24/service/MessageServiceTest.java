package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        Message message1 = new Message();
        message1.setSenderId(senderId);
        message1.setRecipientId(recipientId);
        message1.setContent("Hello");

        Message message2 = new Message();
        message2.setSenderId(recipientId);
        message2.setRecipientId(senderId);
        message2.setContent("Hi");

        when(messageRepository.findConversation(senderId, recipientId)).thenReturn(Arrays.asList(message1, message2));

        var result = messageService.getConversation(senderId, recipientId);

        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("Hi", result.get(1).getContent());
    }

    @Test
    public void testGetChatContacts() {
        Long userId = 1L;
        Long partnerId = 2L;

        when(messageRepository.findDistinctChatPartnerIds(userId)).thenReturn(Arrays.asList(partnerId));

        User partner = new User();
        partner.setId(partnerId);
        partner.setUsername("Alice");

        when(userRepository.findById(partnerId)).thenReturn(Optional.of(partner));

        Message lastMessage = new Message();
        lastMessage.setContent("Hello there");

        when(messageRepository.findTopByUserPairOrderByTimestampDesc(userId, partnerId)).thenReturn(lastMessage);

        var contacts = messageService.getChatContacts(userId);

        assertEquals(1, contacts.size());
        assertEquals(partnerId, contacts.get(0).getId());
        assertEquals("Alice", contacts.get(0).getUsername());
        assertEquals("Hello there", contacts.get(0).getLastMessage());
    }



}