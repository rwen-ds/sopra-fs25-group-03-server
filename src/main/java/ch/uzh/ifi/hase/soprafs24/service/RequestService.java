package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FeedbackDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RequestService {

    private final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final NotificationRepository notificationRepository;
    private final DTOMapper dtoMapper;

    @Autowired
    public RequestService(RequestRepository requestRepository, NotificationService notificationService, UserService userService, NotificationRepository notificationRepository) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.notificationRepository = notificationRepository;
        this.dtoMapper = DTOMapper.INSTANCE;
    }

    public List<Request> getRequests(String token) {
        User user = userService.getUserByToken(token);
        if (!user.getUsername().equals("admin")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return requestRepository.findAll().stream()
                .filter(request -> request.getStatus() != RequestStatus.DELETED)
                .collect(Collectors.toList());
    }

    public Request createRequest(Request newRequest, Long userId) {

        newRequest.setStatus(RequestStatus.WAITING);
        newRequest.setCreationDate(LocalDate.now());
        newRequest.setPoster(userService.getUserById(userId));

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
        if (request.getStatus() == RequestStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has been deleted.");
        }
        return request;
    }

    public Request updateRequest(Long id, Request updatedRequest, String token) {
        Request existingRequest = getRequestById(id);
        User user = userService.getUserByToken(token);
        if (!user.getUsername().equals("admin") && !existingRequest.getPoster().getToken().equals(token)) {
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
        if (updatedRequest.getLatitude() != null) {
            existingRequest.setLatitude(updatedRequest.getLatitude());
        }
        else {
            existingRequest.setLatitude(null);
        }
        if (updatedRequest.getLongitude() != null) {
            existingRequest.setLongitude(updatedRequest.getLongitude());
        }
        else {
            existingRequest.setLongitude(null);
        }
        if (updatedRequest.getCountryCode() != null) {
            existingRequest.setCountryCode(updatedRequest.getCountryCode());
        }
        else {
            existingRequest.setCountryCode(null);
        }

        requestRepository.save(existingRequest);
        return existingRequest;
    }

    public void deleteRequest(Long id, String token, String reason) {
        User user = userService.getUserByToken(token);
        Request existingRequest = getRequestById(id);
        if (!user.getUsername().equals("admin") && !user.getId().equals(existingRequest.getPoster().getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus().equals(RequestStatus.DELETED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request has been deleted");
        }
        existingRequest.setStatus(RequestStatus.DELETED);
        existingRequest.setDeletedAt(LocalDate.now());
        existingRequest.setDeletedByUserId(user.getId());
        existingRequest.setDeleteReason(reason);

        requestRepository.save(existingRequest);
        notificationRepository.deleteByRequest(existingRequest);
    }

    public void acceptRequest(Long requestId, Long volunteerId) {
        Request existingRequest = getRequestById(requestId);
        if (existingRequest.getStatus() != RequestStatus.VOLUNTEERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests with status VOLUNTEERED can be accepted");
        }
        if (existingRequest.getVolunteer() == null || !existingRequest.getVolunteer().getId().equals(volunteerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only accept the volunteer who applied for this request");
        }

        User volunteer = userService.getUserById(volunteerId);
        existingRequest.setStatus(RequestStatus.ACCEPTING);
        requestRepository.save(existingRequest);

        notificationService.acceptNotification(existingRequest, volunteer);
    }

    public void completeRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        if (existingRequest.getStatus() != RequestStatus.ACCEPTING || existingRequest.getVolunteer() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests with ACCEPTING status can be completed");
        }
        if (!existingRequest.getVolunteer().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        existingRequest.setStatus(RequestStatus.COMPLETED);
        requestRepository.save(existingRequest);

        notificationService.completeNotification(existingRequest);
    }

    public void cancelRequest(Long id, String token) {
        Request existingRequest = getRequestById(id);
        User poster = existingRequest.getPoster();
        User volunteer = existingRequest.getVolunteer();
        if (volunteer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It's not volunteered");
        }
        if (!existingRequest.getPoster().getToken().equals(token) && !existingRequest.getVolunteer().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user");
        }
        if (poster.getToken().equals(token)) {
            if (existingRequest.getStatus() != RequestStatus.ACCEPTING && existingRequest.getStatus() != RequestStatus.VOLUNTEERED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests with status ACCEPTING or VOLUNTEERED can be canceled");
            }
            notificationService.posterCancelNotification(existingRequest);
        }
        else if (volunteer.getToken().equals(token)) {
            if (existingRequest.getStatus() != RequestStatus.VOLUNTEERED && existingRequest.getStatus() != RequestStatus.ACCEPTING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests with status ACCEPTING or VOLUNTEERED can be canceled");
            }
            notificationService.volunteerCancelNotification(existingRequest);
        }

        existingRequest.setStatus(RequestStatus.WAITING);
        existingRequest.setVolunteer(null);
        requestRepository.save(existingRequest);
    }

    public List<Request> getWaitingRequests() {
        List<Request> waitingRequests = requestRepository.findByStatus(RequestStatus.WAITING);

        return waitingRequests;
    }

    public void volunteerRequest(Long requestId, String token) {
        Request request = getRequestById(requestId);
        User volunteer = userService.getUserByToken(token);
        if (request.getPoster().getId().equals(volunteer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot volunteer for your own request.");
        }

        if (!request.getStatus().equals(RequestStatus.WAITING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only volunteer for requests that are still waiting.");
        }
        request.setVolunteer(volunteer);
        request.setStatus(RequestStatus.VOLUNTEERED);
        requestRepository.save(request);

        notificationService.volunteerNotification(request, volunteer);
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

    public void feedback(Long requestId, String token, String feedback, Integer rating) {
        Request existingRequest = getRequestById(requestId);
        if (!existingRequest.getPoster().getToken().equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (existingRequest.getStatus() != RequestStatus.DONE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests be marked as done can be feedback");
        }
        existingRequest.setFeedback(feedback);
        existingRequest.setRating(rating);
        requestRepository.save(existingRequest);

        notificationService.feedbackNotification(existingRequest);
    }

    public List<Request> getRequestByPoster(String token) {
        User user = userService.getUserByToken(token);
        return requestRepository.findByPoster(user);
    }

    public List<FeedbackDTO> getFeedbackById(Long volunteerId) {
        List<Request> requests = requestRepository.findByVolunteerId(volunteerId);
        return requests.stream()
                .filter(request -> request.getRating() != null)
                .filter(request -> !request.getStatus().equals(RequestStatus.DELETED))
                .map(request -> new FeedbackDTO(request.getId(), request.getFeedback(), request.getRating()))
                .collect(Collectors.toList());
    }

    public List<RequestGetDTO> getPostRequestsByUserId(Long userId) {
        List<Request> requests = requestRepository.findByPosterId(userId);
        return requests.stream()
                .filter(request -> !request.getStatus().equals(RequestStatus.DELETED))
                .map(dtoMapper::convertEntityToRequestGetDTO)
                .collect(Collectors.toList());
    }

    public List<RequestGetDTO> getVolunteerRequestsByUserId(Long volunteerId) {
        List<Request> requests = requestRepository.findByVolunteerId(volunteerId);
        return requests.stream()
                .filter(request -> !request.getStatus().equals(RequestStatus.DELETED))
                .map(dtoMapper::convertEntityToRequestGetDTO)
                .collect(Collectors.toList());
    }
}
