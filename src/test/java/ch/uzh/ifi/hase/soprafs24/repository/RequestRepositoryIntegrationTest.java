package ch.uzh.ifi.hase.soprafs24.repository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@DataJpaTest
public class RequestRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RequestRepository requestRepository;

    @Test
    public void findById_success() {
        User poster = new User();
        poster.setUsername("testUser");
        poster.setPassword("password");
        poster.setCreationDate(LocalDate.parse("2025-02-22"));
        poster.setToken("token123");
        poster.setEmail("testuser@edu.example.com");
        poster.setStatus(UserStatus.OFFLINE);

        entityManager.persist(poster);
        entityManager.flush();


        Request request = new Request();
        request.setTitle("Test Request Title");
        request.setDescription("This is a test request description.");
        request.setContactInfo("contact@example.com");
        request.setLocation("Zurich");
        request.setFeedback("No feedback yet.");
        request.setStatus(RequestStatus.WAITING);
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setCreationDate(LocalDate.now());
        request.setPoster(poster);

        entityManager.persist(request);
        entityManager.flush();


        Request testRequest = requestRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Not testRequest"));

        // then
        assertNotNull(testRequest.getId());
        assertEquals("Test Request Title", testRequest.getTitle());
        assertEquals("This is a test request description.", testRequest.getDescription());
        assertEquals("contact@example.com", testRequest.getContactInfo());
        assertEquals("Zurich", testRequest.getLocation());
        assertEquals("No feedback yet.", testRequest.getFeedback());
        assertEquals(RequestStatus.WAITING, testRequest.getStatus());
        assertEquals(RequestEmergencyLevel.LOW, testRequest.getEmergencyLevel());
        assertEquals(LocalDate.now(), testRequest.getCreationDate());
        assertNotNull(testRequest.getPoster());
        assertEquals("testUser", testRequest.getPoster().getUsername());
    }
}