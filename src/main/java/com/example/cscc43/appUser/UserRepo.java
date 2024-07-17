package com.example.cscc43.appUser;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)

public interface UserRepo extends JpaRepository<AppUser, Long>{

    Optional<AppUser> findByUsername(String username);
}
