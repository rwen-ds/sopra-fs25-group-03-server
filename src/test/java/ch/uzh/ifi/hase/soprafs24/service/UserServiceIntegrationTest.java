package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;


@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;


    @Test
    public void testGetUsersIntegration_success() {

        User user1 = new User();
        user1.setUsername("integrationUser1");
        user1.setEmail("integration1@edu.example.com");
        user1.setPassword("password1");
        User user2 = new User();
        user2.setUsername("integrationUser2");
        user2.setEmail("integration2@edu.example.com");
        user2.setPassword("password2");

        userService.createUser(user1);
        userService.createUser(user2);

        var users = userService.getUsers();
        assertEquals(3, users.size());

    }

    @Test
    public void testCreateUserIntegration_success() {

        User newUser = new User();
        newUser.setUsername("integrationUser");
        newUser.setEmail("integration@edu.example.com");
        newUser.setPassword("password");


        User createdUser = userService.createUser(newUser);


        assertNotNull(createdUser.getId(), "Id not found");
        assertNotNull(createdUser.getToken(), "Token not found");
        assertEquals(UserStatus.ONLINE, createdUser.getStatus(), "User status should be ONLINE");
        assertEquals(LocalDate.now(), createdUser.getCreationDate(), "Creation not correct");


        Optional<User> found = userRepository.findById(createdUser.getId());
        assertTrue(found.isPresent(), "DB should find user");
        assertEquals("integrationUser", found.get().getUsername());
    }

    @Test
    public void testCreateUser_conflictUsername() {
        User user1 = new User();
        user1.setUsername("conflictUser");
        user1.setEmail("unique1@edu.example.com");
        user1.setPassword("password");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("conflictUser");
        user2.setEmail("unique2@edu.example.com");
        user2.setPassword("password");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(user2);
        });
        assertTrue(ex.getMessage().contains("already exists"), "Expected username conflict");
    }

    @Test
    public void testCreateUser_conflictEmail() {
        User user1 = new User();
        user1.setUsername("uniqueUser1");
        user1.setEmail("conflict@edu.example.com");
        user1.setPassword("password");
        userService.createUser(user1);

        User user2 = new User();
        user2.setUsername("uniqueUser2");
        user2.setEmail("conflict@edu.example.com");
        user2.setPassword("password");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.createUser(user2);
        });
        assertTrue(ex.getMessage().contains("already exists"), "Expected email conflict");
    }

    @Test
    public void testGetUserByIdIntegration_success() {
        User newUser = new User();
        newUser.setUsername("getUser");
        newUser.setEmail("getuser@edu.example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);

        User fetchedUser = userService.getUserById(createdUser.getId());
        assertEquals(createdUser.getId(), fetchedUser.getId());
    }

    @Test
    public void testGetUserById_notFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserById(999L);
        });
        assertTrue(ex.getMessage().contains("was not found"));
    }

    @Test
    public void testUpdateUser_userNotFound() {
        User user = new User();
        user.setUsername("updateNonExist");
        user.setEmail("updateNonExist@edu.example.com");
        user.setPassword("password");
        User createdUser = userService.createUser(user);

        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO putDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        putDTO.setUsername("changedName");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(9999L, putDTO, createdUser.getToken());
        });
        assertTrue(ex.getMessage().contains("was not found"));
    }

    @Test
    public void testUpdateUser_conflictUsername() {
        User userA = new User();
        userA.setUsername("userA_conflict");
        userA.setEmail("userA_conflict@edu.example.com");
        userA.setPassword("password");
        User createdA = userService.createUser(userA);

        User userB = new User();
        userB.setUsername("userB_conflict");
        userB.setEmail("userB_conflict@edu.example.com");
        userB.setPassword("password");
        User createdB = userService.createUser(userB);

        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO putDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        putDTO.setUsername("userA_conflict");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(createdB.getId(), putDTO, createdB.getToken());
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    public void testUpdateUser_conflictEmail() {
        User userA = new User();
        userA.setUsername("userA_conflictEmail");
        userA.setEmail("userA_conflictEmail@edu.example.com");
        userA.setPassword("password");
        User createdA = userService.createUser(userA);

        User userB = new User();
        userB.setUsername("userB_conflictEmail");
        userB.setEmail("userB_conflictEmail@edu.example.com");
        userB.setPassword("password");
        User createdB = userService.createUser(userB);

        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO putDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        putDTO.setEmail("userA_conflictEmail@edu.example.com");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(createdB.getId(), putDTO, createdB.getToken());
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    public void testLoginIntegration_success() {

        User newUser = new User();
        newUser.setUsername("loginUser");
        newUser.setEmail("loginuser@edu.example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);
        String oldToken = createdUser.getToken();


        userService.logout(createdUser.getToken());


        User loginInput = new User();
        loginInput.setUsername("loginUser");
        loginInput.setPassword("password");

        User loggedInUser = userService.login(loginInput);


        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
        assertNotNull(loggedInUser.getToken());
        assertNotEquals(oldToken, loggedInUser.getToken(), "Should update token after login");
    }

    @Test
    public void testLogin_userNotFound() {
        User loginInput = new User();
        loginInput.setUsername("nonExisting");
        loginInput.setPassword("password");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.login(loginInput);
        });
        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    public void testLogin_invalidPassword() {
        User user = new User();
        user.setUsername("loginTestUser");
        user.setEmail("loginTest@edu.example.com");
        user.setPassword("correctPassword");
        userService.createUser(user);

        User loginInput = new User();
        loginInput.setUsername("loginTestUser");
        loginInput.setPassword("wrongPassword");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.login(loginInput);
        });
        assertTrue(ex.getMessage().contains("Invalid password"));
    }

    @Test
    public void testLogout_invalidToken() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.logout("nonExistingToken");
        });
        assertTrue(ex.getMessage().contains("Invalid token"));
    }

    @Test
    public void testGetUserByToken_notFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.getUserByToken("nonExistingToken");
        });
        assertTrue(ex.getMessage().contains("Invalid token"));
    }


    @Test
    public void testDeleteUserIntegration_success() {

        User newUser = new User();
        newUser.setUsername("deleteUser");
        newUser.setEmail("deleteuser@edu.example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);


        userService.deleteUser(createdUser.getId(), createdUser.getToken());


        assertThrows(ResponseStatusException.class, () -> {
            userService.getUserById(createdUser.getId());
        });
    }

    @Test
    public void testUpdateUserIntegration_success() {

        User newUser = new User();
        newUser.setUsername("updateUser");
        newUser.setEmail("updateuser@edu.example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);


        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO userPutDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        userPutDTO.setUsername("updatedUser");
        userPutDTO.setEmail("updateduser@edu.example.com");


        userService.updateUser(createdUser.getId(), userPutDTO, createdUser.getToken());

        User updatedUser = userService.getUserById(createdUser.getId());
        assertEquals("updatedUser", updatedUser.getUsername());
        assertEquals("updateduser@edu.example.com", updatedUser.getEmail());
    }

    @Test
    public void testDeleteUser_unauthorized() {
        User userA = new User();
        userA.setUsername("deleteUserA");
        userA.setEmail("deleteUserA@edu.example.com");
        userA.setPassword("password");
        User createdA = userService.createUser(userA);

        User userB = new User();
        userB.setUsername("deleteUserB");
        userB.setEmail("deleteUserB@edu.example.com");
        userB.setPassword("password");
        User createdB = userService.createUser(userB);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(createdA.getId(), createdB.getToken());
        });
        assertTrue(ex.getMessage().contains("unauthorized"));
    }

    @Test
    public void testDeleteUser_userNotFound() {
        User newUser = new User();
        newUser.setUsername("nonExistDelete");
        newUser.setEmail("nonExistDelete@edu.example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.deleteUser(9999L, createdUser.getToken());
        });
        assertTrue(ex.getMessage().contains("was not found"));
    }

    @Test
    public void testUpdateUser_unauthorized() {
        User userA = new User();
        userA.setUsername("userA");
        userA.setEmail("userA@edu.example.com");
        userA.setPassword("password");
        User createdA = userService.createUser(userA);

        User userB = new User();
        userB.setUsername("userB");
        userB.setEmail("userB@edu.example.com");
        userB.setPassword("password");
        User createdB = userService.createUser(userB);

        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO putDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        putDTO.setUsername("newUsername");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            userService.updateUser(createdA.getId(), putDTO, createdB.getToken());
        });
        assertTrue(ex.getMessage().contains("unauthorized"));
    }
}