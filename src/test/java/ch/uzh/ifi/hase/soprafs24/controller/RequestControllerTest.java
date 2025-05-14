package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.DeleteRequestDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.FeedbackDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.RequestService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)  // close Filter
public class RequestControllerTest {

    private static final String AUTH_HEADER = "token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthFilter authFilter;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RequestRepository requestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * to Json
     */
    private String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        }
        catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON error: " + e.getMessage());
        }
    }

    @Test
    void createRequest_success() throws Exception {
        RequestPostDTO dto = new RequestPostDTO();
        dto.setTitle("Test Title");
        dto.setDescription("Test Description");

        Request created = new Request();
        created.setId(1L);

        when(requestService.createRequest(any(Request.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/requests?posterId=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateRequest_titleIsEmpty() throws Exception {
        RequestPostDTO requestPostDTO = new RequestPostDTO();
        requestPostDTO.setTitle("");
        requestPostDTO.setDescription("Sample Description");
        requestPostDTO.setEmergencyLevel(RequestEmergencyLevel.HIGH);

        Long posterId = 1L;

        when(requestService.createRequest(any(Request.class), eq(posterId)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be empty"));

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestPostDTO))
                        .param("posterId", posterId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Title cannot be empty"));
    }


    @Test
    public void testGetAllRequests_serviceThrowsException() throws Exception {
        String token = "valid-token";
        String errorMessage = "Invalid token";

        when(requestService.getRequests(token))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage));

        mockMvc.perform(get("/requests")
                        .header(AUTH_HEADER, token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }


    @Test
    void getRequestById_success() throws Exception {
        Request found = new Request();
        found.setId(1L);
        found.setTitle("Sample Title");
        found.setCreationDate(LocalDate.now());

        when(requestService.getRequestById(eq(1L))).thenReturn(found);

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample Title"));
    }

    @Test
    public void testGetRequestById_notFound() throws Exception {
        Long requestId = 1L;
        String errorMessage = "Request not found with id: " + requestId;

        when(requestService.getRequestById(requestId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }


    @Test
    void updateRequest_success() throws Exception {
        RequestPostDTO dto = new RequestPostDTO();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Desc");

        Request updated = new Request();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Desc");

        when(requestService.updateRequest(eq(1L), any(Request.class), eq("validToken"))).thenReturn(updated);

        mockMvc.perform(put("/requests/1")
                        .header(AUTH_HEADER, "validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Desc"));
    }

    @Test
    public void testUpdateRequest_Unauthorized() throws Exception {
        Long requestId = 1L;
        String token = "invalid-token";

        when(requestService.updateRequest(eq(requestId), any(Request.class), eq(token)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        RequestPostDTO requestPostDTO = new RequestPostDTO();
        requestPostDTO.setTitle("Updated Title");
        requestPostDTO.setDescription("Updated Description");

        mockMvc.perform(put("/requests/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestPostDTO))
                        .header(AUTH_HEADER, token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }


    @Test
    void deleteRequest_success() throws Exception {
        DeleteRequestDTO deleteDTO = new DeleteRequestDTO();
        deleteDTO.setReason("Some valid reason");

        doNothing().when(requestService).deleteRequest(eq(1L), eq("validToken"), eq("Some valid reason"));

        mockMvc.perform(put("/requests/1/delete")
                        .header(AUTH_HEADER, "validToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Some valid reason\"}"))
                .andExpect(status().isOk());  // Expecting HTTP 200 OK status
    }

    @Test
    public void testDeleteRequest_Unauthorized() throws Exception {
        String token = "validToken";
        String reason = "Some valid reason";
        Long requestId = 1L;

        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
                .when(requestService).deleteRequest(eq(requestId), eq(token), eq(reason));

        // Act & Assert
        mockMvc.perform(put("/requests/{requestId}/delete", requestId)
                        .header(AUTH_HEADER, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"" + reason + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }


    @Test
    void acceptRequest_success() throws Exception {
        doNothing().when(requestService).acceptRequest(eq(1L), eq(2L));

        mockMvc.perform(put("/requests/1/accept?volunteerId=2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAcceptRequest_BadRequest() throws Exception {
        Long requestId = 1L;
        Long volunteerId = 2L;

        Request request = new Request();
        request.setStatus(RequestStatus.WAITING);
        when(requestService.getRequestById(requestId)).thenReturn(request);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is not in a state to accept a volunteer"))
                .when(requestService).acceptRequest(requestId, volunteerId);

        mockMvc.perform(put("/requests/{requestId}/accept", requestId)
                        .param("volunteerId", String.valueOf(volunteerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request is not in a state to accept a volunteer"));
    }


    @Test
    void completeRequest_success() throws Exception {
        doNothing().when(requestService).completeRequest(eq(1L), eq("validToken"));

        mockMvc.perform(put("/requests/1/complete")
                        .header(AUTH_HEADER, "validToken"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCompleteRequest_BadRequest() throws Exception {
        Long requestId = 1L;
        String token = "validToken";

        Request request = new Request();
        request.setStatus(RequestStatus.WAITING);
        request.setVolunteer(null);

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only accepted requests can be completed"))
                .when(requestService).completeRequest(requestId, token);

        mockMvc.perform(put("/requests/{requestId}/complete", requestId)
                        .header(AUTH_HEADER, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only accepted requests can be completed"));
    }


    @Test
    void cancelRequest_success() throws Exception {
        doNothing().when(requestService).cancelRequest(eq(1L), eq("validToken"));

        mockMvc.perform(put("/requests/1/cancel")
                        .header(AUTH_HEADER, "validToken"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCancelRequest_BadRequest() throws Exception {
        Long requestId = 1L;
        String token = "validToken";

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "It's not volunteered"))
                .when(requestService).cancelRequest(requestId, token);

        mockMvc.perform(put("/requests/{requestId}/cancel", requestId)
                        .header(AUTH_HEADER, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("It's not volunteered"));
    }


    @Test
    void getAllRequests_success() throws Exception {
        Request req = new Request();
        req.setId(1L);
        req.setTitle("AllRequests Title");
        req.setCreationDate(LocalDate.now());

        when(requestService.getRequests(eq("validToken"))).thenReturn(Collections.singletonList(req));

        mockMvc.perform(get("/requests")
                        .header(AUTH_HEADER, "validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("AllRequests Title"));
    }

    @Test
    public void testGetRequestByPosterId_success() throws Exception {
        Request req = new Request();
        req.setId(1L);
        req.setTitle("AllRequests Title");
        req.setCreationDate(LocalDate.now());

        when(requestService.getRequestByPoster(eq("validToken"))).thenReturn(Collections.singletonList(req));

        mockMvc.perform(get("/requests/me")
                        .header(AUTH_HEADER, "validToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("AllRequests Title"));

    }

    @Test
    public void testGetRequestByPosterId_Exception() throws Exception {
        when(requestService.getRequestByPoster(eq("validToken"))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No requests found"));

        mockMvc.perform(get("/requests/me")
                        .header(AUTH_HEADER, "validToken"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No requests found"));
    }

    @Test
    public void testVolunteerRequest_Success() throws Exception {
        Long requestId = 1L;
        String token = "validToken";

        mockMvc.perform(put("/requests/{requestId}/volunteer", requestId)
                        .header(AUTH_HEADER, token))
                .andExpect(status().isOk());
    }

    @Test
    public void testVolunteerRequest_BadRequest_SelfVolunteer() throws Exception {
        Long requestId = 1L;
        String token = "userToken";

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot volunteer for your own request."))
                .when(requestService).volunteerRequest(eq(requestId), eq(token));

        mockMvc.perform(put("/requests/{requestId}/volunteer", requestId)
                        .header(AUTH_HEADER, token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot volunteer for your own request."));
    }

    @Test
    public void testGetActiveRequests_success() throws Exception {
        Request req = new Request();
        req.setId(1L);
        req.setTitle("Active Request Title");
        req.setCreationDate(LocalDate.now());

        when(requestService.getWaitingRequests()).thenReturn(Collections.singletonList(req));

        mockMvc.perform(get("/requests/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Active Request Title"));
    }

    @Test
    public void testGetActiveRequests_InternalServerError() throws Exception {
        when(requestService.getWaitingRequests()).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));

        mockMvc.perform(get("/requests/active"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    public void testMarkRequestAsDone_success() throws Exception {
        Long requestId = 1L;
        String validToken = "validToken";

        Request req = new Request();
        req.setId(requestId);
        req.setStatus(RequestStatus.COMPLETED);
        req.setPoster(new User());  // Mock user as poster
        req.getPoster().setToken(validToken);

        when(requestService.getRequestById(requestId)).thenReturn(req);

        mockMvc.perform(put("/requests/{requestId}/done", requestId)
                        .header(AUTH_HEADER, validToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testMarkRequestAsDone_badRequest() throws Exception {
        Long requestId = 1L;
        String validToken = "validToken";

        Request req = new Request();
        req.setId(requestId);
        req.setStatus(RequestStatus.WAITING);  // Status is not COMPLETED
        req.setPoster(new User());
        req.getPoster().setToken(validToken);

        when(requestService.getRequestById(requestId)).thenReturn(req);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only completed requests can be mark as done"))
                .when(requestService).markRequestAsDone(eq(requestId), eq(validToken));

        mockMvc.perform(put("/requests/{requestId}/done", requestId)
                        .header(AUTH_HEADER, validToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only completed requests can be mark as done"));
    }

    @Test
    public void testSendFeedback_success() throws Exception {
        Long requestId = 1L;
        String validToken = "validToken";
        String feedbackMessage = "This is feedback message";
        int rating = 5;

        Request req = new Request();
        req.setId(requestId);
        req.setStatus(RequestStatus.DONE);
        req.setPoster(new User());
        req.getPoster().setToken(validToken);

        when(requestService.getRequestById(requestId)).thenReturn(req);

        doNothing().when(requestService).feedback(eq(requestId), eq(validToken), eq(feedbackMessage), eq(rating));

        mockMvc.perform(put("/requests/{requestId}/feedback", requestId)
                        .header(AUTH_HEADER, validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"feedback\": \"" + feedbackMessage + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSendFeedback_badRequest() throws Exception {
        Long requestId = 1L;
        String validToken = "validToken";
        String feedbackMessage = "This is feedback message";
        int rating = 5;

        Request req = new Request();
        req.setId(requestId);
        req.setStatus(RequestStatus.WAITING);
        req.setPoster(new User());
        req.getPoster().setToken(validToken);

        when(requestService.getRequestById(requestId)).thenReturn(req);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only requests be marked as done can be feedback"))
                .when(requestService).feedback(eq(requestId), eq(validToken), eq(feedbackMessage), eq(rating));


        mockMvc.perform(put("/requests/{requestId}/feedback", requestId)
                        .header(AUTH_HEADER, validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"feedback\": \"%s\", \"rating\": %d}", feedbackMessage, rating)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only requests be marked as done can be feedback"));
    }

    @Test
    public void testGetFeedbacksByVolunteer_success() throws Exception {
        Long volunteerId = 1L;
        FeedbackDTO feedbackDTO1 = new FeedbackDTO("Great job!", 5);
        FeedbackDTO feedbackDTO2 = new FeedbackDTO("Needs improvement", 2);


        when(requestService.getFeedbackById(volunteerId)).thenReturn(Arrays.asList(feedbackDTO1, feedbackDTO2));

        mockMvc.perform(get("/requests/{volunteerId}/feedbacks", volunteerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].feedback").value(feedbackDTO1.getFeedback()))
                .andExpect(jsonPath("$[0].rating").value(feedbackDTO1.getRating()))
                .andExpect(jsonPath("$[1].feedback").value(feedbackDTO2.getFeedback()))
                .andExpect(jsonPath("$[1].rating").value(feedbackDTO2.getRating()));
    }

    @Test
    public void testGetFeedbacksByVolunteer_notFound() throws Exception {
        Long volunteerId = 1L;

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "The user with id: " + volunteerId + " was not found"))
                .when(requestService).getFeedbackById(volunteerId);

        mockMvc.perform(get("/requests/{volunteerId}/feedbacks", volunteerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The user with id: " + volunteerId + " was not found"));
    }


}