package com.payflow.payflow.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindByEmail(){
        User user=new User("test@gmail.com","Mohit");
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userRepository.findByEmail("test@gmail.com");
        assertTrue(foundUser.isPresent());

        assertEquals("test@gmail.com", foundUser.get().getEmail());

    }
}
