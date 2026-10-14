package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.response.RoleResponse;
import com.papeleriafundadores.papeleria.entity.Role;
import com.papeleriafundadores.papeleria.exception.BadRequestException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.RoleMapper;
import com.papeleriafundadores.papeleria.repository.RoleRepository;
import com.papeleriafundadores.papeleria.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse create(String name, String description) {
        // IMPORTANTE: conservamos el formato original del nombre sin forzar mayúsculas.
        // Los roles deben coincidir exactamente con las expresiones @PreAuthorize:
        //   hasRole('Administrador'), hasRole('Empleado'), hasRole('Cliente')
        // Si se guarda como "ADMINISTRADOR", el @PreAuthorize("hasRole('Administrador')") FALLA.
        String formattedName = name.trim();
        if (roleRepository.findByName(formattedName).isPresent()) {
            throw new BadRequestException("El rol '" + formattedName + "' ya existe");
        }
        Role role = Role.builder()
                .name(formattedName)
                .description(description)
                .build();
        return roleMapper.toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return roleRepository.findById(id)
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findByName(String name) {
        return roleRepository.findByName(name.trim())
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "nombre", name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .toList();
    }
}
