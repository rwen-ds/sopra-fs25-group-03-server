package ch.uzh.ifi.hase.soprafs24.rest.dto;


import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

public class NotificationDTO {
    private RequestStatus type;           // "volunteer", "accepted", "completed"
    private String content;        //  "Volunteer John is applying to help your request ABC"
    private Long requestId;        // button
    private Long posterId;
    private Long volunteerId;  // volunteer name
    private String requestTitle;

    public NotificationDTO() {
    }

    public NotificationDTO(RequestStatus type, String content, Long requestId, Long posterId, Long volunteerId, String requestTitle) {
        this.type = type;
        this.content = content;
        this.requestId = requestId;
        this.posterId = posterId;
        this.volunteerId = volunteerId;
        this.requestTitle = requestTitle;
    }

    public RequestStatus getType() {
        return type;
    }

    public void setType(RequestStatus type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getPosterId() {
        return posterId;
    }

    public void setPosterId(Long posterId) {
        this.posterId = posterId;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }
}
