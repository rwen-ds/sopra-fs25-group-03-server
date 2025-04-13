package ch.uzh.ifi.hase.soprafs24.repository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setCreationDate(LocalDate.parse("2025-02-22"));
        user.setBirthday(LocalDate.parse("2000-01-01"));
        user.setToken("token123");
        user.setEmail("testuser@example.com");
        user.setStatus(UserStatus.OFFLINE);
        
        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    public void findByUsername_success() {
        User testUser = userRepository.findByUsername(user.getUsername());
        
        assertNotNull(testUser.getId());
        assertEquals(user.getUsername(), testUser.getUsername());
        assertEquals(user.getPassword(), testUser.getPassword());
        assertEquals(user.getCreationDate(), testUser.getCreationDate());
        assertEquals(user.getBirthday(), testUser.getBirthday());
        assertEquals(user.getToken(), testUser.getToken());
        assertEquals(user.getEmail(), testUser.getEmail());
        assertEquals(user.getStatus(), testUser.getStatus());
    }
    
    @Test
    public void findByEmail_success() {
        User testUser = userRepository.findByEmail(user.getEmail());
        
        assertNotNull(testUser.getId());
        assertEquals(user.getEmail(), testUser.getEmail());
        assertEquals(user.getUsername(), testUser.getUsername());
        assertEquals(user.getPassword(), testUser.getPassword());
        assertEquals(user.getCreationDate(), testUser.getCreationDate());
        assertEquals(user.getBirthday(), testUser.getBirthday());
        assertEquals(user.getToken(), testUser.getToken());
        assertEquals(user.getStatus(), testUser.getStatus());
    }
    
    @Test
    public void findByToken_success() {
        User testUser = userRepository.findByToken(user.getToken());
        
        assertNotNull(testUser.getId());
        assertEquals(user.getToken(), testUser.getToken());
        assertEquals(user.getUsername(), testUser.getUsername());
        assertEquals(user.getPassword(), testUser.getPassword());
        assertEquals(user.getCreationDate(), testUser.getCreationDate());
        assertEquals(user.getBirthday(), testUser.getBirthday());
        assertEquals(user.getEmail(), testUser.getEmail());
        assertEquals(user.getStatus(), testUser.getStatus());
    }
}