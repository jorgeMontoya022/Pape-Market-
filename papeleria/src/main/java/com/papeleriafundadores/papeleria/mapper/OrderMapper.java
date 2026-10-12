package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.OrderResponse;
import com.papeleriafundadores.papeleria.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class, OrderDetailMapper.class})
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}
