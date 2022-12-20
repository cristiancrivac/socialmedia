package com.example.socialmedia;

import com.example.socialmedia.authentication.AuthenticationService;
import com.example.socialmedia.authentication.RegistrationRequest;
import com.example.socialmedia.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopulateDatabase {

    private final AuthenticationService authenticationService;

    @Autowired
    public PopulateDatabase(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Bean
    public CommandLineRunner addUser(UserRepository repository) {
        return args -> {
            RegistrationRequest request = new RegistrationRequest("aladin", "ababa", "hey@hey.com", "ass");

            authenticationService.register(request);
        };
    }
}
