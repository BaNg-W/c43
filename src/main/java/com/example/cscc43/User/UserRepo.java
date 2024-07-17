package com.example.cscc43.User;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)

public interface UserRepo {
    
    Optional<User> findByUsername(String username);
}
