package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.SupplierRequest;
import com.papeleriafundadores.papeleria.dto.response.SupplierResponse;

import java.util.List;

public interface SupplierService {

    SupplierResponse create(SupplierRequest request);

    SupplierResponse update(Long id, SupplierRequest request);

    SupplierResponse findById(Long id);

    SupplierResponse findByNit(String nit);

    List<SupplierResponse> findAll();

    void toggleStatus(Long id); //Para activar o inactivar
}
