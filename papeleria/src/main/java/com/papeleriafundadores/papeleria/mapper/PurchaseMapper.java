package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.PurchaseResponse;
import com.papeleriafundadores.papeleria.entity.Purchase;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SupplierMapper.class, UserMapper.class, PurchaseDetailMapper.class})
public interface PurchaseMapper {

    PurchaseResponse toResponse(Purchase purchase);
}
