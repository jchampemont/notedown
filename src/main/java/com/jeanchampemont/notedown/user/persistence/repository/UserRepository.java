package com.jeanchampemont.notedown.user.persistence.repository;

import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);
}
