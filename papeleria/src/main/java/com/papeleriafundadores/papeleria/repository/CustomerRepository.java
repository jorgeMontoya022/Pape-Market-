package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.Customer;
import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.document = :document")
    Optional<Customer> findByDocument(@Param("document") String document);

    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.document = :document")
    boolean existsByDocument(@Param("document") String document);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c WHERE c.email = :email")
    boolean existsByEmail(@Param("email") String email);


    @Query("SELECT c FROM Customer c WHERE c.status = :status")
    List<Customer> findByStatus(@Param("status") CustomerStatus status);

    @Query("SELECT c FROM Customer c WHERE c.userId = :userId")
    Optional<Customer> findByUserId(@Param("userId") Long userId);
}
