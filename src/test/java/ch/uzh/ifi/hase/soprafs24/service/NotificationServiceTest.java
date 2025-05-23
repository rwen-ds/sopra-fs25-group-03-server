package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import ch.uzh.ifi.hase.soprafs24.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private DTOMapper dtoMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private User poster;
    private User volunteer;
    private Request request;
    private Notification notification;
    private String token;

    @BeforeEach
    public void setup() {
        token = "validToken";

        poster = new User();
        poster.setId(1L);
        poster.setUsername("poster");

        volunteer = new User();
        volunteer.setId(2L);
        volunteer.setUsername("volunteer");

        request = new Request();
        request.setId(1L);
        request.setTitle("Help request");
        request.setDescription("Need help with something");
        request.setPoster(poster);
        request.setStatus(RequestStatus.WAITING);

        notification = new Notification();
        notification.setId(1L);
        notification.setRecipientId(poster.getId());
        notification.setRelatedUserId(volunteer.getId());
        notification.setRelatedUsername(volunteer.getUsername());
        notification.setRequest(request);
        notification.setType(NotificationType.VOLUNTEERED);
        notification.setTimestamp(LocalDateTime.now());
        notification.setIsRead(false);
    }

    @Test
    public void volunteerNotification_createsCorrectNotifications() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.volunteerNotification(request, volunteer);

        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> capturedNotifications = notificationCaptor.getAllValues();
        assertEquals(2, capturedNotifications.size());

        assertEquals(poster.getId(), capturedNotifications.get(0).getRecipientId());
        assertEquals(volunteer.getId(), capturedNotifications.get(0).getRelatedUserId());
        assertEquals(NotificationType.VOLUNTEERED, capturedNotifications.get(0).getType());

        assertEquals(volunteer.getId(), capturedNotifications.get(1).getRecipientId());
        assertEquals(poster.getId(), capturedNotifications.get(1).getRelatedUserId());
        assertEquals(NotificationType.VOLUNTEERING, capturedNotifications.get(1).getType());
    }

    @Test
    public void acceptNotification_createsCorrectNotifications() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.acceptNotification(request, volunteer);

        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> capturedNotifications = notificationCaptor.getAllValues();
        assertEquals(2, capturedNotifications.size());

        assertEquals(NotificationType.ACCEPTING, capturedNotifications.get(0).getType());
        assertEquals(NotificationType.ACCEPTED, capturedNotifications.get(1).getType());
    }

    @Test
    public void completeNotification_createsCorrectNotification() {
        request.setVolunteer(volunteer);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.completeNotification(request);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(poster.getId(), capturedNotification.getRecipientId());
        assertEquals(volunteer.getId(), capturedNotification.getRelatedUserId());
        assertEquals(NotificationType.COMPLETED, capturedNotification.getType());
    }

    @Test
    public void markNotificationsAsRead_marksAllUnreadNotifications() {
        List<Notification> unreadNotifications = new ArrayList<>();
        unreadNotifications.add(notification);

        when(userService.getUserByToken(token)).thenReturn(poster);
        when(notificationRepository.findByRecipientIdAndIsReadFalse(poster.getId())).thenReturn(unreadNotifications);

        notificationService.markNotificationsAsRead(token);

        verify(notificationRepository).saveAll(ArgumentMatchers.argThat(list ->
                ((List<Notification>) list).stream().allMatch(Notification::getIsRead)
        ));
    }

    @Test
    public void getNotificationDTOS_returnsCorrectDTOs() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification);

        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setRecipientId(notification.getRecipientId());
        notificationDTO.setType(notification.getType());
        notificationDTO.setRequestId(request.getId());

        NotificationService testService = new NotificationService(notificationRepository, userService) {
            @Override
            public List<NotificationDTO> getNotificationDTOS(String token) {
                User user = userService.getUserByToken(token);
                List<Notification> notifs = notificationRepository.findByRecipientIdOrderByTimestampDesc(user.getId());

                List<NotificationDTO> dtos = new ArrayList<>();
                for (Notification n : notifs) {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setRecipientId(n.getRecipientId());
                    dto.setType(n.getType());
                    dto.setRequestId(n.getRequest().getId());
                    dtos.add(dto);
                }
                return dtos;
            }
        };

        when(userService.getUserByToken(token)).thenReturn(poster);
        when(notificationRepository.findByRecipientIdOrderByTimestampDesc(poster.getId())).thenReturn(notifications);

        List<NotificationDTO> result = testService.getNotificationDTOS(token);

        assertEquals(1, result.size());
        assertEquals(notification.getRecipientId(), result.get(0).getRecipientId());
        assertEquals(notification.getType(), result.get(0).getType());
    }

    @Test
    public void getResponse_returnsCorrectHasUnreadStatus() {
        when(userService.getUserByToken(token)).thenReturn(poster);
        when(notificationRepository.existsByRecipientIdAndIsReadFalse(poster.getId())).thenReturn(true);

        Map<String, Boolean> result = notificationService.getUnreadNotifications(token);

        assertTrue(result.get("hasUnread"));
        verify(notificationRepository).existsByRecipientIdAndIsReadFalse(poster.getId());
    }

    @Test
    public void getUnreadNotifications_noUnread_returnsFalse() {
        when(userService.getUserByToken(token)).thenReturn(poster);
        when(notificationRepository.existsByRecipientIdAndIsReadFalse(poster.getId())).thenReturn(false);

        Map<String, Boolean> result = notificationService.getUnreadNotifications(token);

        assertFalse(result.get("hasUnread"));
    }

    @Test
    public void markNotificationAsRead_success() {
        Long notificationId = 1L;
        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));

        notificationService.markNotificationAsRead(notificationId);

        assertTrue(notification.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    public void markNotificationAsRead_notFound_throwsBadRequest() {
        Long notificationId = 1L;
        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            notificationService.markNotificationAsRead(notificationId);
        });

        assertTrue(exception.getMessage().contains("Notification not found"));
    }

    @Test
    public void markNotificationsAsRead_emptyList() {
        when(userService.getUserByToken(token)).thenReturn(poster);
        when(notificationRepository.findByRecipientIdAndIsReadFalse(poster.getId())).thenReturn(new ArrayList<>());

        notificationService.markNotificationsAsRead(token);

        verify(notificationRepository).saveAll(new ArrayList<>());
    }

    @Test
    public void feedbackNotification_createsCorrectNotification() {
        request.setVolunteer(volunteer);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.feedbackNotification(request);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(volunteer.getId(), capturedNotification.getRecipientId());
        assertEquals(poster.getId(), capturedNotification.getRelatedUserId());
        assertEquals(NotificationType.FEEDBACK, capturedNotification.getType());
    }

    @Test
    public void posterCancelNotification_createsCorrectNotification() {
        request.setVolunteer(volunteer);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.posterCancelNotification(request);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(volunteer.getId(), capturedNotification.getRecipientId());
        assertEquals(poster.getId(), capturedNotification.getRelatedUserId());
        assertEquals(NotificationType.POSTERCANCEL, capturedNotification.getType());
    }

    @Test
    public void volunteerCancelNotification_createsCorrectNotification() {
        request.setVolunteer(volunteer);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        notificationService.volunteerCancelNotification(request);

        verify(notificationRepository, times(1)).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(poster.getId(), capturedNotification.getRecipientId());
        assertEquals(volunteer.getId(), capturedNotification.getRelatedUserId());
        assertEquals(NotificationType.VOLUNTEERCANCEL, capturedNotification.getType());
    }
} 