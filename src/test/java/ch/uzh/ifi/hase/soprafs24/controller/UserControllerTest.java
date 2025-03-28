package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

//    @Test
//    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
//        // given
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("username");
//        user.setPassword("password");
//        user.setToken("1");
//        user.setStatus(UserStatus.OFFLINE);
//        user.setCreationDate(LocalDate.now());
//        user.setBirthday(LocalDate.of(2000, 1, 1));
//
//        List<User> allUsers = Collections.singletonList(user);
//
//        // this mocks the UserService -> we define above what the userService should
//        // return when getUsers() is called
//        given(userService.getUsers()).willReturn(allUsers);
//
//        // when
//        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);
//
//        // then
//        mockMvc.perform(getRequest).andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
//                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())))
//                .andExpect(jsonPath("$[0].id", is(user.getId().intValue())))
//                .andExpect(jsonPath("$[0].creationDate", is(user.getCreationDate().toString())))
//                .andExpect(jsonPath("$[0].birthday", is(user.getBirthday().toString())));
//    }
//
//    @Test
//    public void createUser_validInput_userCreated() throws Exception {
//        // given
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("testUsername");
//        user.setPassword("password");
//        user.setStatus(UserStatus.OFFLINE);
//        user.setCreationDate(LocalDate.now());
//        user.setBirthday(null);
//
//
//        UserPostDTO userPostDTO = new UserPostDTO();
//        userPostDTO.setUsername("testUsername");
//        userPostDTO.setPassword("password");
//
//        given(userService.createUser(Mockito.any())).willReturn(user);
//
//        // when/then -> do the request + validate the result
//        MockHttpServletRequestBuilder postRequest = post("/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(userPostDTO));
//
//        // then
//        mockMvc.perform(postRequest)
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
//                .andExpect(jsonPath("$.username", is(user.getUsername())))
//                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
//                .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
//                .andExpect(jsonPath("$.birthday", is(user.getBirthday())));
//    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     *
     * @param object
     * @return string
     */
//    private String asJsonString(final Object object) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.registerModule(new JavaTimeModule());
//            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//            return mapper.writeValueAsString(object);
//        }
//        catch (JsonProcessingException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                    String.format("The request body could not be created.%s", e.toString()));
//        }
//    }
//
//    @Test
//    public void createUser_usernameAlreadyExists_conflict() throws Exception {
//        UserPostDTO userPostDTO = new UserPostDTO();
//        userPostDTO.setUsername("testUsername");
//        userPostDTO.setPassword("password");
//
//        // for duplicate username
//        given(userService.createUser(Mockito.any()))
//                .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "The username(testUsername) already exist."));
//
//        // When/Then -> perform the POST request and assert the conflict error
//        MockHttpServletRequestBuilder postRequest = post("/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(asJsonString(userPostDTO));
//
//        // Then
//        mockMvc.perform(postRequest)
//                .andExpect(status().isConflict()) // 409 Conflict
//                .andExpect(jsonPath("$.message", is("The username(testUsername) already exist.")));
//
//    }
//
//
//    @Test
//    public void getUser_validInput_userFound() throws Exception {
//        Long userId = 1L;
//        User user = new User();
//        user.setId(userId);
//        user.setUsername("testUsername");
//        user.setPassword("password");
//        user.setStatus(UserStatus.ONLINE);
//        user.setCreationDate(LocalDate.now());
//        user.setBirthday(LocalDate.of(2000, 1, 1));
//
//
//        given(userService.getUserById(userId)).willReturn(user);
//        MockHttpServletRequestBuilder getRequest = get("/users/" + userId).contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(getRequest)
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
//                .andExpect(jsonPath("$.username", is(user.getUsername())))
//                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
//                .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
//                .andExpect(jsonPath("$.birthday", is(user.getBirthday().toString())));
//    }
//
//    @Test
//    public void getUser_validInput_userNotFound() throws Exception {
//        Long userId = 1L;
//
//        given(userService.getUserById(userId)).
//                willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The user with id: " + userId + " was not found"));
//        MockHttpServletRequestBuilder getRequest = get("/users/" + userId).contentType(MediaType.APPLICATION_JSON);
//        mockMvc.perform(getRequest)
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message", is("The user with id: " + userId + " was not found")));
//    }

//    @Test
//    public void updateUser_validInput_userFound() throws Exception {
//        Long userId = 1L;
//
//        UserPutDTO userPutDTO = new UserPutDTO();
//        userPutDTO.setUsername("updatedUsername");
//        userPutDTO.setBirthday(LocalDate.of(2000, 1, 1));
//
//        Mockito.doNothing().when(userService).updateUser(Mockito.eq(userId), Mockito.any(UserPutDTO.class));
//        System.out.println(asJsonString(userPutDTO));
//
//        MockHttpServletRequestBuilder putRequest = put("/users/" + userId).contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPutDTO));
//        mockMvc.perform(putRequest)
//                .andExpect(status().isNoContent());
//    }

//    @Test
//    public void updateUser_validInput_userNotFound() throws Exception {
//        Long userId = 1L;
//        UserPutDTO userPutDTO = new UserPutDTO();
//        userPutDTO.setUsername("updatedUsername");
//        userPutDTO.setBirthday(LocalDate.of(2000, 1, 1));
//
//        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " was not found"))
//                .when(userService).updateUser(Mockito.eq(userId), Mockito.any(UserPutDTO.class));
//
//        MockHttpServletRequestBuilder putRequest = put("/users/" + userId)
//                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(userPutDTO));
//        mockMvc.perform(putRequest)
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message", is("User with ID " + userId + " was not found")));
//    }
}

