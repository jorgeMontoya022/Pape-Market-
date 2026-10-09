package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository  extends JpaRepository<Sale, Long> {

    @Query("SELECT s FROM Sale s WHERE s.user.id = :userId ORDER BY s.saleDate DESC")
    List<Sale> findByUser(@Param("userId") Long userId);
}
