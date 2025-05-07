package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    private void createAndSaveNotification(User recipient, User relatedUser, Request request, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipientId(recipient.getId());
        notification.setRelatedUserId(relatedUser.getId());
        notification.setRelatedUsername(relatedUser.getUsername());
        notification.setRequest(request);
        notification.setTimestamp(LocalDateTime.now());
        notification.setType(type);
        notification.setRead(false);
        notificationRepository.save(notification);
    }


    public void volunteerNotification(Request request, User volunteer) {
        createAndSaveNotification(request.getPoster(), volunteer, request, NotificationType.VOLUNTEERED);
        createAndSaveNotification(volunteer, request.getPoster(), request, NotificationType.VOLUNTEERING);
    }

    public void acceptNotification(Request request, User volunteer) {
        createAndSaveNotification(request.getPoster(), volunteer, request, NotificationType.ACCEPTING);
        createAndSaveNotification(volunteer, request.getPoster(), request, NotificationType.ACCEPTED);
    }

    public void completeNotification(Request request) {
        createAndSaveNotification(request.getPoster(), request.getVolunteer(), request, NotificationType.COMPLETED);
    }

    public void markNotificationsAsRead(String token) {
        User user = userService.getUserByToken(token);
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalse(user.getId());
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    public void feedbackNotification(Request request) {
        createAndSaveNotification(request.getVolunteer(), request.getPoster(), request, NotificationType.FEEDBACK);
    }

    public void posterCancelNotification(Request request) {
        createAndSaveNotification(request.getVolunteer(), request.getPoster(), request, NotificationType.POSTERCALCEL);
    }

    public void volunteerCancelNotification(Request request) {
        createAndSaveNotification(request.getPoster(), request.getVolunteer(), request, NotificationType.VOLUNTEERCALCEL);
    }

    public List<NotificationDTO> getNotificationDTOS(String token) {
        User user = userService.getUserByToken(token);
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByTimestampDesc(user.getId());

        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(DTOMapper.INSTANCE::convertEntityToNotificationDTO)
                .collect(Collectors.toList());
        return notificationDTOs;
    }

    public Map<String, Boolean> getResponse(String token) {
        User user = userService.getUserByToken(token);
        boolean hasUnread = notificationRepository.existsByRecipientIdAndIsReadFalse(user.getId());
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUnread", hasUnread);
        return response;
    }
}
