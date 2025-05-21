package ch.uzh.ifi.hase.soprafs24.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable the filter
public class MessageControllerTest {

    private static final String AUTH_HEADER = "token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthFilter authFilter;

    @Test
    void getConversationMessages_success() throws Exception {
        // Prepare message data
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("Alice");

        User recipient = new User();
        recipient.setId(2L);
        recipient.setUsername("Bob");

        Message message1 = new Message();
        message1.setId(1L);
        message1.setSenderId(1L);
        message1.setRecipientId(2L);
        message1.setContent("Hello, World!");
        message1.setTimestamp(LocalDateTime.now());
        message1.setRead(true);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSenderId(2L);
        message2.setRecipientId(1L);
        message2.setContent("Hi there!");
        message2.setTimestamp(LocalDateTime.now());
        message2.setRead(false);

        // Mock the service call to get the conversation
        when(messageService.getConversation(1L, 2L)).thenReturn(List.of(message1, message2));

        // Perform the GET request to the endpoint and verify the response
        mockMvc.perform(get("/messages/conversation/{senderId}/{recipientId}", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value(1))
                .andExpect(jsonPath("$[0].recipientId").value(2))
                .andExpect(jsonPath("$[0].content").value("Hello, World!"))
                .andExpect(jsonPath("$[0].read").value(true))
                .andExpect(jsonPath("$[1].senderId").value(2))
                .andExpect(jsonPath("$[1].recipientId").value(1))
                .andExpect(jsonPath("$[1].content").value("Hi there!"))
                .andExpect(jsonPath("$[1].read").value(false));
    }

    @Test
    void getChatContacts_success() throws Exception {
        // Prepare the mock data
        Long userId = 1L;
        String token = "validToken";

        // Mock User object
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setPassword("testPassword");
        user.setToken(token);
        user.setEmail("test@test.com");

        // Mock ContactDTO objects
        ContactDTO contact1 = new ContactDTO(2L, "contact1", "Last message 1");
        ContactDTO contact2 = new ContactDTO(3L, "contact2", "Last message 2");

        // Mock the service calls
        when(userService.getUserByToken(token)).thenReturn(user);
        when(messageService.getChatContacts(token)).thenReturn(Arrays.asList(contact1, contact2));

        mockMvc.perform(get("/messages/contacts")
                        .header("token", token))  // Simulate the Authorization header
                .andExpect(status().isOk())  // Verify that the response status is OK
                .andExpect(jsonPath("$[0].id").value(2L))  // Verify the first contact's id
                .andExpect(jsonPath("$[0].username").value("contact1"))  // Verify the first contact's username
                .andExpect(jsonPath("$[0].lastMessage").value("Last message 1"))  // Verify the first contact's lastMessage
                .andExpect(jsonPath("$[1].id").value(3L))  // Verify the second contact's id
                .andExpect(jsonPath("$[1].username").value("contact2"))  // Verify the second contact's username
                .andExpect(jsonPath("$[1].lastMessage").value("Last message 2"));  // Verify the second contact's lastMessage
    }

    @Test
    void poll_returnsDeferredResult() throws Exception {
        Long userId = 1L;
        DeferredResult<String> mockResult = new DeferredResult<>();
        mockResult.setResult("someData");

        when(messageService.poll(userId)).thenReturn(mockResult);

        mockMvc.perform(get("/messages/poll/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void sendMessage_success() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setSenderId(1L);
        messageDTO.setRecipientId(2L);
        messageDTO.setContent("Hello test!");

        mockMvc.perform(post("/messages/send")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(messageDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testMarkMessageAsRead_success() throws Exception {
        Long senderId = 1L;
        Long recipientId = 2L;

        doNothing().when(messageService).markMessageAsRead(senderId, recipientId);

        mockMvc.perform(put("/messages/mark-read/{senderId}/{recipientId}", senderId, recipientId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testMarkMessageAsRead_serviceThrowsException() throws Exception {
        Long senderId = 1L;
        Long recipientId = 2L;

        String errorMessage = "No messages found";
        HttpStatus status = HttpStatus.NOT_FOUND;

        doThrow(new ResponseStatusException(status, errorMessage))
                .when(messageService).markMessageAsRead(senderId, recipientId);

        mockMvc.perform(put("/messages/mark-read/{senderId}/{recipientId}", senderId, recipientId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void testHasUnreadMessages_success() throws Exception {
        String token = "token";
        Map<String, Boolean> responseMap = new HashMap<>();
        responseMap.put("hasUnread", true);

        // Mock service method to return the expected result
        when(messageService.hasUnreadMessage(token)).thenReturn(responseMap);

        mockMvc.perform(get("/messages/unread").header(AUTH_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasUnread").value(true));  // Expecting "hasUnread": true
    }

    @Test
    public void testHasUnreadMessages_serviceThrowsException() throws Exception {
        String token = "invalid-token";
        String errorMessage = "User not found";

        // Mock service method to throw a ResponseStatusException
        when(messageService.hasUnreadMessage(token))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage));

        mockMvc.perform(get("/messages/unread")
                        .header(AUTH_HEADER, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));  // Expecting the error message in the response
    }

    @Test
    public void testGetConversation_invalidSenderAndRecipient() throws Exception {
        Long senderId = 1L;
        Long recipientId = 1L;  // Same sender and recipient, should throw exception

        // Mock service method to throw ResponseStatusException
        when(messageService.getConversation(senderId, recipientId))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot get conversation with self"));

        mockMvc.perform(get("/messages/conversation/{senderId}/{recipientId}", senderId, recipientId))
                .andExpect(status().isBadRequest())  // Expect 400 Bad Request
                .andExpect(jsonPath("$.message").value("Cannot get conversation with self"));  // Expect the error message in the response
    }

    @Test
    public void testGetChatContacts_serviceThrowsException() throws Exception {
        String token = "valid-token";
        String errorMessage = "Invalid token";

        when(messageService.getChatContacts(token))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage));

        mockMvc.perform(get("/messages/contacts")
                        .header(AUTH_HEADER, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }


}