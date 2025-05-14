package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class FeedbackDTO {
    private String feedback;
    private int rating;

    public FeedbackDTO() {
    }

    public FeedbackDTO(String feedback, int rating) {
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
}
