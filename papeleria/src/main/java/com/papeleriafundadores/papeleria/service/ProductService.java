package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.ProductRequest;
import com.papeleriafundadores.papeleria.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse findById(Long id);

    ProductResponse findByBarcode(String barcode);

    List<ProductResponse> searchByName(String name);

    List<ProductResponse> findByCategory(Long categoryId);

    List<ProductResponse> findLowStock();

    List<ProductResponse> findAll();

    void toggleStatus(Long id);
}
