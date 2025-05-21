package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.NotificationRepository;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FeedbackDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RequestService requestService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private DTOMapper dtoMapper;

    private User createSampleUser(Long id, String username, String token) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@edu.example.com");
        user.setPassword("password");
        user.setCreationDate(LocalDate.now());
        user.setToken(token);
        user.setIsAdmin(false);
        return user;
    }

    private Request createSampleRequest(Long id, String title, RequestStatus status, User poster) {
        Request request = new Request();
        request.setId(id);
        request.setTitle(title);
        request.setDescription("Sample description");
        request.setContactInfo("sample@example.com");
        request.setLocation("Sample location");
        request.setCreationDate(LocalDate.now());
        request.setStatus(status);
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setPoster(poster);
        return request;
    }

    private Request createSampleRequest(Long id, String title, RequestStatus status, User poster, User volunteer) {
        Request request = new Request();
        request.setId(id);
        request.setTitle(title);
        request.setDescription("Sample description");
        request.setContactInfo("sample@example.com");
        request.setLocation("Sample location");
        request.setCreationDate(LocalDate.now());
        request.setStatus(status);
        request.setEmergencyLevel(RequestEmergencyLevel.LOW);
        request.setPoster(poster);
        request.setVolunteer(volunteer);
        return request;
    }

    @Test
    public void testCreateRequest_success() {
        Long posterId = 100L;
        User poster = createSampleUser(posterId, "posterUser", "adminToken");
        when(userService.getUserById(posterId)).thenReturn(poster);

        Request newRequest = new Request();
        newRequest.setTitle("Test Request Title");
        newRequest.setDescription("Test Request Description");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.LOW);

        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        doNothing().when(requestRepository).flush();

        Request createdRequest = requestService.createRequest(newRequest, posterId);
        assertNotNull(createdRequest);
        assertNotNull(createdRequest.getId());
        assertEquals(RequestStatus.WAITING, createdRequest.getStatus());
        assertNotNull(createdRequest.getCreationDate());

        assertNotNull(createdRequest.getPoster());
        assertEquals("posterUser", createdRequest.getPoster().getUsername());
    }

    @Test
    public void testGetRequestById_success() {
        Request request = new Request();
        request.setId(1L);
        request.setTitle("Some Title");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Request foundRequest = requestService.getRequestById(1L);
        assertNotNull(foundRequest);
        assertEquals(1L, foundRequest.getId());
    }

    @Test
    public void testGetRequestById_notFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.getRequestById(1L);
        });
        assertTrue(exception.getMessage().contains("Request not found with id: 1"));
    }

    @Test
    public void testUpdateRequest_success() {

        User poster = createSampleUser(100L, "posterUser", "token");
        Request existingRequest = createSampleRequest(1L, "Old Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userService.getUserByToken("token")).thenReturn(poster);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Request update = new Request();
        update.setTitle("New Title");
        update.setDescription("New Description");


        Request updatedRequest = requestService.updateRequest(1L, update, "token");
        assertEquals("New Title", updatedRequest.getTitle());
        assertEquals("New Description", updatedRequest.getDescription());
    }

    @Test
    public void testUpdateRequest_invalidToken_throwsUnauthorized() {

        User poster = createSampleUser(100L, "posterUser", "correctToken");
        Request existingRequest = createSampleRequest(1L, "Old Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));

        User fakeUser = createSampleUser(200L, "notAdmin", "wrongToken");
        when(userService.getUserByToken("wrongToken")).thenReturn(fakeUser);

        Request update = new Request();
        update.setTitle("New Title");


        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.updateRequest(1L, update, "wrongToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testDeleteRequest_success() {
        User poster = createSampleUser(100L, "posterUser", "token");
        Request existingRequest = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(userService.getUserByToken("token")).thenReturn(poster);

        assertDoesNotThrow(() -> requestService.deleteRequest(1L, "token", "Some reason"));

        verify(requestRepository, times(1)).save(existingRequest);
        verify(notificationRepository, times(1)).deleteByRequest(existingRequest);
    }

    @Test
    public void testDeleteRequest_invalidToken_throwsUnauthorized() {
        User poster = createSampleUser(100L, "posterUser", "correctToken");
        Request existingRequest = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(existingRequest));

        User fakeUser = createSampleUser(200L, "notAdmin", "wrongToken");
        when(userService.getUserByToken("wrongToken")).thenReturn(fakeUser);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.deleteRequest(1L, "wrongToken", "");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testAcceptRequest_success() {
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.VOLUNTEERED, createSampleUser(100L, "posterUser", "token"), volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userService.getUserById(200L)).thenReturn(volunteer);

        requestService.acceptRequest(1L, 200L);
        assertEquals(RequestStatus.ACCEPTING, request.getStatus());
        assertEquals(volunteer, request.getVolunteer());
    }

    @Test
    public void testAcceptRequest_invalidStatus_throwsBadRequest() {

        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, createSampleUser(100L, "posterUser", "token"));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        createSampleUser(200L, "volunteerUser", "volunteerToken");

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.acceptRequest(1L, 200L);
        });
        assertTrue(exception.getMessage().contains("Request is not in a state to accept a volunteer"));
    }

    @Test
    public void testCompleteRequest_success() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestService.completeRequest(1L, "volunteerToken");
        assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }

    @Test
    public void testCompleteRequest_invalidStatus_throwsBadRequest() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(1L, "volunteerToken");
        });
        assertTrue(exception.getMessage().contains("Only accepted requests can be completed"));
    }

    @Test
    public void testCancelRequest_success() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(100L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.ACCEPTING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestService.cancelRequest(1L, "token");
        assertEquals(RequestStatus.WAITING, request.getStatus());
    }

    @Test
    public void testCancelRequest_invalidStatus_throwsBadRequest() {
        User poster = createSampleUser(100L, "posterUser", "token");
        User volunteer = createSampleUser(100L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Title", RequestStatus.WAITING, poster, volunteer);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(1L, "token");
        });
        assertTrue(exception.getMessage().contains("Only accepted or volunteered requests can be canceled"));
    }

    @Test
    public void testGetRequests_and_GetWaitingRequests() {
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.WAITING, createSampleUser(100L, "posterUser", "token"));
        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.ACCEPTING, createSampleUser(100L, "posterUser", "token"));
        Request request3 = createSampleRequest(3L, "Request 3", RequestStatus.WAITING, createSampleUser(100L, "posterUser", "token"));

        User admin = createSampleUser(200L, "admin", "adminToken");
        when(userService.getUserByToken("adminToken")).thenReturn(admin);

        when(requestRepository.findAll()).thenReturn(List.of(request1, request2, request3));
        when(requestRepository.findByStatus(RequestStatus.WAITING)).thenReturn(List.of(request1, request3));


        List<Request> allRequests = requestService.getRequests("adminToken");
        assertEquals(3, allRequests.size());

        List<Request> waitingRequests = requestService.getWaitingRequests();
        assertEquals(2, waitingRequests.size());
        for (Request req : waitingRequests) {
            assertEquals(RequestStatus.WAITING, req.getStatus());
        }
    }

    @Test
    public void testVolunteerRequest_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.WAITING, poster);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getUserByToken("volunteerToken")).thenReturn(volunteer);
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        requestService.volunteerRequest(1L, "volunteerToken");

        // 验证结果
        assertEquals(RequestStatus.VOLUNTEERED, request.getStatus());
        assertEquals(volunteer, request.getVolunteer());
        verify(notificationService).volunteerNotification(request, volunteer);
    }

    @Test
    public void testVolunteerRequest_ownRequest_throwsBadRequest() {
        // 设置请求和用户 - 用户尝试为自己的请求做志愿者
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.WAITING, poster);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getUserByToken("posterToken")).thenReturn(poster);

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.volunteerRequest(1L, "posterToken");
        });
        assertTrue(exception.getMessage().contains("You cannot volunteer for your own request"));
    }

    @Test
    public void testVolunteerRequest_notWaiting_throwsBadRequest() {
        // 设置请求和用户 - 请求已经有志愿者了
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer1 = createSampleUser(200L, "volunteer1", "volunteer1Token");
        User volunteer2 = createSampleUser(300L, "volunteer2", "volunteer2Token");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.VOLUNTEERED, poster, volunteer1);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getUserByToken("volunteer2Token")).thenReturn(volunteer2);

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.volunteerRequest(1L, "volunteer2Token");
        });
        assertTrue(exception.getMessage().contains("You can only volunteer for requests that are still waiting"));
    }

    @Test
    public void testMarkRequestAsDone_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.COMPLETED, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        requestService.markRequestAsDone(1L, "posterToken");

        // 验证结果
        assertEquals(RequestStatus.DONE, request.getStatus());
    }

    @Test
    public void testMarkRequestAsDone_invalidToken_throwsUnauthorized() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.COMPLETED, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.markRequestAsDone(1L, "wrongToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testMarkRequestAsDone_invalidStatus_throwsBadRequest() {
        // 设置请求和用户 - 请求状态不是COMPLETED
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.ACCEPTING, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.markRequestAsDone(1L, "posterToken");
        });
        assertTrue(exception.getMessage().contains("Only completed requests can be mark as done"));
    }

    @Test
    public void testFeedback_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.DONE, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        requestService.feedback(1L, "posterToken", "Great help!", 5);

        // 验证结果
        assertEquals("Great help!", request.getFeedback());
        assertEquals(5, request.getRating());
        verify(notificationService).feedbackNotification(request);
    }

    @Test
    public void testFeedback_invalidToken_throwsUnauthorized() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.DONE, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.feedback(1L, "wrongToken", "Great help!", 5);
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testFeedback_invalidStatus_throwsBadRequest() {
        // 设置请求和用户 - 请求状态不是DONE
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.COMPLETED, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.feedback(1L, "posterToken", "Great help!", 5);
        });
        assertTrue(exception.getMessage().contains("Only requests be marked as done can be feedback"));
    }

    @Test
    public void testGetRequestByPoster_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.WAITING, poster);
        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.COMPLETED, poster);
        List<Request> posterRequests = Arrays.asList(request1, request2);

        // 设置模拟行为
        when(userService.getUserByToken("posterToken")).thenReturn(poster);
        when(requestRepository.findByPoster(poster)).thenReturn(posterRequests);

        // 执行测试
        List<Request> result = requestService.getRequestByPoster("posterToken");

        // 验证结果
        assertEquals(2, result.size());
        assertEquals("Request 1", result.get(0).getTitle());
        assertEquals("Request 2", result.get(1).getTitle());
    }

    @Test
    public void testGetFeedbackById_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");

        // 创建两个有反馈的请求和一个没有反馈的请求
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.DONE, poster, volunteer);
        request1.setFeedback("Excellent service");
        request1.setRating(5);

        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.DONE, poster, volunteer);
        request2.setFeedback("Good job");
        request2.setRating(4);

        Request request3 = createSampleRequest(3L, "Request 3", RequestStatus.DONE, poster, volunteer);
        // 没有设置反馈

        List<Request> volunteerRequests = Arrays.asList(request1, request2, request3);

        // 设置模拟行为
        when(requestRepository.findByVolunteerId(200L)).thenReturn(volunteerRequests);

        // 执行测试
        List<FeedbackDTO> feedbacks = requestService.getFeedbackById(200L);

        // 验证结果
        assertEquals(2, feedbacks.size());
        assertEquals("Excellent service", feedbacks.get(0).getFeedback());
        assertEquals(5, feedbacks.get(0).getRating());
        assertEquals("Good job", feedbacks.get(1).getFeedback());
        assertEquals(4, feedbacks.get(1).getRating());
    }

    @Test
    public void testGetPostRequestsByUserId_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.WAITING, poster);
        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.COMPLETED, poster);
        List<Request> requests = Arrays.asList(request1, request2);

        // 设置模拟行为
        when(requestRepository.findByPosterId(100L)).thenReturn(requests);
        
        // 设置DTOMapper的行为
        RequestGetDTO dto1 = new RequestGetDTO();
        dto1.setId(1L);
        RequestGetDTO dto2 = new RequestGetDTO();
        dto2.setId(2L);
        
        when(dtoMapper.convertEntityToRequestGetDTO(request1)).thenReturn(dto1);
        when(dtoMapper.convertEntityToRequestGetDTO(request2)).thenReturn(dto2);
        
        // 设置RequestService的DTOMapper字段
        ReflectionTestUtils.setField(requestService, "dtoMapper", dtoMapper);

        // 执行测试
        List<RequestGetDTO> result = requestService.getPostRequestsByUserId(100L);

        // 验证结果
        assertEquals(2, result.size());
        verify(dtoMapper, times(1)).convertEntityToRequestGetDTO(request1);
        verify(dtoMapper, times(1)).convertEntityToRequestGetDTO(request2);
    }

    @Test
    public void testGetVolunteerRequestsByUserId_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request1 = createSampleRequest(1L, "Request 1", RequestStatus.VOLUNTEERED, poster, volunteer);
        Request request2 = createSampleRequest(2L, "Request 2", RequestStatus.COMPLETED, poster, volunteer);
        List<Request> requests = Arrays.asList(request1, request2);

        // 设置模拟行为
        when(requestRepository.findByVolunteerId(200L)).thenReturn(requests);
        
        // 设置DTOMapper的行为
        RequestGetDTO dto1 = new RequestGetDTO();
        dto1.setId(1L);
        RequestGetDTO dto2 = new RequestGetDTO();
        dto2.setId(2L);
        
        when(dtoMapper.convertEntityToRequestGetDTO(request1)).thenReturn(dto1);
        when(dtoMapper.convertEntityToRequestGetDTO(request2)).thenReturn(dto2);
        
        // 设置RequestService的DTOMapper字段
        ReflectionTestUtils.setField(requestService, "dtoMapper", dtoMapper);

        // 执行测试
        List<RequestGetDTO> result = requestService.getVolunteerRequestsByUserId(200L);

        // 验证结果
        assertEquals(2, result.size());
        verify(dtoMapper, times(1)).convertEntityToRequestGetDTO(request1);
        verify(dtoMapper, times(1)).convertEntityToRequestGetDTO(request2);
    }

    @Test
    public void testCreateRequest_missingTitle_throwsBadRequest() {
        // 创建缺少标题的请求
        Request newRequest = new Request();
        newRequest.setDescription("Test Description");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.LOW);

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(newRequest, 1L);
        });
        assertTrue(exception.getMessage().contains("Title cannot be empty"));
    }

    @Test
    public void testCreateRequest_missingDescription_throwsBadRequest() {
        // 创建缺少描述的请求
        Request newRequest = new Request();
        newRequest.setTitle("Test Title");
        newRequest.setEmergencyLevel(RequestEmergencyLevel.LOW);

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(newRequest, 1L);
        });
        assertTrue(exception.getMessage().contains("Description cannot be empty"));
    }

    @Test
    public void testCreateRequest_missingEmergencyLevel_throwsBadRequest() {
        // 创建缺少紧急程度的请求
        Request newRequest = new Request();
        newRequest.setTitle("Test Title");
        newRequest.setDescription("Test Description");

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.createRequest(newRequest, 1L);
        });
        assertTrue(exception.getMessage().contains("Emergency level must be set"));
    }

    @Test
    public void testGetRequests_notAdmin_throwsUnauthorized() {
        // 设置非管理员用户
        User user = createSampleUser(100L, "regularUser", "userToken");
        when(userService.getUserByToken("userToken")).thenReturn(user);

        // 执行测试并验证异常
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.getRequests("userToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testCompleteRequest_invalidToken_throwsUnauthorized() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.ACCEPTING, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常 - 使用发布者的令牌而不是志愿者的令牌
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.completeRequest(1L, "posterToken");
        });
        assertTrue(exception.getMessage().contains("Invalid token"));
    }

    @Test
    public void testCancelRequest_volunteerCancels_success() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.VOLUNTEERED, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试 - 志愿者取消
        requestService.cancelRequest(1L, "volunteerToken");

        // 验证结果
        assertEquals(RequestStatus.WAITING, request.getStatus());
        assertNull(request.getVolunteer());
        verify(notificationService).volunteerCancelNotification(request);
    }

    @Test
    public void testCancelRequest_invalidUser_throwsUnauthorized() {
        // 设置请求和用户
        User poster = createSampleUser(100L, "posterUser", "posterToken");
        User volunteer = createSampleUser(200L, "volunteerUser", "volunteerToken");
        User otherUser = createSampleUser(300L, "otherUser", "otherToken");
        Request request = createSampleRequest(1L, "Help needed", RequestStatus.VOLUNTEERED, poster, volunteer);

        // 设置模拟行为
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // 执行测试并验证异常 - 使用其他用户的令牌
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            requestService.cancelRequest(1L, "otherToken");
        });
        assertTrue(exception.getMessage().contains("Invalid user"));
    }
}