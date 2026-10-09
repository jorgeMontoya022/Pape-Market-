package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("SELECT p FROM Purchase p WHERE p.supplier.id = :supplierId ORDER BY p.purchaseDate DESC")
    List<Purchase> findBySupplier(@Param("supplierId") Long supplierId);
}
