package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.PurchaseRequest;
import com.papeleriafundadores.papeleria.dto.response.PurchaseResponse;

import java.util.List;

public interface PurchaseService {
    PurchaseResponse registerPurchase(PurchaseRequest request); // Sumará stock automáticamente al guardar

    PurchaseResponse findById(Long id);

    List<PurchaseResponse> findBySupplier(Long supplierId);

    List<PurchaseResponse> findAll();
}
