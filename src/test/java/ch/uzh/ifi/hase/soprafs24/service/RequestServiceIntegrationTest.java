package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@SpringBootTest
@Transactional
public class RequestServiceIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    private User globalPoster;

    private User getOrCreateGlobalPoster() {
    if (globalPoster == null) {
        User poster = new User();
        poster.setUsername("posterUser");
        poster.setEmail("poster@example.com");
        poster.setPassword("password");
        globalPoster = userService.createUser(poster);
    }
    return globalPoster;
}

    private Request createValidRequest() {
        Request req = new Request();
        req.setTitle("Valid Title");
        req.setDescription("Valid Description");
        req.setEmergencyLevel(RequestEmergencyLevel.LOW);
        req.setPoster(getOrCreateGlobalPoster());
        req.setContactInfo("contact@example.com");
        req.setLocation("Test Location");
        return req;
    }

    @Test
    public void testCreateAndGetRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        assertNotNull(created.getId());
        assertEquals(RequestStatus.WAITING, created.getStatus());
        assertNotNull(created.getCreationDate());
        
        Request found = requestService.getRequestById(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Valid Title", found.getTitle());
    }
    
    @Test
    public void testCreateRequest_invalidTitle() {
        Request request = createValidRequest();
        request.setTitle("");
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(request);
        });
        assertTrue(e.getMessage().contains("Title cannot be empty"));
    }
    
    @Test
    public void testCreateRequest_invalidDescription() {
        Request request = createValidRequest();
        request.setDescription(null);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(request);
        });
        assertTrue(e.getMessage().contains("Description cannot be empty"));
    }
    
    @Test
    public void testCreateRequest_invalidEmergencyLevel() {
        Request request = createValidRequest();
        request.setEmergencyLevel(null);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(request);
        });
        assertTrue(e.getMessage().contains("Emergency level must be set"));
    }
    
    @Test
    public void testCreateRequest_invalidPoster() {
        Request request = createValidRequest();
        request.setPoster(null);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(request);
        });
        assertTrue(e.getMessage().contains("Poster must be set"));
    }
    
    @Test
    public void testUpdateRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        
        Request update = new Request();
        update.setTitle("Updated Title");
        update.setDescription("Updated Description");
        update.setContactInfo("updated@example.com");
        update.setLocation("Updated Location");
        update.setFeedback("New Feedback");
        update.setStatus(RequestStatus.ACCEPTING);
        update.setEmergencyLevel(RequestEmergencyLevel.HIGH);
        
        Request updated = requestService.updateRequest(created.getId(), update);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals("updated@example.com", updated.getContactInfo());
        assertEquals("Updated Location", updated.getLocation());
        assertEquals("New Feedback", updated.getFeedback());
        assertEquals(RequestStatus.ACCEPTING, updated.getStatus());
        assertEquals(RequestEmergencyLevel.HIGH, updated.getEmergencyLevel());
    }
    
    @Test
    public void testUpdateRequest_notFound() {
        Request update = new Request();
        update.setTitle("Updated Title");
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.updateRequest(9999L, update);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    
    @Test
    public void testUpdateRequest_noChange() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        Request update = new Request(); 
        Request updated = requestService.updateRequest(created.getId(), update);
        assertEquals(created.getTitle(), updated.getTitle());
        assertEquals(created.getDescription(), updated.getDescription());
        assertEquals(created.getContactInfo(), updated.getContactInfo());
        assertEquals(created.getLocation(), updated.getLocation());
    }
    
    @Test
    public void testDeleteRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        Long id = created.getId();
        assertNotNull(requestService.getRequestById(id));
        requestService.deleteRequest(id);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.getRequestById(id);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    
    @Test
    public void testDeleteRequest_notFound() {
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.deleteRequest(12345L);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    
    @Test
    public void testAcceptRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        
        User volunteer = new User();
        volunteer.setId(200L);
        volunteer.setUsername("volunteerUser");
        volunteer.setEmail("volunteer@example.com");
        volunteer.setPassword("password");
        
        requestService.acceptRequest(created.getId(), volunteer);
        Request accepted = requestService.getRequestById(created.getId());
        assertEquals(RequestStatus.ACCEPTING, accepted.getStatus());
        assertNotNull(accepted.getVolunteer());
        assertEquals(volunteer.getId(), accepted.getVolunteer().getId());
    }
    
    @Test
    public void testAcceptRequest_invalidStatus() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        created.setStatus(RequestStatus.ACCEPTING);
        requestService.updateRequest(created.getId(), created);
        
        User volunteer = new User();
        volunteer.setId(200L);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(created.getId(), volunteer);
        });
        assertTrue(e.getMessage().contains("Request is not in a state to accept a volunteer"));
    }
    
    @Test
    public void testAcceptRequest_notFound() {
        User volunteer = new User();
        volunteer.setId(200L);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(55555L, volunteer);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    
    @Test
    public void testCompleteRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        
        User volunteer = new User();
        volunteer.setId(300L);
        requestService.acceptRequest(created.getId(), volunteer);
        requestService.completeRequest(created.getId());
        Request completed = requestService.getRequestById(created.getId());
        assertEquals(RequestStatus.COMPLETED, completed.getStatus());
    }
    
    @Test
    public void testCompleteRequest_invalidStatus() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(created.getId());
        });
        assertTrue(e.getMessage().contains("Only accepted requests can be completed"));
    }
    
    @Test
    public void testCompleteRequest_notFound() {
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(77777L);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    
    @Test
    public void testCancelRequest_success() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);
        
        User volunteer = new User();
        volunteer.setId(400L);

        requestService.acceptRequest(created.getId(), volunteer);

        requestService.cancelRequest(created.getId());
        Request canceled = requestService.getRequestById(created.getId());
        assertEquals(RequestStatus.CANCELLED, canceled.getStatus());
    }
    
    @Test
    public void testCancelRequest_invalidStatus() {
        Request request = createValidRequest();
        Request created = requestService.createRequest(request);

        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(created.getId());
        });
        assertTrue(e.getMessage().contains("Cannot cancel a request that isn't accepted"));
    }
    

    @Test
    public void testCancelRequest_notFound() {
        Exception e = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(88888L);
        });
        assertTrue(e.getMessage().contains("Request not found with id"));
    }
    

    @Test
    public void testGetRequestsAndWaitingRequests() {
        Request request1 = createValidRequest();
        request1.setTitle("Request 1");
        
        Request request2 = createValidRequest();
        request2.setTitle("Request 2");

        request2.setStatus(RequestStatus.ACCEPTING);
        
        Request request3 = createValidRequest();
        request3.setTitle("Request 3");

        requestService.createRequest(request1);
        requestService.createRequest(request2);
        requestService.createRequest(request3);
        
        assertTrue(requestService.getRequests().size() >= 3);
        long waitingCount = requestService.getRequests().stream()
                              .filter(req -> req.getStatus() == RequestStatus.WAITING)
                              .count();
        assertTrue(waitingCount >= 2);
    }
}