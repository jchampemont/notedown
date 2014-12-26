package com.jeanchampemont.notedown.user;

import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class UserService {

    private UserRepository repo;

    private PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public User create(String email, String password) {
        User result = new User();
        result.setEmail(email);
        result.setPassword(encoder.encode(password));
        result.setNotes(new HashSet<>());
        result = repo.save(result);
        return result;
    }
}
