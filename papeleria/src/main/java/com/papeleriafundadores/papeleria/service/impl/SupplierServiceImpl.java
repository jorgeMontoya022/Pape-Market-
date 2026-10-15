package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.SupplierRequest;
import com.papeleriafundadores.papeleria.dto.response.SupplierResponse;
import com.papeleriafundadores.papeleria.entity.Supplier;
import com.papeleriafundadores.papeleria.exception.DuplicateResourceException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.SupplierMapper;
import com.papeleriafundadores.papeleria.repository.SupplierRepository;
import com.papeleriafundadores.papeleria.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByNit(request.nit())) {
            throw new DuplicateResourceException("Proveedor", "NIT", request.nit());
        }
        Supplier supplier = supplierMapper.toEntity(request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        if (!supplier.getNit().equals(request.nit()) && supplierRepository.existsByNit(request.nit())) {
            throw new DuplicateResourceException("Proveedor", "NIT", request.nit());
        }

        supplier.setCompanyName(request.companyName());
        supplier.setNit(request.nit());
        supplier.setLegalRepresentative(request.legalRepresentative());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setCity(request.city());

        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse findByNit(String nit) {
        return supplierRepository.findByNit(nit)
                .map(supplierMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "NIT", nit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        supplier.setStatus(!supplier.getStatus());
        supplierRepository.save(supplier);
    }
}
