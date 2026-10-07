package com.papeleriafundadores.papeleria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_venta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice; // Guardamos el precio del día aquí para congelarlo

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Sale sale;
}
