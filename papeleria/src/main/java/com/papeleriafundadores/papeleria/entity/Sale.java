package com.papeleriafundadores.papeleria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_venta", nullable = false, updatable = false)
    private LocalDateTime saleDate;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "impuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "cliente_id") // Puede ser null si es "Cliente Mostrador"
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user; // Empleado que hizo la venta

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleDetail> details;

    @PrePersist
    protected void onCreate() {
        this.saleDate = LocalDateTime.now();
    }
}
