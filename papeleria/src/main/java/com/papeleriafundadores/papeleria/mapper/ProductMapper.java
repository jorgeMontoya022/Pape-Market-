package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.request.ProductRequest;
import com.papeleriafundadores.papeleria.dto.response.ProductResponse;
import com.papeleriafundadores.papeleria.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    Product toEntity(ProductRequest request);
}
