package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.request.CategoryRequest;
import com.papeleriafundadores.papeleria.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(Long id, CategoryRequest request);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findActive();

    List<CategoryResponse> findAll();

    void toggleStatus(Long id);
}
