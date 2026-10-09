package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s WHERE s.nit = :nit")
    Optional<Supplier> findByNit(@Param("nit") String nit);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s WHERE s.nit = :nit")
    boolean existsByNit(@Param("nit") String nit);
}
