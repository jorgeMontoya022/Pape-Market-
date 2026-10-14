package com.papeleriafundadores.papeleria.service.impl;

import com.papeleriafundadores.papeleria.dto.request.ProductRequest;
import com.papeleriafundadores.papeleria.dto.response.ProductResponse;
import com.papeleriafundadores.papeleria.entity.Category;
import com.papeleriafundadores.papeleria.entity.Product;
import com.papeleriafundadores.papeleria.entity.Supplier;
import com.papeleriafundadores.papeleria.exception.BadRequestException;
import com.papeleriafundadores.papeleria.exception.ResourceNotFoundException;
import com.papeleriafundadores.papeleria.mapper.ProductMapper;
import com.papeleriafundadores.papeleria.repository.CategoryRepository;
import com.papeleriafundadores.papeleria.repository.ProductRepository;
import com.papeleriafundadores.papeleria.repository.SupplierRepository;
import com.papeleriafundadores.papeleria.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.barcode() != null && !request.barcode().isBlank() && productRepository.findByBarcode(request.barcode()).isPresent()) {
            throw new BadRequestException("El código de barras '" + request.barcode() + "' ya está asignado a otro producto.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.categoryId()));

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.supplierId()));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setSupplier(supplier);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        if (request.barcode() != null && !request.barcode().isBlank() && !request.barcode().equals(product.getBarcode())) {
            if (productRepository.findByBarcode(request.barcode()).isPresent()) {
                throw new BadRequestException("El código de barras '" + request.barcode() + "' ya está asignado a otro producto.");
            }
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.categoryId()));

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.supplierId()));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setBarcode(request.barcode());
        product.setBrand(request.brand());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setStock(request.stock());
        product.setMinimumStock(request.minimumStock());
        product.setImage(request.image());
        product.setCategory(category);
        product.setSupplier(supplier);

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "código de barras", barcode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByName(name).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(Long categoryId) {
        return productRepository.findByCategory(categoryId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return productRepository.findLowStock().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        product.setStatus(!product.getStatus());
        productRepository.save(product);
    }
}
