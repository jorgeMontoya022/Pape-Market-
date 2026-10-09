package com.papeleriafundadores.papeleria.repository;

import com.papeleriafundadores.papeleria.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    // Buscador global corto (la lógica de ignorar mayúsculas/minúsculas se maneja en el @Query)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = true")
    List<Product> findByName(@Param("name") String name);


    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = true")
    List<Product> findByCategory(@Param("categoryId") Long categoryId);


    @Query("SELECT p FROM Product p WHERE p.stock <= p.minimumStock")
    List<Product> findLowStock();
}
