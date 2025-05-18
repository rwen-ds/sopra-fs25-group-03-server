package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class FeedbackDTO {
    private Long requestId;
    private String feedback;
    private int rating;

    public FeedbackDTO() {
    }

    public FeedbackDTO(Long requestId, String feedback, int rating) {
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
}
