package com.trace.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trace.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            select distinct u
            from User u
            left join fetch u.roles
            where lower(u.email) = lower(:email)
            """)
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmailIgnoreCase(String email);
}