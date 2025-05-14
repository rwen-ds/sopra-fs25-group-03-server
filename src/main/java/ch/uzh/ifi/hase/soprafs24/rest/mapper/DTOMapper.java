package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

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
    @Mapping(source = "isAdmin", target = "isAdmin")
    UserGetDTO convertEntityToUserGetDTO(User user); // Birthday?

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "contactInfo", target = "contactInfo")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "emergencyLevel", target = "emergencyLevel")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "countryCode", target = "countryCode")
    Request convertRequestPostDTOtoEntity(RequestPostDTO requestPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "contactInfo", target = "contactInfo")
    @Mapping(source = "feedback", target = "feedback")
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "emergencyLevel", target = "emergencyLevel")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "creationDate", target = "creationDate")
    @Mapping(source = "poster.id", target = "posterId")
    @Mapping(source = "volunteer.id", target = "volunteerId")
    @Mapping(source = "publishedAt", target = "publishedAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "poster.username", target = "posterUsername")
    @Mapping(source = "volunteer.username", target = "volunteerUsername")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "countryCode", target = "countryCode")
    RequestGetDTO convertEntityToRequestGetDTO(Request request);

    @Mapping(source = "id", target = "notificationId")
    @Mapping(source = "recipientId", target = "recipientId")
    @Mapping(source = "relatedUserId", target = "relatedUserId")
    @Mapping(source = "relatedUsername", target = "relatedUsername")
    @Mapping(source = "request.id", target = "requestId")
    @Mapping(source = "request.title", target = "requestTitle")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "isRead", target = "isRead")
    NotificationDTO convertEntityToNotificationDTO(Notification notification);
}
