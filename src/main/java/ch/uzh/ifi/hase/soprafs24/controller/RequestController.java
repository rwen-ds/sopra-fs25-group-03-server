package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.RequestService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private static final String AUTH_HEADER = "Authorization";
    private final RequestService requestService;
    private final UserService userService;
    private final UserRepository userRepository;

    public RequestController(RequestService requestService, UserService userService, UserRepository userRepository) {
        this.requestService = requestService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllRequests(@RequestHeader(AUTH_HEADER) String token) {
        try {
            List<RequestGetDTO> requests = requestService.getRequests(token).stream()
                    .map(DTOMapper.INSTANCE::convertEntityToRequestGetDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestById(@PathVariable Long requestId) {
        try {
            Request request = requestService.getRequestById(requestId);
            return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToRequestGetDTO(request));
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody RequestPostDTO requestPostDTO, @RequestParam Long posterId) {
        try {
            Request requestEntity = DTOMapper.INSTANCE.convertRequestPostDTOtoEntity(requestPostDTO);
            Request createdRequest = requestService.createRequest(requestEntity, posterId);
            RequestGetDTO requestGetDTO = DTOMapper.INSTANCE.convertEntityToRequestGetDTO(createdRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(requestGetDTO);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Long requestId,
            @RequestBody RequestPostDTO requestPostDTO,
            @RequestHeader(AUTH_HEADER) String token
    ) {
        try {
            Request requestToUpdate = DTOMapper.INSTANCE.convertRequestPostDTOtoEntity(requestPostDTO);
            Request updatedRequest = requestService.updateRequest(requestId, requestToUpdate, token);
            return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToRequestGetDTO(updatedRequest));
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.deleteRequest(requestId, token);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, @RequestParam Long volunteerId) {
        try {
            requestService.acceptRequest(requestId, volunteerId);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/complete")
    public ResponseEntity<?> completeRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.completeRequest(requestId, token);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.cancelRequest(requestId, token);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    // @GetMapping("/{requestId}/volunteer")
    // public ResponseEntity<?> volunteer(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
    //     try {
    //         Request request = requestService.getRequestById(requestId);
    //         User volunteer = userService.getUserByToken(token);
    //         Long volunteerId = volunteer.getId();
    //         Long requestPosterId = request.getPoster().getId();
    //         return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToUserGetDTO(request.getVolunteer()));
    //     } catch (ResponseStatusException ex) {
    //         return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
    //     }
    // }

    @GetMapping("/me")
    public List<RequestGetDTO> getRequestByPosterId(@RequestHeader(AUTH_HEADER) String token) {

        User user = userRepository.findByToken(token);

        List<RequestGetDTO> requestGetDTOs = new ArrayList<>();
        List<Request> requests = requestService.getRequestByPoster(user);

        // convert each user to the API representation
        for (Request request : requests) {
            requestGetDTOs.add(DTOMapper.INSTANCE.convertEntityToRequestGetDTO(request));
        }
        return requestGetDTOs;
    }

    @PutMapping("/volunteer/{requestId}")
    public ResponseEntity<?> volunteer(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            Request request = requestService.getRequestById(requestId);
            User volunteer = userService.getUserByToken(token);
            requestService.volunteerRequest(request, volunteer);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/notifications")
    public List<NotificationDTO> getNotifications(@RequestHeader(AUTH_HEADER) String token) {
        User user = userService.getUserByToken(token);
        List<NotificationDTO> notifications = requestService.getNotifications(user);
        return notifications;
    }

    @GetMapping("/active")
    public List<RequestGetDTO> getActiveRequests() {
        List<RequestGetDTO> requestGetDTOs = new ArrayList<>();
        List<Request> activeRequests = requestService.getActiveRequests();
        for (Request request : activeRequests) {
            requestGetDTOs.add(DTOMapper.INSTANCE.convertEntityToRequestGetDTO(request));
        }
        return requestGetDTOs;
    }

    @PutMapping("/{requestId}/done")
    public ResponseEntity<?> markRequestAsDone(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.markRequestAsDone(requestId, token);
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/feedback")
    public ResponseEntity<?> sendFeedback(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token, @RequestBody FeedbackDTO feedback) {
        try {
            requestService.feedback(requestId, token, feedback.getFeedback());
            return ResponseEntity.ok().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }
}
