package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        if (!token.equals(adminToken) && !existingRequest.getPoster().getToken().equals(token)) {
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
        if (!token.equals(adminToken) && !existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        requestRepository.delete(existingRequest);
    }

    public void acceptRequest(Long userId, Long volunteerId) {
        Request existingRequest = getRequestById(userId);
        if (existingRequest.getStatus() != RequestStatus.VOLUNTEERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not in a state to accept a volunteer");
        }
        existingRequest.setVolunteer(userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + volunteerId)));
        existingRequest.setStatus(RequestStatus.ACCEPTING);
        requestRepository.save(existingRequest);
    }

    public void completeRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        if (!existingRequest.getVolunteer().getToken().equals(token)) {
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

    public List<Request> getRequestByPosterId(Long posterId) {
        List<Request> requests = requestRepository.findByPosterId(posterId);
        return requests;
    }

    public List<NotificationDTO> getNotifications(User user) {
        List<NotificationDTO> notifications = new ArrayList<>();
        List<Request> postedRequests = requestRepository.findByPoster(user);
        for (Request request : postedRequests) {
            if (request.getStatus() == RequestStatus.VOLUNTEERED && request.getVolunteer() != null) {
                notifications.add(new NotificationDTO(
                        RequestStatus.VOLUNTEERED,
                        "Volunteer " + request.getVolunteer().getUsername() + " is applying to help your request '" + request.getTitle() + "'",
                        request.getId(),
                        user.getId(),
                        request.getVolunteer().getId(),
                        request.getTitle()
                ));
            }
            else if (request.getStatus() == RequestStatus.COMPLETED && request.getVolunteer() != null) {
                notifications.add(new NotificationDTO(
                        RequestStatus.COMPLETED,
                        "Your request '" + request.getTitle() + "' is completed by volunteer " + request.getVolunteer().getUsername() + "!",
                        request.getId(),
                        user.getId(),
                        request.getVolunteer().getId(),
                        request.getTitle()
                ));
            }
        }

        List<Request> volunteeredRequests = requestRepository.findByVolunteer(user);
        for (Request request : volunteeredRequests) {
            if (request.getStatus() == RequestStatus.ACCEPTING) {
                notifications.add(new NotificationDTO(
                        RequestStatus.ACCEPTING,
                        "Your volunteer for request '" + request.getTitle() + "' is accepted!",
                        request.getId(),
                        request.getPoster().getId(),
                        user.getId(),
                        request.getTitle()
                ));
            }
        }
        return notifications;
    }

    public List<Request> getActiveRequests() {
        List<Request> waitingRequests = requestRepository.findByStatus(RequestStatus.WAITING);

        return waitingRequests;
    }

    public void volunteerRequest(Request request, User volunteer) {
        request.setVolunteer(volunteer);
        request.setStatus(RequestStatus.VOLUNTEERED);
        requestRepository.save(request);
    }

    public void markRequestAsDone(Long requestId, String token) {
        Request existingRequest = getRequestById(requestId);
        if (!existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus() != RequestStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed requests can be mark as done");
        }
        existingRequest.setStatus(RequestStatus.DONE);
        requestRepository.save(existingRequest);
    }

    public void feedback(Long requestId, String token, String feedback) {
        Request existingRequest = getRequestById(requestId);
        if (!existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus() != RequestStatus.DONE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests be marked as done can be feedback");
        }
        existingRequest.setFeedback(feedback);
        requestRepository.save(existingRequest);
    }
}
