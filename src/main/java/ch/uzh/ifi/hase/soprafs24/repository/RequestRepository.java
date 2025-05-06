package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs24.entity.Request;

import java.util.List;

@Repository("requestRepository")
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByPosterId(Long posterId);
    List<Request> findByPoster(User poster);
    List<Request> findByVolunteer(User volunteer);
    List<Request> findByStatus(RequestStatus status);
    List<Request> findByVolunteerId(Long volunteerId);

}