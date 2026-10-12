package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.PurchaseDetailResponse;
import com.papeleriafundadores.papeleria.entity.PurchaseDetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface PurchaseDetailMapper {

    PurchaseDetailResponse toResponse(PurchaseDetails purchaseDetail);
}
