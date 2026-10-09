package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.PurchaseDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseDetailRepository  extends JpaRepository<PurchaseDetails, Long> {

    @Query("SELECT pd FROM PurchaseDetails pd WHERE pd.purchase.id = :purchaseId")
    List<PurchaseDetails> findByPurchase(@Param("purchaseId") Long purchaseId);
}
