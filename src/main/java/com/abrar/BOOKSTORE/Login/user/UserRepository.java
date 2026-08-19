package com.abrar.BOOKSTORE.Login.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.usernameOrEmail = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // Username collisions are shown to the user directly (see
    // AuthService.registerUser) - two people picking the same handle is
    // normal, expected UX, unlike confirming whether a given *email* is
    // registered, which is a genuine enumeration risk.
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.usernameOrEmail = :username")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.usernameOrEmail = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.usernameOrEmail = :value OR u.email = :value")
    Optional<User> findByUsernameOrEmailOrEmail(@Param("value") String value);

    long countByRole(String role);
}