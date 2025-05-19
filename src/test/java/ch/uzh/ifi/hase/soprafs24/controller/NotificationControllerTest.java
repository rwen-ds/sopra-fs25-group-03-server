package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.NotificationType;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.NotificationDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthFilter authFilter;

    @Test
    public void getUserNotifications_success() throws Exception {
        // 准备测试数据
        NotificationDTO notification1 = new NotificationDTO();
        notification1.setRecipientId(1L);
        notification1.setRelatedUserId(2L);
        notification1.setRelatedUsername("volunteer");
        notification1.setRequestId(1L);
        notification1.setRequestTitle("Help needed");
        notification1.setType(NotificationType.VOLUNTEERED);
        notification1.setTimestamp(LocalDateTime.now());
        notification1.setIsRead(false);

        NotificationDTO notification2 = new NotificationDTO();
        notification2.setRecipientId(1L);
        notification2.setRelatedUserId(3L);
        notification2.setRelatedUsername("another_volunteer");
        notification2.setRequestId(2L);
        notification2.setRequestTitle("Another help");
        notification2.setType(NotificationType.ACCEPTED);
        notification2.setTimestamp(LocalDateTime.now().minusHours(1));
        notification2.setIsRead(true);

        List<NotificationDTO> notifications = Arrays.asList(notification1, notification2);

        // 模拟服务响应
        given(notificationService.getNotificationDTOS(anyString())).willReturn(notifications);

        // 构建请求
        MockHttpServletRequestBuilder getRequest = get("/notifications")
                .header("token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].recipientId", is(notification1.getRecipientId().intValue())))
                .andExpect(jsonPath("$[0].relatedUserId", is(notification1.getRelatedUserId().intValue())))
                .andExpect(jsonPath("$[0].relatedUsername", is(notification1.getRelatedUsername())))
                .andExpect(jsonPath("$[0].requestId", is(notification1.getRequestId().intValue())))
                .andExpect(jsonPath("$[0].requestTitle", is(notification1.getRequestTitle())))
                .andExpect(jsonPath("$[0].type", is(notification1.getType().toString())))
                .andExpect(jsonPath("$[0].isRead", is(notification1.getIsRead())))
                .andExpect(jsonPath("$[1].recipientId", is(notification2.getRecipientId().intValue())))
                .andExpect(jsonPath("$[1].type", is(notification2.getType().toString())));
    }

    @Test
    public void getUserNotifications_error() throws Exception {
        // 模拟服务抛出异常
        given(notificationService.getNotificationDTOS(anyString()))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid token"));

        // 构建请求
        MockHttpServletRequestBuilder getRequest = get("/notifications")
                .header("token", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid token")));
    }

    @Test
    public void markNotificationsAsRead_success() throws Exception {
        // 模拟服务行为 - 不抛出异常表示成功
        doNothing().when(notificationService).markNotificationsAsRead(anyString());

        // 构建请求
        MockHttpServletRequestBuilder putRequest = put("/notifications/mark-read")
                .header("token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    @Test
    public void markNotificationsAsRead_error() throws Exception {
        // 模拟服务抛出异常
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid token"))
                .when(notificationService).markNotificationsAsRead(anyString());

        // 构建请求
        MockHttpServletRequestBuilder putRequest = put("/notifications/mark-read")
                .header("token", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid token")));
    }

    @Test
    public void getUnreadNotifications_success() throws Exception {
        // 准备测试数据
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasUnread", true);

        // 模拟服务响应
        given(notificationService.getUnreadNotifications(anyString())).willReturn(response);

        // 构建请求
        MockHttpServletRequestBuilder getRequest = get("/notifications/unread")
                .header("token", "valid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasUnread", is(true)));
    }

    @Test
    public void getUnreadNotifications_error() throws Exception {
        // 模拟服务抛出异常
        given(notificationService.getUnreadNotifications(anyString()))
                .willThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid token"));

        // 构建请求
        MockHttpServletRequestBuilder getRequest = get("/notifications/unread")
                .header("token", "invalid-token")
                .contentType(MediaType.APPLICATION_JSON);

        // 执行请求并检验结果
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid token")));
    }

    @Test
    public void testMarkNotificationAsRead_Success() throws Exception {
        Long notificationId = 1L;
        doNothing().when(notificationService).markNotificationAsRead(notificationId);

        mockMvc.perform(MockMvcRequestBuilders.put("/notifications/{notificationId}/mark-read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Notification marked as read"));
    }

    @Test
    public void testMarkNotificationAsRead_Failure() throws Exception {
        Long notificationId = 1L;
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"))
                .when(notificationService).markNotificationAsRead(notificationId);

        mockMvc.perform(MockMvcRequestBuilders.put("/notifications/{notificationId}/mark-read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())  // 期望返回404 Not Found
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Notification not found"));
    }
}