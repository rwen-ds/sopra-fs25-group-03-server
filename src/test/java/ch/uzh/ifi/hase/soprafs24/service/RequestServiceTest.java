package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;
    
    @Mock
    private ch.uzh.ifi.hase.soprafs24.repository.UserRepository userRepository;

    @InjectMocks
    private RequestService requestService;

    private User createSampleUser(Long id, String username, String token) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setCreationDate(LocalDate.now());
        user.setToken(token);
        return user;
    }
    
    private Request createSampleRequest(Long id, String title, RequestStatus status, User poster) {
        Request request = new Request();
        request.setId(id);
        request.setTitle(title);
        request.setDescription("Sample description");
        request.setContactInfo("sample@example.com");
        request.setLocation("Sample location");
        request.setCreationDate(LocalDate.now());
        request.setStatus(status);
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setPoster(poster);
        return request;
    }

    private Request createSampleRequest(Long id, String title, RequestStatus status, User poster, User volunteer) {
        Request request = new Request();
        request.setId(id);
        request.setTitle(title);
        request.setDescription("Sample description");
        request.setContactInfo("sample@example.com");
        request.setLocation("Sample location");
        request.setCreationDate(LocalDate.now());
        request.setStatus(status);
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setPoster(poster);
        request.setVolunteer(volunteer);
        return request;
    }
    
    @Test
    public void testCreateRequest_success() {
        Long posterId = 100L;
        User poster = createSampleUser(posterId, "posterUser", "adminToken");
        when(userRepository.findById(posterId)).thenReturn(Optional.of(poster));
        
        Request newRequest = new Request();
        newRequest.setTitle("Test Request Title");
        newRequest.setDescription("Test Request Description");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.LOW);

        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        doNothing().when(requestRepository).flush();
        
        Request createdRequest = requestService.createRequest(newRequest, posterId);
        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals(RequestStatus.WAITING, createdRequest.getStatus());
        assertNotNull(createdRequest.getCreationDate());

        assertNotNull(createdRequest.getPoster());
        assertEquals("posterUser", createdRequest.getPoster().getUsername());
    }
    
    @Test
    public void testGetRequestById_success() {
        Request request = new Request();
        request.setId(1L);
        request.setTitle("Some Title");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Request foundRequest = requestService.getRequestById(1L);
        assertNotNull(foundRequest);
        assertEquals(1L, foundRequest.getId());
    }
    
    @Test
    public void testGetRequestById_notFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.getRequestById(1L);
        });
        assertTrue(exception.getMessage().contains("Request not found with id: 1"));
    }
    
    @Test
    public void testUpdateRequest_success() {

        User poster = createSampleUser(100L, "posterUser", "token");
        Request existingRequest = createSampleRequest(1L, "Old Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRepository.findByToken("token")).thenReturn(poster);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Request update = new Request();
        update.setTitle("New Title");
        update.setDescription("New Description");
        

        Request updatedRequest = requestService.updateRequest(1L, update, "token");
        assertEquals("New Title", updatedRequest.getTitle());
        assertEquals("New Description", updatedRequest.getDescription());
    }
    
    @Test
    public void testUpdateRequest_invalidToken_throwsUnauthorized() {

        User poster = createSampleUser(100L, "posterUser", "correctToken");
        Request existingRequest = createSampleRequest(1L, "Old Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));

        User fakeUser = createSampleUser(200L, "notAdmin", "wrongToken");
        when(userRepository.findByToken("wrongToken")).thenReturn(fakeUser);

        Request update = new Request();
        update.setTitle("New Title");
        

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.updateRequest(1L, update, "wrongToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }
    
    @Test
    public void testDeleteRequest_success() {

        User poster = createSampleUser(100L, "posterUser", "token");
        Request existingRequest = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userRepository.findByToken("token")).thenReturn(poster);
        doNothing().when(requestRepository).delete(existingRequest);
        
        assertDoesNotThrow(() -> requestService.deleteRequest(1L, "token"));

        verify(requestRepository, times(1)).delete(existingRequest);
    }
    
    @Test
    public void testDeleteRequest_invalidToken_throwsUnauthorized() {
        User poster = createSampleUser(100L, "posterUser", "correctToken");
        Request existingRequest = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));

        User fakeUser = createSampleUser(200L, "notAdmin", "wrongToken");
        when(userRepository.findByToken("wrongToken")).thenReturn(fakeUser);
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.deleteRequest(1L, "wrongToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }
    
    @Test
    public void testAcceptRequest_success() {

        Request request = createSampleRequest(1L, "Title", RequestStatus.VOLUNTEERED, createSampleUser(100L, "posterUser", "token"));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        when(userRepository.findById(200L)).thenReturn(Optional.of(volunteer));
        
        requestService.acceptRequest(1L, 200L);
        assertEquals(RequestStatus.ACCEPTING, request.getStatus());
        assertEquals(volunteer, request.getVolunteer());
    }
    
    @Test
    public void testAcceptRequest_invalidStatus_throwsBadRequest() {

        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, createSampleUser(100L, "posterUser", "token"));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        
        createSampleUser(200L, "volunteerUser", "volunteerToken");
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(1L, 200L);
        });
        assertTrue(exception.getMessage().contains("Request is not in a state to accept a volunteer"));
    }
    
    @Test
    public void testCompleteRequest_success() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        requestService.completeRequest(1L, "volunteerToken");
        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }
    
    @Test
    public void testCompleteRequest_invalidStatus_throwsBadRequest() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(1L, "volunteerToken");
        });
        assertTrue(exception.getMessage().contains("Only accepted requests can be completed"));
    }
    
    @Test
    public void testCancelRequest_success() {
        User poster = createSampleUser(100L, "posterUser", "token");
        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        requestService.cancelRequest(1L, "token");
        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }
    
    @Test
    public void testCancelRequest_invalidStatus_throwsBadRequest() {
        User poster = createSampleUser(100L, "posterUser", "token");
        Request request = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(1L, "token");
        });
        assertTrue(exception.getMessage().contains("Cannot cancel a request that isn't accepted"));
    }
    
    @Test
    public void testGetRequests_and_GetWaitingRequests() {
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.WAITING, createSampleUser(100L, "posterUser", "token"));
        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.ACCEPTING, createSampleUser(100L, "posterUser", "token"));
        Request request3 = createSampleRequest(3L, "Request 3", RequestStatus.WAITING, createSampleUser(100L, "posterUser", "token"));

        User admin = createSampleUser(200L, "admin", "adminToken");
        when(userRepository.findByToken("adminToken")).thenReturn(admin);

        when(requestRepository.findAll()).thenReturn(List.of(request1, request2, request3));
        when(requestRepository.findByStatus(RequestStatus.WAITING)).thenReturn(List.of(request1, request3));


        List<Request> allRequests = requestService.getRequests("adminToken");
        assertEquals(3, allRequests.size());
        
        List<Request> waitingRequests = requestService.getWaitingRequests();
        assertEquals(2, waitingRequests.size());
        for (Request req : waitingRequests) {
            assertEquals(RequestStatus.WAITING, req.getStatus());
        }
    }
}