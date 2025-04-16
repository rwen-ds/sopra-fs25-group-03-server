package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ErrorResponse;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.RequestService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private static final String AUTH_HEADER = "Authorization";
    private final RequestService requestService;
    private final UserService userService;

    public RequestController(RequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllRequests(@RequestHeader(AUTH_HEADER) String token) {
        try {
            List<RequestGetDTO> requests = requestService.getRequests(token).stream()
                    .map(DTOMapper.INSTANCE::convertEntityToRequestGetDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(requests);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequestById(@PathVariable Long requestId) {
        try {
            Request request = requestService.getRequestById(requestId);
            return ResponseEntity.ok(DTOMapper.INSTANCE.convertEntityToRequestGetDTO(request));
        } catch (ResponseStatusException ex) {
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
        } catch (ResponseStatusException ex) {
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
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.deleteRequest(requestId, token);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, @RequestParam Long volunteerId) {
        try {
            requestService.acceptRequest(requestId, volunteerId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/complete")
    public ResponseEntity<?> completeRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.completeRequest(requestId, token);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Long requestId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            requestService.cancelRequest(requestId, token);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
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
}
