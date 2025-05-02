package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.UUID;

@Configuration
public class AppConfig {

    @Bean
    public CommandLineRunner run(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin") == null){
                User adminUser = new User();
                adminUser.setEmail("admin@example.com");
                adminUser.setUsername("admin");
                adminUser.setPassword("admin"); // Use plain text password
                adminUser.setCreationDate(LocalDate.now());
                adminUser.setStatus(UserStatus.OFFLINE); // Set the status
                adminUser.setToken(UUID.randomUUID().toString());
                adminUser.setIsAdmin(true); // Set this user as an admin

                // Save the user to the repository
                userRepository.save(adminUser);
            }
        };
    }
}
