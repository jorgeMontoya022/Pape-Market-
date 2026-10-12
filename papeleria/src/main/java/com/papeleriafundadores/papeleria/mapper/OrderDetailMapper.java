package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.response.OrderDetailResponse;
import com.papeleriafundadores.papeleria.entity.OrderDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface OrderDetailMapper {

    OrderDetailResponse toResponse(OrderDetail orderDetail);
}
