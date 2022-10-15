package com.example.socialmedia.authentication.email.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepo;

    @Autowired
    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepo) {
        this.confirmationTokenRepo = confirmationTokenRepo;
    }

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepo.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepo.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        confirmationTokenRepo.updateConfirmedAt(token, LocalDateTime.now());
    }
}