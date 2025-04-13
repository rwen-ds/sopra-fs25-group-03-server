package ch.uzh.ifi.hase.soprafs24.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;

@Entity
@Table(name = "REQUEST")
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column
    private String contactInfo;

    @ManyToOne
    @JoinColumn(name = "volunteerId")
    private User volunteer;

    @Column
    private String location;

    @Column
    private String feedback;

    @Column(nullable = false)
    private RequestStatus status;

    @Column(nullable = false)
    private RequestEmergencyLevel emergencyLevel;

    @Column(nullable = false, name = "creation_date")
    private LocalDate creationDate;
    
    @OneToOne
    @JoinColumn(name = "posterId", nullable = false)
    private User poster;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public User getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(User volunteer) {
        this.volunteer = volunteer;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public RequestEmergencyLevel getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(RequestEmergencyLevel emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public User getPoster() {
        return poster;
    }

    public void setPoster(User poster) {
        this.poster = poster;
    }
}
