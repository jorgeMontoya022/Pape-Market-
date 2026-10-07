package com.papeleriafundadores.papeleria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "descripcion", length = 1000)
    private String description;

    @Column(name = "codigo_barras", unique = true, length = 50)
    private String barcode;

    @Column(name = "marca", length = 100)
    private String brand;

    @Column(name = "precio_compra", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "stock_minimo", nullable = false)
    private Integer minimumStock;

    @Column(name = "imagen", length = 255)
    private String image;

    @Column(name = "estado", nullable = false)
    private Boolean status;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fecha_modificacion")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Supplier supplier;

    // Automata de auditoría y valores por defecto
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = true; // Activo por defecto
        }
        if (this.stock == null) {
            this.stock = 0;
        }
        if (this.minimumStock == null) {
            this.minimumStock = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
