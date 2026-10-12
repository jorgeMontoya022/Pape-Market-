package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.request.CategoryRequest;
import com.papeleriafundadores.papeleria.dto.response.CategoryResponse;
import com.papeleriafundadores.papeleria.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryRequest request);
}
