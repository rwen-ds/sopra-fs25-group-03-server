package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@Service
@Transactional
public class RequestService {
    
    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final String adminToken = "adminToken"; // This should be replaced with a secure token management system


    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public List<Request> getRequests(String token) {
        if (!token.equals(adminToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return this.requestRepository.findAll();
    }

    public List<Request> getWaitingRequests(String token) {
        if (!token.equals(adminToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return this.requestRepository.findAll().stream()
            .filter(request -> request.getStatus() == RequestStatus.WAITING)
            .toList();
    }

    public Request createRequest(Request newRequest, Long userId) {

        newRequest.setStatus(RequestStatus.WAITING);
        newRequest.setCreationDate(LocalDate.now());
        newRequest.setPoster(userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId)));

        if (newRequest.getTitle() == null || newRequest.getTitle().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }
        if (newRequest.getDescription() == null || newRequest.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot be empty");
        }
        if (newRequest.getEmergencyLevel() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Emergency level must be set");
        }

        newRequest = requestRepository.save(newRequest);
        requestRepository.flush();

        log.debug("Created Information for Request: {}", newRequest);
        return newRequest;
    }

    public Request getRequestById(Long id) {
        Request request = requestRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found with id: " + id));
        return request;
    }

    public Request updateRequest(Long id, Request updatedRequest, String token) {
        Request existingRequest = getRequestById(id);
        if (!token.equals(adminToken) || !existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        if (updatedRequest.getTitle() != null) {
            existingRequest.setTitle(updatedRequest.getTitle());
        }
        if (updatedRequest.getDescription() != null) {
            existingRequest.setDescription(updatedRequest.getDescription());
        }
        if (updatedRequest.getContactInfo() != null) {
            existingRequest.setContactInfo(updatedRequest.getContactInfo());
        }
        if (updatedRequest.getLocation() != null) {
            existingRequest.setLocation(updatedRequest.getLocation());
        }
        if (updatedRequest.getFeedback() != null) {
            existingRequest.setFeedback(updatedRequest.getFeedback());
        }
        if (updatedRequest.getStatus() != null) {
            existingRequest.setStatus(updatedRequest.getStatus());
        }
        if (updatedRequest.getEmergencyLevel() != null) {
            existingRequest.setEmergencyLevel(updatedRequest.getEmergencyLevel());
        }

        requestRepository.save(existingRequest);
        return existingRequest;
    }

    public void deleteRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        if (!token.equals(adminToken) || !existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        requestRepository.delete(existingRequest);
    }

    public void acceptRequest(Long userId, Long volunteerId) {
        Request existingRequest = getRequestById(userId);
        if (existingRequest.getStatus() != RequestStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not in a state to accept a volunteer");
        }
        existingRequest.setVolunteer(userRepository.findById(volunteerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + volunteerId)));
        existingRequest.setStatus(RequestStatus.ACCEPTING);
        requestRepository.save(existingRequest);
    }

    public void completeRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        if (!existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus() != RequestStatus.ACCEPTING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only accepted requests can be completed");
        }
        existingRequest.setStatus(RequestStatus.COMPLETED);
        requestRepository.save(existingRequest);
    }

    public void cancelRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        if (!existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus() != RequestStatus.ACCEPTING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a request that isn't accepted");
        }
        existingRequest.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(existingRequest);
    }


}
