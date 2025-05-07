package ch.uzh.ifi.hase.soprafs24.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;

@DataJpaTest
public class MessageRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User sender;
    private User recipient;
    private Message message;

    @BeforeEach
    public void setup() {
        // 创建发送者
        sender = new User();
        sender.setUsername("sender");
        sender.setPassword("password");
        sender.setEmail("sender@example.com");
        sender.setStatus(UserStatus.ONLINE);
        entityManager.persist(sender);
        
        // 创建接收者
        recipient = new User();
        recipient.setUsername("recipient");
        recipient.setPassword("password");
        recipient.setEmail("recipient@example.com");
        recipient.setStatus(UserStatus.ONLINE);
        entityManager.persist(recipient);
        
        // 创建消息
        message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("Hello, World!");
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        entityManager.persist(message);
        
        entityManager.flush();
    }

    @Test
    public void findBySenderIdAndRecipientIdAndIsReadFalse_success() {
        List<Message> messages = messageRepository.findBySenderIdAndRecipientIdAndIsReadFalse(
                sender.getId(), recipient.getId());
        
        assertEquals(1, messages.size());
        assertEquals("Hello, World!", messages.get(0).getContent());
        assertFalse(messages.get(0).isRead());
    }
    
    @Test
    public void findConversation_success() {
        // 创建回复消息
        Message replyMessage = new Message();
        replyMessage.setSender(recipient);
        replyMessage.setRecipient(sender);
        replyMessage.setContent("Hi there!");
        replyMessage.setTimestamp(LocalDateTime.now().plusMinutes(1));
        replyMessage.setRead(false);
        entityManager.persist(replyMessage);
        entityManager.flush();
        
        List<Message> conversation = messageRepository.findConversation(
                sender.getId(), recipient.getId());
        
        assertEquals(2, conversation.size());
        assertEquals("Hello, World!", conversation.get(0).getContent());
        assertEquals("Hi there!", conversation.get(1).getContent());
    }
    
    @Test
    public void findDistinctChatPartnerIds_success() {
        // 创建第三个用户并发送消息
        User thirdUser = new User();
        thirdUser.setUsername("thirduser");
        thirdUser.setPassword("password");
        thirdUser.setEmail("third@example.com");
        thirdUser.setStatus(UserStatus.ONLINE);
        entityManager.persist(thirdUser);
        
        Message anotherMessage = new Message();
        anotherMessage.setSender(sender);
        anotherMessage.setRecipient(thirdUser);
        anotherMessage.setContent("Hey third user!");
        anotherMessage.setTimestamp(LocalDateTime.now());
        anotherMessage.setRead(false);
        entityManager.persist(anotherMessage);
        entityManager.flush();
        
        List<Long> chatPartnerIds = messageRepository.findDistinctChatPartnerIds(sender.getId());
        
        assertEquals(2, chatPartnerIds.size());
        assertTrue(chatPartnerIds.contains(recipient.getId()));
        assertTrue(chatPartnerIds.contains(thirdUser.getId()));
    }
    
    @Test
    public void findTopByUserPairOrderByTimestampDesc_success() {
        // 创建另一条更新的消息
        Message newerMessage = new Message();
        newerMessage.setSender(recipient);
        newerMessage.setRecipient(sender);
        newerMessage.setContent("Newer message");
        newerMessage.setTimestamp(LocalDateTime.now().plusHours(1));
        newerMessage.setRead(false);
        entityManager.persist(newerMessage);
        entityManager.flush();
        
        List<Message> messages = messageRepository.findTopByUserPairOrderByTimestampDesc(
                sender.getId(), recipient.getId());
        
        assertEquals(2, messages.size());
        assertEquals("Newer message", messages.get(0).getContent()); // 应该是时间较新的消息排在前面
    }
    
    @Test
    public void existsUnreadByRecipientId_success() {
        boolean hasUnread = messageRepository.existsUnreadByRecipientId(recipient.getId());
        assertTrue(hasUnread);
        
        // 将所有消息标记为已读
        message.setRead(true);
        entityManager.persist(message);
        entityManager.flush();
        
        hasUnread = messageRepository.existsUnreadByRecipientId(recipient.getId());
        assertFalse(hasUnread);
    }
}
