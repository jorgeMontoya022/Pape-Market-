package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.request.SupplierRequest;
import com.papeleriafundadores.papeleria.dto.response.SupplierResponse;
import com.papeleriafundadores.papeleria.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "purchases", ignore = true)
    Supplier toEntity(SupplierRequest request);
}
