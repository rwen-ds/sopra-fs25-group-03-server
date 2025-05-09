package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MessageRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;

    private Message message;

    @BeforeEach
    public void setup() {
        message = new Message();
        message.setSenderId(1L);
        message.setRecipientId(2L);
        message.setContent("Hello, World!");
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        entityManager.persist(message);

        entityManager.flush();
    }

    @Test
    public void findBySenderIdAndRecipientIdAndIsReadFalse_success() {
        List<Message> messages = messageRepository.findBySenderIdAndRecipientIdAndIsReadFalse(
                1L, 2L);

        assertEquals(1, messages.size());
        assertEquals("Hello, World!", messages.get(0).getContent());
        assertFalse(messages.get(0).isRead());
    }

    @Test
    public void findConversation_success() {
        Message replyMessage = new Message();
        replyMessage.setSenderId(2L);
        replyMessage.setRecipientId(1L);
        replyMessage.setContent("Hi there!");
        replyMessage.setTimestamp(LocalDateTime.now().plusMinutes(1));
        replyMessage.setRead(false);
        entityManager.persist(replyMessage);
        entityManager.flush();

        List<Message> conversation = messageRepository.findConversation(
                1L, 2L);

        assertEquals(2, conversation.size());
        assertEquals("Hello, World!", conversation.get(0).getContent());
        assertEquals("Hi there!", conversation.get(1).getContent());
    }

    @Test
    public void findDistinctChatPartnerIds_success() {

        Message anotherMessage = new Message();
        anotherMessage.setSenderId(1L);
        anotherMessage.setRecipientId(3L);
        anotherMessage.setContent("Hey third user!");
        anotherMessage.setTimestamp(LocalDateTime.now());
        anotherMessage.setRead(false);
        entityManager.persist(anotherMessage);
        entityManager.flush();

        List<Long> chatPartnerIds = messageRepository.findDistinctChatPartnerIds(1L);

        assertEquals(2, chatPartnerIds.size());
        assertTrue(chatPartnerIds.contains(2L));
        assertTrue(chatPartnerIds.contains(3L));
    }

    @Test
    public void findTopByUserPairOrderByTimestampDesc_success() {
        Message newerMessage = new Message();
        newerMessage.setSenderId(2L);
        newerMessage.setRecipientId(1L);
        newerMessage.setContent("Newer message");
        newerMessage.setTimestamp(LocalDateTime.now().plusHours(1));
        newerMessage.setRead(false);
        entityManager.persist(newerMessage);
        entityManager.flush();

        List<Message> messages = messageRepository.findTopByUserPairOrderByTimestampDesc(
                1L, 2L);

        assertEquals(2, messages.size());
        assertEquals("Newer message", messages.get(0).getContent()); // 应该是时间较新的消息排在前面
    }

    @Test
    public void existsUnreadByRecipientId_success() {
        boolean hasUnread = messageRepository.existsUnreadByRecipientId(2L);
        assertTrue(hasUnread);

        message.setRead(true);
        entityManager.persist(message);
        entityManager.flush();

        hasUnread = messageRepository.existsUnreadByRecipientId(2L);
        assertFalse(hasUnread);
    }
}
