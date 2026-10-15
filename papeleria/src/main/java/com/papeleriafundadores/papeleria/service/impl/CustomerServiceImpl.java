package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.CustomerRequest;
import com.papeleriafundadores.papeleria.dto.response.CustomerResponse;
import com.papeleriafundadores.papeleria.entity.Customer;
import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;
import com.papeleriafundadores.papeleria.exception.DuplicateResourceException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.CustomerMapper;
import com.papeleriafundadores.papeleria.repository.CustomerRepository;
import com.papeleriafundadores.papeleria.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByDocument(request.document())) {
            throw new DuplicateResourceException("Cliente", "documento", request.document());
        }
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Cliente", "correo", request.email());
        }
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (!customer.getDocument().equals(request.document()) && customerRepository.existsByDocument(request.document())) {
            throw new DuplicateResourceException("Cliente", "documento", request.document());
        }
        if (!customer.getEmail().equals(request.email()) && customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Cliente", "correo", request.email());
        }

        customer.setNames(request.names());
        customer.setSurnames(request.surnames());
        customer.setDocument(request.document());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByDocument(String document) {
        return customerRepository.findByDocument(document)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "documento", document));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "userId", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void changeStatus(Long id, CustomerStatus newStatus) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        customer.setStatus(newStatus);
        customerRepository.save(customer);

    }
}
