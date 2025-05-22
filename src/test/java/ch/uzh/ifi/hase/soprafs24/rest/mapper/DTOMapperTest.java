package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representations works.
 */
public class DTOMapperTest {

  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("name");
    userPostDTO.setPassword("password");
    userPostDTO.setEmail("user@edu.example.com");

    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    assertEquals(userPostDTO.getUsername(), user.getUsername());
    assertEquals(userPostDTO.getEmail(), user.getEmail());
    assertEquals(userPostDTO.getPassword(), user.getPassword());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    user.setId(1L); 
    user.setUsername("firstname@lastname");
    user.setPassword("password");
    user.setEmail("firstname.lastname@edu.example.com");
    user.setStatus(UserStatus.OFFLINE);
    user.setToken("1");
    

    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    assertEquals(user.getId(), userGetDTO.getId());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
    assertEquals(user.getEmail(), userGetDTO.getEmail());
    assertEquals(user.getStatus(), userGetDTO.getStatus());
  }

  @Test
  public void testCreateRequest_fromRequestPostDTO_toRequest_success() {
    // create RequestPostDTO
    RequestPostDTO requestPostDTO = new RequestPostDTO();
    requestPostDTO.setTitle("Test Request Title");
    requestPostDTO.setDescription("Test Request Description");
    requestPostDTO.setContactInfo("contact@example.com");
    requestPostDTO.setLocation("Zurich");
    requestPostDTO.setEmergencyLevel(RequestEmergencyLevel.LOW);

    Request request = DTOMapper.INSTANCE.convertRequestPostDTOtoEntity(requestPostDTO);

    // check content
    assertEquals(requestPostDTO.getTitle(), request.getTitle());
    assertEquals(requestPostDTO.getDescription(), request.getDescription());
    assertEquals(requestPostDTO.getContactInfo(), request.getContactInfo());
    assertEquals(requestPostDTO.getLocation(), request.getLocation());
    assertEquals(requestPostDTO.getEmergencyLevel(), request.getEmergencyLevel());
  }

  @Test
  public void testGetRequest_fromRequest_toRequestGetDTO_success() {
    // create Request entity
    Request request = new Request();
    request.setId(1L);
    request.setTitle("Test Request Title");
    request.setDescription("Test Request Description");
    request.setContactInfo("contact@example.com");
    request.setFeedback("No feedback");
    request.setStatus(RequestStatus.ACCEPTING);
    request.setEmergencyLevel(RequestEmergencyLevel.LOW);
    request.setLocation("Zurich");
    request.setCreationDate(java.time.LocalDate.now());
    
    User poster = new User();
    poster.setId(100L);
    request.setPoster(poster);

    request.setVolunteer(null);

    RequestGetDTO requestGetDTO = DTOMapper.INSTANCE.convertEntityToRequestGetDTO(request);

    // check content
    assertEquals(request.getId(), requestGetDTO.getId());
    assertEquals(request.getTitle(), requestGetDTO.getTitle());
    assertEquals(request.getDescription(), requestGetDTO.getDescription());
    assertEquals(request.getContactInfo(), requestGetDTO.getContactInfo());
    assertEquals(request.getFeedback(), requestGetDTO.getFeedback());
    assertEquals(request.getStatus(), requestGetDTO.getStatus());
    assertEquals(request.getEmergencyLevel(), requestGetDTO.getEmergencyLevel());
    assertEquals(request.getLocation(), requestGetDTO.getLocation());
    assertEquals(request.getCreationDate(), requestGetDTO.getCreationDate());
    assertEquals(poster.getId(), requestGetDTO.getPosterId());

    assertEquals(null, requestGetDTO.getVolunteerId());
  }
}