package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private void createAndSaveNotification(User recipient, User relatedUser, Request request, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setRelatedUser(relatedUser);
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

    public void markNotificationsAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientAndIsReadFalse(user);
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
}
