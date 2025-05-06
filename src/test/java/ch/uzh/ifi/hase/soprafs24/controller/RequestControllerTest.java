package ch.uzh.ifi.hase.soprafs24.controller;

import java.time.LocalDate;
import java.util.Collections;

import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.RequestService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

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
    void deleteRequest_success() throws Exception {
        doNothing().when(requestService).deleteRequest(eq(1L), eq("validToken"));

        mockMvc.perform(delete("/requests/1")
                .header(AUTH_HEADER, "validToken"))
            .andExpect(status().isOk());
    }

    @Test
    void acceptRequest_success() throws Exception {
        doNothing().when(requestService).acceptRequest(eq(1L), eq(2L));

        mockMvc.perform(put("/requests/1/accept?volunteerId=2"))
            .andExpect(status().isOk());
    }

    @Test
    void completeRequest_success() throws Exception {
        doNothing().when(requestService).completeRequest(eq(1L), eq("validToken"));

        mockMvc.perform(put("/requests/1/complete")
                .header(AUTH_HEADER, "validToken"))
            .andExpect(status().isOk());
    }

    @Test
    void cancelRequest_success() throws Exception {
        doNothing().when(requestService).cancelRequest(eq(1L), eq("validToken"));

        mockMvc.perform(put("/requests/1/cancel")
                .header(AUTH_HEADER, "validToken"))
            .andExpect(status().isOk());
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
}