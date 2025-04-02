package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.RequestPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "email", target = "email")
  @Mapping(source = "password", target = "password")
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "email", target = "email")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "gender", target = "gender")
  @Mapping(source = "school", target = "school")
  @Mapping(source = "age", target = "age")
  @Mapping(source = "language", target = "language")
  UserGetDTO convertEntityToUserGetDTO(User user); // Birthday?

  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "contactInfo", target = "contactInfo")
  @Mapping(source = "location", target = "location")
  @Mapping(source = "emergencyLevel", target = "emergencyLevel")
  Request convertRequestPostDTOtoEntity(RequestPostDTO requestPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "title", target = "title")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "contactInfo", target = "contactInfo")
  @Mapping(source = "feedback", target = "feedback")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "emergencyLevel", target = "emergencyLevel")
  @Mapping(source = "location", target = "location")
  @Mapping(source = "creationDate", target = "creationDate")
  @Mapping(source = "poster.id", target = "posterId")
  @Mapping(source = "volunteer.id", target = "volunteerId")
  RequestGetDTO convertEntityToRequestGetDTO(Request request);
}
