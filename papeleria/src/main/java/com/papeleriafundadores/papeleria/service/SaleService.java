package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.SaleRequest;
import com.papeleriafundadores.papeleria.dto.response.SaleResponse;

import java.util.List;

public interface SaleService {
    SaleResponse registerSale(SaleRequest request); // Restará stock automáticamente y validará inventario

    SaleResponse findById(Long id);

    List<SaleResponse> findByUserId(Long userId); // Ventas realizadas por un cajero específico

    List<SaleResponse> findAll();
}
