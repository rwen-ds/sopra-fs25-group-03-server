package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageService messageService;

    private User testUser;
    private User otherUser;
    private Message testMessage;
    private String validToken;

    @BeforeEach
    public void setup() {
        validToken = "validToken";
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@edu.example.com");
        testUser.setPassword("password");
        testUser.setToken(validToken);
        testUser.setCreationDate(LocalDate.now());
        testUser.setIsAdmin(false);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        otherUser.setEmail("other@edu.example.com");
        otherUser.setPassword("password");
        otherUser.setToken("otherToken");
        otherUser.setCreationDate(LocalDate.now());
        otherUser.setIsAdmin(false);

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSenderId(1L);
        testMessage.setRecipientId(2L);
        testMessage.setContent("Test message");
        testMessage.setTimestamp(LocalDateTime.now());
        testMessage.setRead(false);
    }

    @Test
    public void testGetConversation_success() {
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
    public void testGetConversation_sameSenderAndRecipient_throwsBadRequest() {
        Long userId = 1L;

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            messageService.getConversation(userId, userId);
        });

        assertTrue(exception.getMessage().contains("Cannot get conversation with self"));
    }

    @Test
    public void testHasUnreadMessage_hasUnread() {
        when(userService.getUserByToken(validToken)).thenReturn(testUser);
        when(messageRepository.existsUnreadByRecipientId(testUser.getId())).thenReturn(true);

        Map<String, Boolean> result = messageService.hasUnreadMessage(validToken);

        assertTrue(result.get("hasUnread"));
        verify(userService).getUserByToken(validToken);
        verify(messageRepository).existsUnreadByRecipientId(testUser.getId());
    }

    @Test
    public void testHasUnreadMessage_noUnread() {
        when(userService.getUserByToken(validToken)).thenReturn(testUser);
        when(messageRepository.existsUnreadByRecipientId(testUser.getId())).thenReturn(false);

        Map<String, Boolean> result = messageService.hasUnreadMessage(validToken);

        assertFalse(result.get("hasUnread"));
    }

    @Test
    public void testMarkMessageAsRead_success() {
        Long senderId = 1L;
        Long recipientId = 2L;
        List<Message> unreadMessages = Arrays.asList(testMessage);

        when(messageRepository.findBySenderIdAndRecipientIdAndIsReadFalse(senderId, recipientId))
                .thenReturn(unreadMessages);

        messageService.markMessageAsRead(senderId, recipientId);

        assertTrue(testMessage.isRead());
        verify(messageRepository).saveAll(unreadMessages);
    }

    @Test
    public void testMarkMessageAsRead_noMessages() {
        Long senderId = 1L;
        Long recipientId = 2L;

        when(messageRepository.findBySenderIdAndRecipientIdAndIsReadFalse(senderId, recipientId))
                .thenReturn(Arrays.asList());

        messageService.markMessageAsRead(senderId, recipientId);

        verify(messageRepository, never()).saveAll(any());
    }

    @Test
    public void testGetChatContacts_success() {
        when(userService.getUserByToken(validToken)).thenReturn(testUser);
        when(messageRepository.findDistinctChatPartnerIds(testUser.getId())).thenReturn(Arrays.asList(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(messageRepository.hasUnreadMessages(2L, testUser.getId())).thenReturn(true);

        List<ContactDTO> result = messageService.getChatContacts(validToken);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("otherUser", result.get(0).getUsername());
        assertTrue(result.get(0).isHasUnread());
    }

    @Test
    public void testGetChatContacts_userNotFound() {
        when(userService.getUserByToken(validToken)).thenReturn(testUser);
        when(messageRepository.findDistinctChatPartnerIds(testUser.getId())).thenReturn(Arrays.asList(2L));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        List<ContactDTO> result = messageService.getChatContacts(validToken);

        assertEquals(0, result.size());
    }

    @Test
    public void testChat_success() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSenderId(1L);
        messageDTO.setRecipientId(2L);
        messageDTO.setContent("Hello world");
        messageDTO.setRead(false);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        messageService.chat(messageDTO);

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    public void testChat_sameSenderAndRecipient_throwsBadRequest() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSenderId(1L);
        messageDTO.setRecipientId(1L);
        messageDTO.setContent("Hello world");

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            messageService.chat(messageDTO);
        });

        assertTrue(exception.getMessage().contains("Sender and recipient cannot be the same user"));
    }

    @Test
    public void testPoll_success() {
        Long userId = 1L;

        DeferredResult<String> result = messageService.poll(userId);

        assertNotNull(result);
    }

    @Test
    public void testGetUnreadMessages_success() {
        Long userId = 1L;
        List<Message> unreadMessages = Arrays.asList(testMessage);

        when(messageRepository.findByRecipientIdAndIsReadFalse(userId)).thenReturn(unreadMessages);

        List<Message> result = messageService.getUnreadMessages(userId);

        assertEquals(1, result.size());
        assertEquals(testMessage, result.get(0));
    }

    @Test
    public void testSendMessage_success() {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSenderId(1L);
        messageDTO.setRecipientId(2L);
        messageDTO.setContent("Test message");

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setTimestamp(LocalDateTime.now());
            return saved;
        });

        MessageDTO result = messageService.sendMessage(messageDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getRecipientId());
        assertEquals("Test message", result.getContent());
        assertFalse(result.isRead());
        assertNotNull(result.getTimestamp());
    }
}