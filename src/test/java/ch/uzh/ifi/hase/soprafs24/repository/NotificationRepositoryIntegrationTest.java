package ch.uzh.ifi.hase.soprafs24.repository;

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
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
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
        // 创建用户
        recipient = new User();
        recipient.setUsername("recipient");
        recipient.setPassword("password");
        recipient.setEmail("recipient@example.com");
        entityManager.persist(recipient);

        relatedUser = new User();
        relatedUser.setUsername("volunteer");
        relatedUser.setPassword("password");
        relatedUser.setEmail("volunteer@example.com");
        entityManager.persist(relatedUser);

        // 创建请求
        request = new Request();
        request.setTitle("Help needed");
        request.setDescription("Need help with something");
        request.setPoster(recipient);
        request.setStatus(RequestStatus.WAITING);
        entityManager.persist(request);

        // 创建通知
        notification1 = new Notification();
        notification1.setRecipientId(recipient.getId());
        notification1.setRelatedUserId(relatedUser.getId());
        notification1.setRelatedUsername(relatedUser.getUsername());
        notification1.setRequest(request);
        notification1.setType(NotificationType.VOLUNTEERED);
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setRead(false);
        entityManager.persist(notification1);

        // 第二个通知 (已读)
        notification2 = new Notification();
        notification2.setRecipientId(recipient.getId());
        notification2.setRelatedUserId(relatedUser.getId());
        notification2.setRelatedUsername(relatedUser.getUsername());
        notification2.setRequest(request);
        notification2.setType(NotificationType.VOLUNTEERING);
        notification2.setTimestamp(LocalDateTime.now().minusHours(1));
        notification2.setRead(true);
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
        assertFalse(unreadNotifications.get(0).isRead());
    }

    @Test
    public void existsByRecipientIdAndIsReadFalse_success() {
        boolean hasUnread = notificationRepository.existsByRecipientIdAndIsReadFalse(recipient.getId());
        assertTrue(hasUnread);

        // 将所有通知标记为已读
        notification1.setRead(true);
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
        newNotification.setRead(false);

        Notification savedNotification = notificationRepository.save(newNotification);

        assertNotNull(savedNotification.getId());
        assertEquals(NotificationType.ACCEPTED, savedNotification.getType());
        assertEquals(relatedUser.getId(), savedNotification.getRecipientId());
    }
} 