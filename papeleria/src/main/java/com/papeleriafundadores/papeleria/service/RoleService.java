package com.papeleriafundadores.papeleria.service;

import com.papeleriafundadores.papeleria.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse create(String name, String description);

    RoleResponse findById(Long id);

    RoleResponse findByName(String name);

    List<RoleResponse> findAll();
}
