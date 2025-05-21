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
            "WHERE m.senderId = :senderId AND m.recipientId = :recipientId AND m.isRead = false")
    List<Message> findBySenderIdAndRecipientIdAndIsReadFalse(@Param("senderId") Long senderId,
                                                             @Param("recipientId") Long recipientId);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :senderId AND m.recipientId = :recipientId) " +
            "   OR (m.senderId = :recipientId AND m.recipientId = :senderId) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("senderId") Long senderId,
                                   @Param("recipientId") Long recipientId);

    @Query("SELECT DISTINCT " +
            "CASE WHEN m.senderId = :userId THEN m.recipientId ELSE m.senderId END " +
            "FROM Message m " +
            "WHERE m.senderId = :userId OR m.recipientId = :userId")
    List<Long> findDistinctChatPartnerIds(@Param("userId") Long userId);


    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :userId1 AND m.recipientId = :userId2) " +
            "   OR (m.senderId = :userId2 AND m.recipientId = :userId1) " +
            "ORDER BY m.timestamp DESC")
    List<Message> findTopByUserPairOrderByTimestampDesc(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.recipientId = :recipientId AND m.isRead = false")
    boolean existsUnreadByRecipientId(@Param("recipientId") Long recipientId);

    List<Message> findByRecipientIdAndIsReadFalse(Long userId);
}
