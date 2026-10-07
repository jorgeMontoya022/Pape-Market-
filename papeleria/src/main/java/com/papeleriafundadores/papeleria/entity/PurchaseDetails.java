package com.papeleriafundadores.papeleria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Purchase purchase;
}
