package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UserService userService;

    @MockBean
    private AuthFilter authFilter;

    @Test
    void getConversationMessages_success() throws Exception {
        // Prepare message data
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
                .andExpect(status().isOk()) // Verify that the response status is OK
                .andExpect(jsonPath("$[0].id").value(1)) // Verify the first message's ID
                .andExpect(jsonPath("$[0].content").value("Hello, World!")) // Verify the first message's content
                .andExpect(jsonPath("$[1].id").value(2)) // Verify the second message's ID
                .andExpect(jsonPath("$[1].content").value("Hi there!")); // Verify the second message's content
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
        when(messageService.getChatContacts(userId)).thenReturn(Arrays.asList(contact1, contact2));

        mockMvc.perform(get("/messages/contacts")
                        .header("Authorization", token))  // Simulate the Authorization header
                .andExpect(status().isOk())  // Verify that the response status is OK
                .andExpect(jsonPath("$[0].id").value(2L))  // Verify the first contact's id
                .andExpect(jsonPath("$[0].username").value("contact1"))  // Verify the first contact's username
                .andExpect(jsonPath("$[0].lastMessage").value("Last message 1"))  // Verify the first contact's lastMessage
                .andExpect(jsonPath("$[1].id").value(3L))  // Verify the second contact's id
                .andExpect(jsonPath("$[1].username").value("contact2"))  // Verify the second contact's username
                .andExpect(jsonPath("$[1].lastMessage").value("Last message 2"));  // Verify the second contact's lastMessage
    }
}