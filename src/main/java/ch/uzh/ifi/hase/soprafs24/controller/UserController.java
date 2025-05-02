package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ErrorResponse;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final String AUTH_HEADER = "token";
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserPostDTO userPostDTO) {
        try {
            // convert API user to internal representation
            User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

            // create user
            User createdUser = userService.createUser(userInput);
//          // get token
            String token = createdUser.getToken();
            // set header
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(AUTH_HEADER, token);
            // convert internal representation of user back to API
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).headers(responseHeaders).body(userGetDTO);
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserPostDTO userPostDTO) {
        try {
            // convert API user to internal representation
            User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
            // authenticate user to login
            User user = userService.login(userInput);
            String token = user.getToken();
            // set header
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(AUTH_HEADER, token);
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
            return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(userGetDTO);
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            //get user
            User user = userService.getUserById(userId);
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
            return ResponseEntity.status(HttpStatus.OK).body(userGetDTO);
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserPutDTO userPutDTO, @RequestHeader(AUTH_HEADER) String token) {
        try {
            //update user
            userService.updateUser(userId, userPutDTO, token);
            return ResponseEntity.noContent().build();
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @PutMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(AUTH_HEADER) String token) {
        try {
            // logout by token
            userService.logout(token);
            return ResponseEntity.noContent().build();
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> getUserMe(@RequestHeader(AUTH_HEADER) String token) {
        try {
            // get user by token to check if the user has the privilege to edit the profile
            User user = userService.getUserByToken(token);
            return ResponseEntity.status(HttpStatus.OK).body(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @RequestHeader(AUTH_HEADER) String token) {
        try {
            userService.deleteUser(userId, token);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }

    }
}
