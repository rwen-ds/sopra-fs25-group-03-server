package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestServiceIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    private final String adminToken = "adminToken";

    private User createUser(String username, String token) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("password");
        user.setCreationDate(LocalDate.now());
        user.setToken(token);
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);
        return user;
    }

    private Request createRequest(String title, RequestStatus status, User poster) {
        Request request = new Request();
        request.setTitle(title);
        request.setDescription("Integration test description");
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setStatus(status);
        request.setCreationDate(LocalDate.now());
        request.setPoster(poster);
        requestRepository.save(request);
        return request;
    }

    @Test
    public void createAndRetrieveRequest_success() {
        User poster = createUser("poster", adminToken);

        Request newRequest = new Request();
        newRequest.setTitle("Test Request");
        newRequest.setDescription("Integration Test");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.HIGH);

        Request created = requestService.createRequest(newRequest, poster.getId());

        assertNotNull(created.getId());
        assertEquals(RequestStatus.WAITING, created.getStatus());

        Request retrieved = requestService.getRequestById(created.getId());
        assertEquals("Test Request", retrieved.getTitle());
    }

    @Test
    public void createRequest_invalidParameters_throwsException() {
        User poster = createUser("poster", adminToken);

        Request newRequest = new Request();
        newRequest.setEmergencyLevel(RequestEmergencyLevel.HIGH);

        assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(newRequest, poster.getId());
        });
    }

    @Test
    public void updateRequest_validToken_success() {
        User poster = createUser("poster", adminToken);
        Request request = createRequest("Initial Title", RequestStatus.WAITING, poster);

        Request update = new Request();
        update.setTitle("Updated Title");

        Request updatedRequest = requestService.updateRequest(request.getId(), update, adminToken);
        assertEquals("Updated Title", updatedRequest.getTitle());
    }

    @Test
    public void updateRequest_invalidToken_throwsException() {
        User poster = createUser("poster", adminToken);
        Request request = createRequest("Initial Title", RequestStatus.WAITING, poster);

        Request update = new Request();
        update.setTitle("Updated Title");

        assertThrows(ResponseStatusException.class, () -> {
            requestService.updateRequest(request.getId(), update, "wrongToken");
        });
    }

    @Test
    public void acceptRequest_validScenario_success() {
        User poster = createUser("poster", adminToken);
        User volunteer = createUser("volunteer", "volunteerToken");
        Request request = createRequest("Need Help", RequestStatus.WAITING, poster);

        requestService.acceptRequest(request.getId(), volunteer.getId());

        Request acceptedRequest = requestService.getRequestById(request.getId());
        assertEquals(RequestStatus.ACCEPTING, acceptedRequest.getStatus());
        assertEquals(volunteer.getId(), acceptedRequest.getVolunteer().getId());
    }

    @Test
    public void completeRequest_validScenario_success() {
        User poster = createUser("poster", adminToken);
        Request request = createRequest("Complete me", RequestStatus.ACCEPTING, poster);

        requestService.completeRequest(request.getId(), adminToken);

        Request completedRequest = requestService.getRequestById(request.getId());
        assertEquals(RequestStatus.COMPLETED, completedRequest.getStatus());
    }

    @Test
    public void cancelRequest_invalidStatus_throwsException() {
        User poster = createUser("poster", adminToken);
        Request request = createRequest("Cancel me", RequestStatus.WAITING, poster);

        assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(request.getId(), adminToken);
        });
    }

    @Test
    public void getRequestById_notFound_throwsException() {
        assertThrows(ResponseStatusException.class, () -> {
            requestService.getRequestById(999L);
        });
    }

    @Test
    public void deleteRequest_notFound_throwsException() {
        assertThrows(ResponseStatusException.class, () -> {
            requestService.deleteRequest(999L, adminToken);
        });
    }

    @Test
    public void acceptRequest_invalidRequest_throwsException() {
        User volunteer = createUser("volunteer", "volunteerToken");
        assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(999L, volunteer.getId());
        });
    }

    @Test
    public void completeRequest_invalidToken_throwsUnauthorized() {
        User poster = createUser("poster", adminToken);
        Request request = createRequest("Complete me", RequestStatus.ACCEPTING, poster);

        assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(request.getId(), "wrongToken");
        });
    }
}