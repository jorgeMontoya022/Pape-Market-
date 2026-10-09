package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.User;
import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.document = :document")
    boolean existsByDocument(@Param("document") String document);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.state = :state")
    List<User> findByState(@Param("state") UserStatus state);

    @Query("SELECT u FROM User u WHERE u.resetToken = :token")
    Optional<User> findByResetToken(@Param("token") String token);

}
