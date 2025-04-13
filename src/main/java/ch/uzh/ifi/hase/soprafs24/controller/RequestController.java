package ch.uzh.ifi.hase.soprafs24.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.RequestService;

@RestController
@RequestMapping("/requests")
public class RequestController {
    
    private final RequestService requestService;
    private final DTOMapper dtoMapper = DTOMapper.INSTANCE;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<RequestGetDTO> getAllRequests() {
        List<Request> requests = requestService.getRequests();
        return requests.stream()
                .map(dtoMapper::convertEntityToRequestGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/waiting")
    public List<RequestGetDTO> getWaitingRequests() {
        List<Request> requests = requestService.getWaitingRequests();
        return requests.stream()
                .map(dtoMapper::convertEntityToRequestGetDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RequestGetDTO getRequestById(@PathVariable Long id) {
        Request request = requestService.getRequestById(id);
        return dtoMapper.convertEntityToRequestGetDTO(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestGetDTO createRequest(@RequestBody RequestPostDTO requestPostDTO) {
        Request requestEntity = dtoMapper.convertRequestPostDTOtoEntity(requestPostDTO);
        Request createdRequest = requestService.createRequest(requestEntity);
        return dtoMapper.convertEntityToRequestGetDTO(createdRequest);
    }

    @PutMapping("/{id}")
    public RequestGetDTO updateRequest(@PathVariable Long id, @RequestBody RequestPostDTO requestPostDTO) {
        Request requestToUpdate = dtoMapper.convertRequestPostDTOtoEntity(requestPostDTO);
        Request updatedRequest = requestService.updateRequest(id, requestToUpdate);
        return dtoMapper.convertEntityToRequestGetDTO(updatedRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
    }

    @PutMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptRequest(@PathVariable Long id, @RequestParam Long volunteerId) {
        User volunteer = new User();
        volunteer.setId(volunteerId);
        requestService.acceptRequest(id, volunteer);
    }

    @PutMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    public void completeRequest(@PathVariable Long id) {
        requestService.completeRequest(id);
    }

    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancelRequest(@PathVariable Long id) {
        requestService.cancelRequest(id);
    }
}
