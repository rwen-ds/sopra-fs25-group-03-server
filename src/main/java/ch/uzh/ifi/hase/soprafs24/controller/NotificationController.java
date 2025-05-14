package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.ErrorResponse;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final String AUTH_HEADER = "token";
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getUserNotifications(@RequestHeader(AUTH_HEADER) String token) {
        try {
            List<NotificationDTO> notificationDTOS = notificationService.getNotificationDTOS(token);
            return ResponseEntity.ok(notificationDTOS);
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }


    @PutMapping("/mark-read")
    public ResponseEntity<?> markNotificationsAsRead(@RequestHeader(AUTH_HEADER) String token) {
        try {
            notificationService.markNotificationsAsRead(token);
            return ResponseEntity.noContent().build();
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }

    }

    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read");
        }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader(AUTH_HEADER) String token) {
        try {
            Map<String, Boolean> response = notificationService.getUnreadNotifications(token);
            return ResponseEntity.ok(response);
        }

        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getReason()));
        }
    }


}
