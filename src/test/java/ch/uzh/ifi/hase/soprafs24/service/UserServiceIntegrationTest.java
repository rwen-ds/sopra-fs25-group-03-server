// package ch.uzh.ifi.hase.soprafs24.service;

// import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
// import ch.uzh.ifi.hase.soprafs24.entity.User;
// import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.web.WebAppConfiguration;
// import org.springframework.web.server.ResponseStatusException;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * Test class for the UserResource REST resource.
//  *
//  * @see UserService
//  */
// @WebAppConfiguration
// @SpringBootTest
// public class UserServiceIntegrationTest {

//   @Qualifier("userRepository")
//   @Autowired
//   private UserRepository userRepository;

//   @Autowired
//   private UserService userService;

//   @BeforeEach
//   public void setup() {
//     userRepository.deleteAll();
//   }

// //  @Test
// //  public void createUser_validInputs_success() {
// //    // given
// //    assertNull(userRepository.findByUsername("testUsername"));
// //
// //    User testUser = new User();
// //    testUser.setUsername("testUsername");
// //    testUser.setPassword("testPassword");
// //
// //    // when
// //    User createdUser = userService.createUser(testUser);
// //
// //    // then
// //    assertEquals(testUser.getId(), createdUser.getId());
// //    assertEquals(testUser.getUsername(), createdUser.getUsername());
// //    assertNotNull(createdUser.getToken());
// //    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
// //  }
// //
// //  @Test
// //  public void createUser_duplicateUsername_throwsException() {
// //    assertNull(userRepository.findByUsername("testUsername"));
// //
// //    User testUser = new User();
// //    testUser.setUsername("testUsername");
// //    testUser.setPassword("testPassword");
// //    User createdUser = userService.createUser(testUser);
// //
// //    // attempt to create second user with same username
// //    User testUser2 = new User();
// //
// //    // change the password but forget about the username
// //    testUser2.setUsername("testUsername");
// //    testUser2.setPassword("testPassword2");
// //
// //    // check that an error is thrown
// //    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
// //  }
// }

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
    public void testGetUsersIntegration_success(){
        
        User user1 = new User();
        user1.setUsername("integrationUser1");
        user1.setEmail("integration1@example.com");
        user1.setPassword("password1");
        User user2 = new User();
        user2.setUsername("integrationUser2");
        user2.setEmail("integration2@example.com");
        user2.setPassword("password2");

        userService.createUser(user1);
        userService.createUser(user2);

        var users = userService.getUsers();
        assertEquals(2, users.size());
        
    }
    @Test
    public void testCreateUserIntegration_success() {

        User newUser = new User();
        newUser.setUsername("integrationUser");
        newUser.setEmail("integration@example.com");
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
    public void testGetUserByIdIntegration_success() {
        User newUser = new User();
        newUser.setUsername("getUser");
        newUser.setEmail("getuser@example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);

        User fetchedUser = userService.getUserById(createdUser.getId());
        assertEquals(createdUser.getId(), fetchedUser.getId());
    }

    @Test
    public void testLoginIntegration_success() {

        User newUser = new User();
        newUser.setUsername("loginUser");
        newUser.setEmail("loginuser@example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);


        userService.logout(createdUser.getToken());


        User loginInput = new User();
        loginInput.setUsername("loginUser");
        loginInput.setPassword("password");

        User loggedInUser = userService.login(loginInput);


        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
        assertNotNull(loggedInUser.getToken());
        assertNotEquals(createdUser.getToken(), loggedInUser.getToken(), "登录后应更新 token");
    }


    @Test
    public void testDeleteUserIntegration_success() {

        User newUser = new User();
        newUser.setUsername("deleteUser");
        newUser.setEmail("deleteuser@example.com");
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
        newUser.setEmail("updateuser@example.com");
        newUser.setPassword("password");
        User createdUser = userService.createUser(newUser);


        ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO userPutDTO = new ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO();
        userPutDTO.setUsername("updatedUser");
        userPutDTO.setEmail("updateduser@example.com");



        userService.updateUser(createdUser.getId(), userPutDTO, createdUser.getToken());

        User updatedUser = userService.getUserById(createdUser.getId());
        assertEquals("updatedUser", updatedUser.getUsername());
        assertEquals("updateduser@example.com", updatedUser.getEmail());
    }
}