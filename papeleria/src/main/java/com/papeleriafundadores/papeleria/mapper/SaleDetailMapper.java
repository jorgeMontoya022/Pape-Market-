package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.SaleDetailResponse;
import com.papeleriafundadores.papeleria.entity.SaleDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface SaleDetailMapper {

    SaleDetailResponse toResponse(SaleDetail saleDetail);
}
