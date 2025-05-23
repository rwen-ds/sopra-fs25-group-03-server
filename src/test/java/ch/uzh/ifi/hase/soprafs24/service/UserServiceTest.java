package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.UserGender;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private RequestRepository requestRepository;


    @Test
    public void testGetUsers_success() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        var users = userService.getUsers();
        assertEquals(2, users.size());
    }

    @Test
    public void testCreateUser_success() {
        User newUser = new User();
        newUser.setUsername("testUsername");
        newUser.setPassword("password");
        newUser.setEmail("test@edu.example.com");

        when(userRepository.findByUsername("testUsername")).thenReturn(null);
        when(userRepository.findByEmail("test@edu.example.com")).thenReturn(null);

        when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return userArg;
        });

        // when
        User result = userService.createUser(newUser);

        // then
        assertNotNull(result.getId());
        assertNotNull(result.getToken());
        assertEquals(UserStatus.ONLINE, result.getStatus());
        assertEquals(LocalDate.now(), result.getCreationDate());
        verify(userRepository, times(1)).flush();
    }

    @Test
    public void testCreateUser_conflict_username() {

        User newUser = new User();
        newUser.setUsername("testUsername");
        newUser.setPassword("password");
        newUser.setEmail("test@edu.example.com");

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("testUsername");
        when(userRepository.findByUsername("testUsername")).thenReturn(existingUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(newUser);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testCreateUser_conflict_email() {

        User newUser = new User();
        newUser.setUsername("testUsername");
        newUser.setPassword("password");
        newUser.setEmail("test@edu.example.com");

        when(userRepository.findByUsername("testUsername")).thenReturn(null);
        User existingUser = new User();
        existingUser.setId(11L);
        existingUser.setUsername("testUsername");
        existingUser.setEmail("test@edu.example.com");
        when(userRepository.findByEmail("test@edu.example.com")).thenReturn(existingUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(newUser);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testGetUserById_success() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetUserById_notFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserById(99L);
        });
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    public void testLogin_success() {

        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setPassword("password");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("oldToken");

        when(userRepository.findByUsername("testUsername")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        User result = userService.login(user);


        assertEquals("testUsername", result.getUsername());
        assertEquals("password", result.getPassword());
        assertEquals(UserStatus.ONLINE, result.getStatus());

        assertNotNull(result.getToken());
        assertNotEquals("oldToken", result.getToken());
        verify(userRepository, times(1)).flush();
    }

    @Test
    public void testLogin_userNotFound() {

        User userInput = new User();
        userInput.setUsername("unknown");
        userInput.setPassword("password");
        when(userRepository.findByUsername("unknown")).thenReturn(null);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.login(userInput);
        });
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    public void testLogin_invalidPassword() {
        User userInput = new User();
        userInput.setUsername("testUsername");
        userInput.setPassword("wrongpassword");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testUsername");
        existingUser.setPassword("password");

        when(userRepository.findByUsername(userInput.getUsername())).thenReturn(existingUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.login(userInput);
        });
        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    public void testLogout_success() {

        User user = new User();
        user.setId(1L);
        user.setToken("validToken");
        user.setStatus(UserStatus.ONLINE);
        when(userRepository.findByToken("validToken")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.logout("validToken");

        assertEquals(UserStatus.OFFLINE, user.getStatus());
        verify(userRepository, times(1)).flush();
    }

    @Test
    public void testLogout_invalidToken() {
        when(userRepository.findByToken("invalidToken")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.logout("invalidToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testGetUserByToken_success() {
        User user = new User();
        user.setId(1L);
        user.setToken("token123");
        when(userRepository.findByToken("token123")).thenReturn(user);

        User result = userService.getUserByToken("token123");
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetUserByToken_notFound() {
        when(userRepository.findByToken("notFoundToken")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserByToken("notFoundToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testDeleteUser_success() {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.findByToken("validToken")).thenReturn(user);

        userService.deleteUser(1L, "validToken");

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void testDeleteUser_unauthorized() {

        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setUsername("currentUser");
        when(userRepository.findByToken("token")).thenReturn(currentUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(1L, "token");
        });
        assertTrue(exception.getMessage().contains("unauthorized user"));
    }

    @Test
    public void testDeleteUser_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(1L, "token");
        });
        assertTrue(exception.getMessage().contains("was not found"));
    }

    @Test
    public void testCreateUser_adminUsername_throwsBadRequest() {
        User newUser = new User();
        newUser.setUsername("admin");
        newUser.setPassword("password");
        newUser.setEmail("admin@edu.example.com");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(newUser);
        });
        assertTrue(exception.getMessage().contains("reserved and cannot be used"));
    }

    @Test
    public void testCreateUser_invalidEmail_throwsUnprocessableEntity() {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setPassword("password");
        newUser.setEmail("invalid@gmail.com");

        when(userRepository.findByUsername("testUser")).thenReturn(null);
        when(userRepository.findByEmail("invalid@gmail.com")).thenReturn(null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(newUser);
        });
        assertTrue(exception.getMessage().contains("Only student email addresses"));
    }

    @Test
    public void testCreateUser_validUzhEmail_success() {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setPassword("password");
        newUser.setEmail("test@uzh.ch");

        when(userRepository.findByUsername("testUser")).thenReturn(null);
        when(userRepository.findByEmail("test@uzh.ch")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return userArg;
        });

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("test@uzh.ch", result.getEmail());
    }

    @Test
    public void testCreateUser_validEthzEmail_success() {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setPassword("password");
        newUser.setEmail("test@ethz.ch");

        when(userRepository.findByUsername("testUser")).thenReturn(null);
        when(userRepository.findByEmail("test@ethz.ch")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return userArg;
        });

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("test@ethz.ch", result.getEmail());
    }

    @Test
    public void testCreateUser_validStuEmail_success() {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setPassword("password");
        newUser.setEmail("test@student.ch");

        when(userRepository.findByUsername("testUser")).thenReturn(null);
        when(userRepository.findByEmail("test@student.ch")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return userArg;
        });

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("test@student.ch", result.getEmail());
    }

    @Test
    public void testUpdateUser_success() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("old@edu.example.com");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");
        userPutDTO.setEmail("new@edu.example.com");
        userPutDTO.setAge(25);

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("newUsername")).thenReturn(null);
        when(userRepository.findByEmail("new@edu.example.com")).thenReturn(null);

        userService.updateUser(1L, userPutDTO, "validToken");

        assertEquals("newUsername", existingUser.getUsername());
        assertEquals("new@edu.example.com", existingUser.getEmail());
        assertEquals(25, existingUser.getAge());
        verify(userRepository).save(existingUser);
        verify(userRepository).flush();
    }

    @Test
    public void testUpdateUser_adminCanUpdateOtherUser() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setToken("adminToken");

        User targetUser = new User();
        targetUser.setId(1L);
        targetUser.setUsername("targetUser");
        targetUser.setEmail("target@edu.example.com");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("updatedUsername");

        when(userRepository.findByToken("adminToken")).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByUsername("updatedUsername")).thenReturn(null);

        userService.updateUser(1L, userPutDTO, "adminToken");

        assertEquals("updatedUsername", targetUser.getUsername());
        verify(userRepository).save(targetUser);
    }

    @Test
    public void testUpdateUser_unauthorized() {
        User loginUser = new User();
        loginUser.setId(2L);
        loginUser.setUsername("otherUser");
        loginUser.setToken("validToken");

        User targetUser = new User();
        targetUser.setId(1L);
        targetUser.setUsername("targetUser");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("newUsername");

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(1L, userPutDTO, "validToken");
        });
        assertTrue(exception.getMessage().contains("unauthorized user"));
    }

    @Test
    public void testUpdateUser_conflictUsername() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldUsername");

        User conflictUser = new User();
        conflictUser.setId(2L);
        conflictUser.setUsername("conflictUsername");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("conflictUsername");

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("conflictUsername")).thenReturn(conflictUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(1L, userPutDTO, "validToken");
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testUpdateUser_conflictEmail() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@edu.example.com");

        User conflictUser = new User();
        conflictUser.setId(2L);
        conflictUser.setEmail("conflict@edu.example.com");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setEmail("conflict@edu.example.com");

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("conflict@edu.example.com")).thenReturn(conflictUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(1L, userPutDTO, "validToken");
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testUpdateUser_nullFields() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testUser");
        existingUser.setAge(30);
        existingUser.setBirthday(LocalDate.of(1990, 1, 1));
        existingUser.setGender(UserGender.MALE);
        existingUser.setLanguage("English");

        UserPutDTO userPutDTO = new UserPutDTO();
        // All fields null

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, userPutDTO, "validToken");

        assertNull(existingUser.getAge());
        assertNull(existingUser.getBirthday());
        assertNull(existingUser.getGender());
        assertNull(existingUser.getLanguage());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void testUpdateUser_sameUsernameNoChange() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testUser");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("testUser"); // Same username

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, userPutDTO, "validToken");

        assertEquals("testUser", existingUser.getUsername());
        verify(userRepository).save(existingUser);
        verify(userRepository, times(0)).findByUsername("testUser"); // Should not check for conflict
    }

    @Test
    public void testUpdateUser_sameEmailNoChange() {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testUser");
        loginUser.setToken("validToken");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@edu.example.com");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setEmail("test@edu.example.com"); // Same email

        when(userRepository.findByToken("validToken")).thenReturn(loginUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, userPutDTO, "validToken");

        assertEquals("test@edu.example.com", existingUser.getEmail());
        verify(userRepository).save(existingUser);
        verify(userRepository, times(0)).findByEmail("test@edu.example.com"); // Should not check for conflict
    }

    @Test
    public void testDeleteUser_adminCanDeleteAnyUser() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setToken("adminToken");

        User targetUser = new User();
        targetUser.setId(1L);
        targetUser.setUsername("targetUser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));
        when(userRepository.findByToken("adminToken")).thenReturn(adminUser);
        when(requestRepository.findByVolunteer(targetUser)).thenReturn(Arrays.asList());

        userService.deleteUser(1L, "adminToken");

        verify(userRepository).delete(targetUser);
    }

    @Test
    public void testDeleteUser_withVolunteerRequests() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setToken("validToken");

        ch.uzh.ifi.hase.soprafs24.entity.Request request1 = new ch.uzh.ifi.hase.soprafs24.entity.Request();
        request1.setId(1L);
        request1.setVolunteer(user);
        request1.setStatus(ch.uzh.ifi.hase.soprafs24.constant.RequestStatus.VOLUNTEERED);

        ch.uzh.ifi.hase.soprafs24.entity.Request request2 = new ch.uzh.ifi.hase.soprafs24.entity.Request();
        request2.setId(2L);
        request2.setVolunteer(user);
        request2.setStatus(ch.uzh.ifi.hase.soprafs24.constant.RequestStatus.ACCEPTING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByToken("validToken")).thenReturn(user);
        when(requestRepository.findByVolunteer(user)).thenReturn(Arrays.asList(request1, request2));

        userService.deleteUser(1L, "validToken");

        // Verify requests are updated
        assertNull(request1.getVolunteer());
        assertEquals(ch.uzh.ifi.hase.soprafs24.constant.RequestStatus.WAITING, request1.getStatus());
        assertNull(request2.getVolunteer());
        assertEquals(ch.uzh.ifi.hase.soprafs24.constant.RequestStatus.WAITING, request2.getStatus());

        verify(requestRepository, times(2)).save(any(ch.uzh.ifi.hase.soprafs24.entity.Request.class));
        verify(userRepository).delete(user);
    }
}