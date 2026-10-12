package com.papeleriafundadores.papeleria.mapper;

import com.papeleriafundadores.papeleria.dto.request.CustomerRequest;
import com.papeleriafundadores.papeleria.dto.response.CustomerResponse;
import com.papeleriafundadores.papeleria.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Customer toEntity(CustomerRequest request);
}
