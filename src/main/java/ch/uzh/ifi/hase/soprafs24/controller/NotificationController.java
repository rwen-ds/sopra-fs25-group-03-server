package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.MessageDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.NotificationService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final String AUTH_HEADER = "token";
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationController(NotificationRepository notificationRepository, UserRepository userRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@RequestHeader(AUTH_HEADER) String token) {

        User user = userRepository.findByToken(token);
        List<Notification> notifications = notificationRepository.findByRecipientOrderByTimestampDesc(user);

        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(DTOMapper.INSTANCE::convertEntityToNotificationDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notificationDTOs);
    }


    @PutMapping("/mark-read")
    public ResponseEntity<Void> markNotificationsAsRead(@RequestHeader(AUTH_HEADER) String token) {

        User user = userRepository.findByToken(token);
        notificationService.markNotificationsAsRead(user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Boolean>> getUnreadNotifications(@RequestHeader(AUTH_HEADER) String token) {
        User user = userRepository.findByToken(token);
        boolean hasUnread = notificationRepository.existsByRecipientAndIsReadFalse(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUnread", hasUnread);

        return ResponseEntity.ok(response);
    }
}
