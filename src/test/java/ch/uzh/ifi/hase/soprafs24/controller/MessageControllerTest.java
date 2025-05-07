package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable the filter
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MessageRepository messageRepository;

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
        message1.setSender(sender);
        message1.setRecipient(recipient);
        message1.setContent("Hello, World!");
        message1.setTimestamp(LocalDateTime.now());
        message1.setRead(true);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(recipient);
        message2.setRecipient(sender);
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
}