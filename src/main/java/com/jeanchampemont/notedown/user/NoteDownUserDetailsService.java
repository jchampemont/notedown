package com.jeanchampemont.notedown.user;

import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service("userDetailsService")
public class NoteDownUserDetailsService implements UserDetailsService {

    private UserRepository repo;

    @Autowired
    public NoteDownUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = repo.findByEmail(s);
        if (user == null) {
            throw new UsernameNotFoundException("not found");
        }
        return new org.springframework.security.core.userdetails.User
                (user.getEmail(), user.getPassword(), true, true, true, true, Collections.emptyList());
    }
}
