package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.CustomerRequest;
import com.papeleriafundadores.papeleria.dto.response.CustomerResponse;
import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse findById(Long id);

    CustomerResponse findByDocument(String document);

    /**
     * Busca el Customer vinculado a un User por su userId.
     * Usado por el frontend después del login de un CLIENTE
     * para obtener el customerId necesario para carrito y pedidos.
     */
    CustomerResponse findByUserId(Long userId);

    List<CustomerResponse> findAll();

    void changeStatus(Long id, CustomerStatus newStatus);
}
