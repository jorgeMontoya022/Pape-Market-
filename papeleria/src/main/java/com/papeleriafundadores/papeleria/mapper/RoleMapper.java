package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.RoleResponse;
import com.papeleriafundadores.papeleria.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toResponse(Role role);
}
