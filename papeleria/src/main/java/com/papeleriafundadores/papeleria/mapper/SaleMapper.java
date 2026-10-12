package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.SaleResponse;
import com.papeleriafundadores.papeleria.entity.Sale;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, UserMapper.class, SaleDetailMapper.class})
public interface SaleMapper {

    SaleResponse toResponse(Sale sale);
}
