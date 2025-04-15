package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  private static final String AUTH_HEADER = "Authorization";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockBean
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @Autowired
  private MockMvc mockMvc;

  private String asJsonString(final Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "The request body could not be created: " + e.toString());
    }
  }

  private User createSampleUser(Long id, String username, String token) {
    User user = new User();
    user.setId(id);
    user.setUsername(username);
    user.setEmail(username + "@example.com");
    user.setPassword("password");
    user.setCreationDate(java.time.LocalDate.now());
    user.setToken(token);
    user.setStatus(UserStatus.OFFLINE);
    return user;
  }

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    User user = createSampleUser(1L, "user1", "token1");
    user.setStatus(UserStatus.OFFLINE);
    List<User> allUsers = Collections.singletonList(user);

    when(userService.getUsers()).thenReturn(allUsers);

    MockHttpServletRequestBuilder getRequest = get("/users")
        .header(AUTH_HEADER, "anyToken")
        .contentType(MediaType.APPLICATION_JSON);

    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is("user1")))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void getUserById_userFound_returns200() throws Exception {
    User user = createSampleUser(2L, "user2", "token2");
    when(userService.getUserById(2L)).thenReturn(user);

    MockHttpServletRequestBuilder getRequest = get("/users/2")
        .contentType(MediaType.APPLICATION_JSON);

    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())));
  }

  @Test
  public void getUserById_userNotFound_returns404() throws Exception {
    Long invalidId = 999L;
    when(userService.getUserById(invalidId))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + invalidId + " was not found"));

    MockHttpServletRequestBuilder getRequest = get("/users/" + invalidId)
        .contentType(MediaType.APPLICATION_JSON);

    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("User with id " + invalidId + " was not found")));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
    userPostDTO.setEmail("testUsername@example.com");
    userPostDTO.setPassword("password");

    User createdUser = createSampleUser(3L, "testUsername", "token3");
    createdUser.setStatus(UserStatus.ONLINE);
    when(userService.createUser(Mockito.any())).thenReturn(createdUser);

    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(createdUser.getId().intValue())))
        .andExpect(jsonPath("$.username", is(createdUser.getUsername())))
        .andExpect(jsonPath("$.status", is(createdUser.getStatus().toString())))
        .andExpect(result -> {
          String authHeader = result.getResponse().getHeader(AUTH_HEADER);
          if (!"token3".equals(authHeader)) {
            throw new AssertionError("Authorization header does not match token");
          }
        });
  }

  @Test
  public void createUser_duplicateUsername_returns409() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("duplicateUsername");
    userPostDTO.setEmail("duplicate@example.com");
    userPostDTO.setPassword("password");

    when(userService.createUser(Mockito.any()))
        .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "User with this email/username already exists"));

    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message", is("User with this email/username already exists")));
  }

  @Test
  public void loginUser_validCredentials_returns200() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("loginUser");
    userPostDTO.setPassword("password");

    User loggedInUser = createSampleUser(4L, "loginUser", "tokenLogin");
    loggedInUser.setStatus(UserStatus.ONLINE);
    when(userService.login(Mockito.any())).thenReturn(loggedInUser);

    MockHttpServletRequestBuilder postRequest = post("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(loggedInUser.getId().intValue())))
        .andExpect(jsonPath("$.username", is(loggedInUser.getUsername())))
        .andExpect(result -> {
          String authHeader = result.getResponse().getHeader(AUTH_HEADER);
          if (!"tokenLogin".equals(authHeader)) {
            throw new AssertionError("Authorization header does not match token");
          }
        });
  }

  @Test
  public void loginUser_incorrectCredentials_returns401() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("loginUser");
    userPostDTO.setPassword("wrongPassword");

    when(userService.login(Mockito.any()))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect credentials"));

    MockHttpServletRequestBuilder postRequest = post("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message", is("Incorrect credentials")));
  }

  @Test
  public void logoutUser_validToken_returns204() throws Exception {
    doNothing().when(userService).logout("validLogoutToken");

    MockHttpServletRequestBuilder putRequest = put("/users/logout")
        .header(AUTH_HEADER, "validLogoutToken");

    mockMvc.perform(putRequest)
        .andExpect(status().isNoContent());
  }

  @Test
  public void logoutUser_invalidToken_returns404() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token"))
        .when(userService).logout("invalidToken");

    MockHttpServletRequestBuilder putRequest = put("/users/logout")
        .header(AUTH_HEADER, "invalidToken");

    mockMvc.perform(putRequest)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("Invalid token")));
  }

  @Test
  public void getUserMe_validToken_returns200() throws Exception {
    User currentUser = createSampleUser(5L, "currentUser", "tokenMe");
    when(userService.getUserByToken("tokenMe")).thenReturn(currentUser);

    MockHttpServletRequestBuilder getRequest = get("/users/me")
        .header(AUTH_HEADER, "tokenMe");

    mockMvc.perform(getRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(currentUser.getId().intValue())))
        .andExpect(jsonPath("$.username", is(currentUser.getUsername())));
  }

  @Test
  public void getUserMe_invalidToken_returns404() throws Exception {
    when(userService.getUserByToken("badToken"))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user"));

    MockHttpServletRequestBuilder getRequest = get("/users/me")
        .header(AUTH_HEADER, "badToken");

    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("Invalid user")));
  }

  @Test
  public void updateUser_validInput_returns204() throws Exception {
    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("updatedUsername");
    String jsonInput = asJsonString(userPutDTO);

    doNothing().when(userService).updateUser(eq(1L), any(UserPutDTO.class), eq("validToken"));

    MockHttpServletRequestBuilder putRequest = put("/users/1")
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTH_HEADER, "validToken")
        .content(jsonInput);

    mockMvc.perform(putRequest)
        .andExpect(status().isNoContent());
  }

  @Test
  public void updateUser_userNotFound_returns404() throws Exception {
    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("updatedUsername");
    String jsonInput = asJsonString(userPutDTO);

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id 10 was not found"))
        .when(userService).updateUser(eq(10L), any(UserPutDTO.class), eq("someToken"));

    MockHttpServletRequestBuilder putRequest = put("/users/10")
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTH_HEADER, "someToken")
        .content(jsonInput);

    mockMvc.perform(putRequest)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("User with id 10 was not found")));
  }

  @Test
  public void updateUser_forbidden_returns403() throws Exception {
    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("updatedUsername");
    String jsonInput = asJsonString(userPutDTO);

    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit another user’s profile"))
        .when(userService).updateUser(eq(5L), any(UserPutDTO.class), eq("badToken"));

    MockHttpServletRequestBuilder putRequest = put("/users/5")
        .contentType(MediaType.APPLICATION_JSON)
        .header(AUTH_HEADER, "badToken")
        .content(jsonInput);

    mockMvc.perform(putRequest)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message", is("Cannot edit another user’s profile")));
  }

  @Test
  public void deleteUser_valid_returns200() throws Exception {
    doNothing().when(userService).deleteUser(7L, "validToken");

    MockHttpServletRequestBuilder deleteRequest = delete("/users/7")
        .header(AUTH_HEADER, "validToken");

    mockMvc.perform(deleteRequest)
        .andExpect(status().isOk());
  }

  @Test
  public void deleteUser_notAuthorized_returns403() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete"))
        .when(userService).deleteUser(8L, "badToken");

    MockHttpServletRequestBuilder deleteRequest = delete("/users/8")
        .header(AUTH_HEADER, "badToken");

    mockMvc.perform(deleteRequest)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message", is("Not authorized to delete")));
  }

  @Test
  public void deleteUser_notFound_returns404() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id 9 was not found"))
        .when(userService).deleteUser(9L, "validToken");

    MockHttpServletRequestBuilder deleteRequest = delete("/users/9")
        .header(AUTH_HEADER, "validToken");

    mockMvc.perform(deleteRequest)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message", is("User with id 9 was not found")));
  }
}