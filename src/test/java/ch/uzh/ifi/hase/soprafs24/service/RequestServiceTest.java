package ch.uzh.ifi.hase.soprafs24.service;

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

    @InjectMocks
    private RequestService requestService;

    @Test
    public void testCreateRequest_success() {

        Request newRequest = new Request();
        newRequest.setTitle("Test Request Title");
        newRequest.setDescription("Test Request Description");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.LOW);
        newRequest.setPoster(new User());

        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        doNothing().when(requestRepository).flush();

        Request createdRequest = requestService.createRequest(newRequest);
        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals(RequestStatus.WAITING, createdRequest.getStatus());
        assertNotNull(createdRequest.getCreationDate());
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

        Request existingRequest = new Request();
        existingRequest.setId(1L);
        existingRequest.setTitle("Old Title");
        existingRequest.setDescription("Old Description");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Request update = new Request();
        update.setTitle("New Title");
        update.setDescription("New Description");

        Request updatedRequest = requestService.updateRequest(1L, update);
        assertEquals("New Title", updatedRequest.getTitle());
        assertEquals("New Description", updatedRequest.getDescription());
    }

    @Test
    public void testDeleteRequest_success() {
        Request existingRequest = new Request();
        existingRequest.setId(1L);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        doNothing().when(requestRepository).delete(existingRequest);

        assertDoesNotThrow(() -> requestService.deleteRequest(1L));
        verify(requestRepository, times(1)).delete(existingRequest);
    }

    @Test
    public void testAcceptRequest_success() {

        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.WAITING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User volunteer = new User();
        volunteer.setId(100L);

        requestService.acceptRequest(1L, volunteer);
        assertEquals(RequestStatus.ACCEPTING, request.getStatus());
        assertEquals(volunteer, request.getVolunteer());
    }

    @Test
    public void testAcceptRequest_invalidStatus() {

        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.ACCEPTING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        User volunteer = new User();
        volunteer.setId(100L);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(1L, volunteer);
        });
        assertTrue(exception.getMessage().contains("Request is not in a state to accept a volunteer"));
    }

    @Test
    public void testCompleteRequest_success() {

        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.ACCEPTING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestService.completeRequest(1L);
        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    public void testCompleteRequest_invalidStatus() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.WAITING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(1L);
        });
        assertTrue(exception.getMessage().contains("Only accepted requests can be completed"));
    }

    @Test
    public void testCancelRequest_success() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.ACCEPTING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestService.cancelRequest(1L);
        assertEquals(RequestStatus.CANCELLED, request.getStatus());
    }

    @Test
    public void testCancelRequest_invalidStatus() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.WAITING);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(1L);
        });
        assertTrue(exception.getMessage().contains("Cannot cancel a request that isn't accepted"));
    }

    @Test
    public void testGetRequests_and_GetWaitingRequests() {

        Request request1 = new Request();
        request1.setId(1L);
        request1.setStatus(RequestStatus.WAITING);
        Request request2 = new Request();
        request2.setId(2L);
        request2.setStatus(RequestStatus.ACCEPTING);
        Request request3 = new Request();
        request3.setId(3L);
        request3.setStatus(RequestStatus.WAITING);

        when(requestRepository.findAll()).thenReturn(List.of(request1, request2, request3));

        List<Request> allRequests = requestService.getRequests();
        assertEquals(3, allRequests.size());

        List<Request> waitingRequests = requestService.getWaitingRequests();
        assertEquals(2, waitingRequests.size());
        for (Request req : waitingRequests) {
            assertEquals(RequestStatus.WAITING, req.getStatus());
        }
    }
}
