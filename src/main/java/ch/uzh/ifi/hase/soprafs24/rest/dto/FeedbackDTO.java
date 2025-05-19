package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class FeedbackDTO {
    private Long requestId;
    private String feedback;
    private Integer rating;

    public FeedbackDTO() {
    }

    public FeedbackDTO(Long requestId, String feedback, Integer rating) {
        this.requestId = requestId;
        this.feedback = feedback;
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
