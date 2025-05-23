package ch.uzh.ifi.hase.soprafs24.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ch.uzh.ifi.hase.soprafs24.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@DataJpaTest
public class NotificationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User recipient;
    private User relatedUser;
    private Request request;
    private Notification notification1;
    private Notification notification2;

    @BeforeEach
    public void setup() {
        recipient = new User();
        recipient.setUsername("recipientUser");
        recipient.setPassword("password");
        recipient.setEmail("recipient@edu.example.com");
        recipient.setCreationDate(LocalDate.now());
        recipient.setStatus(UserStatus.OFFLINE);
        recipient.setToken("token-recipient");
        recipient.setIsAdmin(false);
        entityManager.persist(recipient);

        relatedUser = new User();
        relatedUser.setUsername("volunteerUser");
        relatedUser.setPassword("password");
        relatedUser.setEmail("volunteer@edu.example.com");
        relatedUser.setCreationDate(LocalDate.now());
        relatedUser.setStatus(UserStatus.OFFLINE);
        relatedUser.setToken("token-volunteer");
        relatedUser.setIsAdmin(false);
        entityManager.persist(relatedUser);

        request = new Request();
        request.setTitle("Help needed");
        request.setDescription("Need help with something");
        request.setPoster(recipient);
        request.setStatus(RequestStatus.WAITING);
        request.setCreationDate(LocalDate.now());
        request.setEmergencyLevel(RequestEmergencyLevel.MEDIUM);
        entityManager.persist(request);

        notification1 = new Notification();
        notification1.setRecipientId(recipient.getId());
        notification1.setRelatedUserId(relatedUser.getId());
        notification1.setRelatedUsername(relatedUser.getUsername());
        notification1.setRequest(request);
        notification1.setType(NotificationType.VOLUNTEERED);
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setIsRead(false);
        entityManager.persist(notification1);

        notification2 = new Notification();
        notification2.setRecipientId(recipient.getId());
        notification2.setRelatedUserId(relatedUser.getId());
        notification2.setRelatedUsername(relatedUser.getUsername());
        notification2.setRequest(request);
        notification2.setType(NotificationType.VOLUNTEERING);
        notification2.setTimestamp(LocalDateTime.now().minusHours(1));
        notification2.setIsRead(true);
        entityManager.persist(notification2);

        entityManager.flush();
    }

    @Test
    public void findByRecipientIdOrderByTimestampDesc_success() {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByTimestampDesc(recipient.getId());

        assertEquals(2, notifications.size());
        assertEquals(NotificationType.VOLUNTEERED, notifications.get(0).getType());
        assertEquals(NotificationType.VOLUNTEERING, notifications.get(1).getType());
    }

    @Test
    public void findByRecipientIdAndIsReadFalse_success() {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(recipient.getId());

        assertEquals(1, unreadNotifications.size());
        assertEquals(NotificationType.VOLUNTEERED, unreadNotifications.get(0).getType());
        assertFalse(unreadNotifications.get(0).getIsRead());
    }

    @Test
    public void existsByRecipientIdAndIsReadFalse_success() {
        boolean hasUnread = notificationRepository.existsByRecipientIdAndIsReadFalse(recipient.getId());
        assertTrue(hasUnread);

        notification1.setIsRead(true);
        entityManager.persist(notification1);
        entityManager.flush();

        hasUnread = notificationRepository.existsByRecipientIdAndIsReadFalse(recipient.getId());
        assertFalse(hasUnread);
    }

    @Test
    public void saveNotification_success() {
        Notification newNotification = new Notification();
        newNotification.setRecipientId(relatedUser.getId());
        newNotification.setRelatedUserId(recipient.getId());
        newNotification.setRelatedUsername(recipient.getUsername());
        newNotification.setRequest(request);
        newNotification.setType(NotificationType.ACCEPTED);
        newNotification.setTimestamp(LocalDateTime.now());
        newNotification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(newNotification);

        assertNotNull(savedNotification.getId());
        assertEquals(NotificationType.ACCEPTED, savedNotification.getType());
        assertEquals(relatedUser.getId(), savedNotification.getRecipientId());
    }
} 