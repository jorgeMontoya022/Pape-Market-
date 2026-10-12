package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.CartResponse;
import com.papeleriafundadores.papeleria.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "customerId", source = "customer.id")
    CartResponse toResponse(Cart cart);
}
