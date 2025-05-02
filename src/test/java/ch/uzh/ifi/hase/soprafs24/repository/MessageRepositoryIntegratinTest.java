package ch.uzh.ifi.hase.soprafs24.repository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import ch.uzh.ifi.hase.soprafs24.constant.RequestEmergencyLevel;
import ch.uzh.ifi.hase.soprafs24.constant.RequestStatus;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Request;
import ch.uzh.ifi.hase.soprafs24.entity.User;

public class MessageRepositoryIntegratinTest {
        
    // @Autowired
    // private MessageRepository messageRepository;

    // @Autowired
    // private UserRepository userRepository;

    // @Test
    // public void testSaveMessage() {
    //     User sender = new User();
    //     sender.setUsername("sender");
    //     userRepository.save(sender);

    //     User recipient = new User();
    //     recipient.setUsername("recipient");
    //     userRepository.save(recipient);

    //     Message message = new Message();
    //     message.setSender(sender);
    //     message.setRecipient(recipient);
    //     message.setContent("Hello, World!");
    //     message.setTimestamp(LocalDateTime.now());
    //     message.setRead(false);

    //     Message savedMessage = messageRepository.save(message);

    //     assertNotNull(savedMessage.getId());
    //     assertEquals("Hello, World!", savedMessage.getContent());
    // }            
}
