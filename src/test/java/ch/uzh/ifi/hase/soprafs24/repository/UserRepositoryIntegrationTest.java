package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

//    @Test
//    public void findByUsername_success() {
//        // given
//        User user = new User();
//        user.setUsername("username");
//        user.setPassword("password");
//        user.setCreationDate(LocalDate.parse("2025-02-22"));
//        user.setBirthday(LocalDate.parse("2000-01-01"));
//        user.setToken("1");
//        user.setStatus(UserStatus.OFFLINE);
//
//        entityManager.persist(user);
//        entityManager.flush();
//
//        // when
//        User found = userRepository.findByUsername(user.getUsername());
//
//        // then
//        assertNotNull(found.getId());
//        assertEquals(found.getUsername(), user.getUsername());
//        assertEquals(found.getPassword(), user.getPassword());
//        assertEquals(found.getCreationDate(), user.getCreationDate());
//        assertEquals(found.getBirthday(), user.getBirthday());
//        assertEquals(found.getToken(), user.getToken());
//        assertEquals(found.getStatus(), user.getStatus());
//
//    }
}
