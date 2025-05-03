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

    public void volunteerNotification(Request request, User volunteer) {
        Notification notification1 = new Notification();
        notification1.setRecipient(request.getPoster());
        notification1.setRelatedUser(volunteer);
        notification1.setRequest(request);
        notification1.setContent("Volunteer " + volunteer.getUsername() + " is applying to help your request '" + request.getTitle() + "'.");
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setType(NotificationType.VOLUNTEERED);
        notification1.setRead(false);

        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setRecipient(volunteer);
        notification2.setRelatedUser(request.getPoster());
        notification2.setRequest(request);
        notification2.setContent("You have volunteered to help '" + request.getTitle() + "' posted by " +
                request.getPoster().getUsername());
        notification2.setTimestamp(LocalDateTime.now());
        notification2.setType(NotificationType.VOLUNTEERING);
        notification2.setRead(false);

        notificationRepository.save(notification2);
    }

    public void acceptNotification(Request request, User volunteer) {
        Notification notification1 = new Notification();
        notification1.setRecipient(request.getPoster());
        notification1.setRelatedUser(volunteer);
        notification1.setRequest(request);
        notification1.setContent("You accepted " + volunteer.getUsername() + "'s help for your request '" + request.getTitle() + "'.");
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setType(NotificationType.ACCEPTING);
        notification1.setRead(false);

        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setRecipient(volunteer);
        notification2.setRelatedUser(request.getPoster());
        notification2.setRequest(request);
        notification2.setContent("Your volunteer for request " + request.getTitle() + " is accepted!");
        notification2.setTimestamp(LocalDateTime.now());
        notification2.setType(NotificationType.ACCEPTED);
        notification2.setRead(false);

        notificationRepository.save(notification2);
    }

    public void completeNotification(Request request) {
        Notification notification = new Notification();
        notification.setRecipient(request.getPoster());
        notification.setRelatedUser(request.getVolunteer());
        notification.setRequest(request);
        notification.setContent("Your request '" + request.getTitle() + "' is completed by volunteer " + request.getVolunteer().getUsername() + "!");
        notification.setTimestamp(LocalDateTime.now());
        notification.setType(NotificationType.COMPLETED);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    public void markNotificationsAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientAndIsReadFalse(user);
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}
