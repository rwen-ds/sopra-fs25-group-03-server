package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndRecipientIdAndIsReadFalse(Long senderId, Long recipientId);

    List<Message> findBySenderIdAndRecipientId(Long senderId, Long recipientId);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :senderId AND m.recipientId = :recipientId) " +
            "   OR (m.senderId = :recipientId AND m.recipientId = :senderId) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("senderId") Long senderId,
                                   @Param("recipientId") Long recipientId);
}
