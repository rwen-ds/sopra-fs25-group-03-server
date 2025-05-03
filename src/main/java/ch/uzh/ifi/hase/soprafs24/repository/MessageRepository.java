package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
            "WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId AND m.isRead = false")
    List<Message> findBySenderIdAndRecipientIdAndIsReadFalse(@Param("senderId") Long senderId,
                                                             @Param("recipientId") Long recipientId);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.sender.id = :senderId AND m.recipient.id = :recipientId) " +
            "   OR (m.sender.id = :recipientId AND m.recipient.id = :senderId) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("senderId") Long senderId,
                                   @Param("recipientId") Long recipientId);

    @Query("SELECT DISTINCT " +
            "CASE WHEN m.sender.id = :userId THEN m.recipient.id ELSE m.sender.id END " +
            "FROM Message m " +
            "WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<Long> findDistinctChatPartnerIds(@Param("userId") Long userId);


    @Query("SELECT m FROM Message m " +
            "WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2) " +
            "   OR (m.sender.id = :userId2 AND m.recipient.id = :userId1) " +
            "ORDER BY m.timestamp DESC")
    List<Message> findTopByUserPairOrderByTimestampDesc(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.recipient.id = :recipientId AND m.isRead = false")
    boolean existsUnreadByRecipientId(@Param("recipientId") Long recipientId);

}
