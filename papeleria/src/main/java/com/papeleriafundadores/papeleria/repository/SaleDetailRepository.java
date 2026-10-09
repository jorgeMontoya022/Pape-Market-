package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    @Query("SELECT sd FROM SaleDetail sd WHERE sd.sale.id = :saleId")
    List<SaleDetail> findBySale(@Param("saleId") Long saleId);
}
