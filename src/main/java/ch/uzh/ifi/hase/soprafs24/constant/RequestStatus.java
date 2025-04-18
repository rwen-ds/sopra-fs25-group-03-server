package ch.uzh.ifi.hase.soprafs24.constant;

public enum RequestStatus {
    WAITING, // Poster just posted a request
    ACCEPTING, // Poster clicked accept
    CANCELLED, // Poster cancelled
    COMPLETED, // Volunteer clicked completed
    VOLUNTEERED, // Volunteer clicked volunteer
    DONE //Poster clicked done
}
