package ch.uzh.ifi.hase.soprafs24.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.RequestRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private RequestRepository requestRepository;


    @Autowired
    public UserService(@Qualifier("userRepository") UserRepository userRepository, RequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User newUser) {
        if (newUser.getUsername().equals("admin")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The username 'admin' is reserved and cannot be used.");
        }
        // check user
        checkIfUserExists(newUser);
        // check email
        User userByEmail = userRepository.findByEmail(newUser.getEmail());
        if (userByEmail != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The email " + newUser.getEmail() + " already exists.");
        }
        // String checkEmail = newUser.getEmail();
        // if (!(checkEmail.contains("uzh") || checkEmail.contains("ethz") || checkEmail.contains("edu") || checkEmail.contains("stu"))) {
        //     throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        //             "The email " + checkEmail + " is not valid.");
        // }
        // initialize
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreationDate(LocalDate.now());
        newUser.setIsAdmin(false);

        // saves the given entity but data is only persisted in the database once
        // flush() is called
        newUser = userRepository.save(newUser);
        userRepository.flush();

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * This is a helper method that will check the uniqueness criteria of the
     * username and the name
     * defined in the User entity. The method will do nothing if the input is unique
     * and throw an error otherwise.
     *
     * @param userToBeCreated
     * @throws org.springframework.web.server.ResponseStatusException
     * @see User
     */
    private void checkIfUserExists(User userToBeCreated) {
        User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

        if (userByUsername != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The username " + userToBeCreated.getUsername() + " already exists");
        }
    }

    // get user by id
    public User getUserById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "The user with id: " + id + " was not found"));
    }

    // update user
    public void updateUser(Long userId, UserPutDTO userPutDTO, String token) {
        User loginUser = getUserByToken(token);
        User user = getUserById(userId);
        if (loginUser == null || (!Objects.equals(loginUser.getId(), userId) && !loginUser.getUsername().equals("admin"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized user");
        }
        // update as we need
        // if the username is the same, do not change the username
        if (userPutDTO.getUsername() != null && !user.getUsername().equals(userPutDTO.getUsername())) {
            User existingUser = userRepository.findByUsername(userPutDTO.getUsername());
            if (existingUser != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, userPutDTO.getUsername() + " already exists");
            }
            user.setUsername(userPutDTO.getUsername());
        }

        if (userPutDTO.getEmail() != null && !user.getEmail().equals(userPutDTO.getEmail())) {
            User existingUser = userRepository.findByEmail(userPutDTO.getEmail());
            if (existingUser != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, userPutDTO.getEmail() + " already exists");
            }
            user.setEmail(userPutDTO.getEmail());
        }

        // if birthday is changed
        if (userPutDTO.getBirthday() != null) {
            user.setBirthday(userPutDTO.getBirthday());
        }

        if (userPutDTO.getAge() != null) {
            user.setAge(userPutDTO.getAge());
        }

        if (userPutDTO.getGender() != null) {
            user.setGender(userPutDTO.getGender());
        }

        if (userPutDTO.getLanguage() != null) {
            user.setLanguage(userPutDTO.getLanguage());
        }

        if (userPutDTO.getSchool() != null) {
            user.setSchool(userPutDTO.getSchool());
        }

        userRepository.save(user);
        userRepository.flush();
    }

    // login check
    public User login(User userInput) {
        User user = userRepository.findByUsername(userInput.getUsername());
        // if user does not in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username " + userInput.getUsername() + " does not exist");
        }
        // if password is not the same
        if (!user.getPassword().equals(userInput.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        // set status to online
        user.setStatus(UserStatus.ONLINE);
        // set a new token to identify the session
        user.setToken(UUID.randomUUID().toString());

        userRepository.save(user);
        userRepository.flush();

        return user;
    }

    public void logout(String token) {
        // find the logout user by token
        User user = getUserByToken(token);
        // set status to offline
        user.setStatus(UserStatus.OFFLINE);

        userRepository.save(user);
        userRepository.flush();
    }

    public User getUserByToken(String token) {
        // return user info by the token
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token");
        }
        return user;
    }

    public void deleteUser(Long userId, String token) {
        User deleteUser = getUserById(userId);
        User currentUser = getUserByToken(token);
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token");
        }
        if (!Objects.equals(userId, currentUser.getId()) && !currentUser.getUsername().equals("admin")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized user");
        }

        List<Request> requests = requestRepository.findByVolunteer(deleteUser);
        for (Request request : requests) {
            request.setVolunteer(null);
            request.setStatus(RequestStatus.WAITING);
            requestRepository.save(request);
        }

        userRepository.delete(deleteUser);
    }
}
