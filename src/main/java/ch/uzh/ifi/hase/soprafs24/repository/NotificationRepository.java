package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Notification;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByTimestampDesc(User recipient);

    List<Notification> findByRecipientAndIsReadFalse(User user);

    boolean existsByRecipientAndIsReadFalse(User recipient);
}
