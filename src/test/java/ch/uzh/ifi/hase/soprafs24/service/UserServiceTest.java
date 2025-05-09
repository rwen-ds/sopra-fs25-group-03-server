package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

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
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password");

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(existingUser);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(newUser);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void testCreateUser_conflict_email() {

        User newUser = new User();
        newUser.setUsername("uniqueuser");
        newUser.setEmail("duplicate@example.com");
        newUser.setPassword("password");

        when(userRepository.findByUsername("uniqueuser")).thenReturn(null);
        User existingUser = new User();
        existingUser.setId(11L);
        existingUser.setEmail("duplicate@example.com");
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(existingUser);

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
        user.setUsername("testuser");
        user.setPassword("password");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("oldToken");

        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        User result = userService.login(user);


        assertEquals("testuser", result.getUsername());
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
        // 创建测试输入
        User userInput = new User();
        userInput.setUsername("testuser");
        userInput.setPassword("wrongpassword");

        // 创建存储在数据库中的用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setPassword("password");

        // 设置Mock行为
        when(userRepository.findByUsername(userInput.getUsername())).thenReturn(existingUser);

        // 执行测试并验证异常
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
        user.setUsername("testuser");
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
}