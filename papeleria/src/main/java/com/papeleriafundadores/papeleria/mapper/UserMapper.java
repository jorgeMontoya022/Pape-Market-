package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.request.UserRegisterRequest;
import com.papeleriafundadores.papeleria.dto.response.UserResponse;
import com.papeleriafundadores.papeleria.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastAccess", ignore = true)
    @Mapping(target = "resetToken", ignore = true)
    @Mapping(target = "tokenExpiration", ignore = true)
    User toEntity(UserRegisterRequest request);
}
