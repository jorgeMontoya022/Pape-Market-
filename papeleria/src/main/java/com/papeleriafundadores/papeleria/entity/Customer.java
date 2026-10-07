package com.papeleriafundadores.papeleria.entity;

import com.papeleriafundadores.papeleria.entity.enums.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Customer {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String names;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String surnames;

    @Column(name = "documento", nullable = false, unique = true, length = 15)
    private String document;

    @Column(name = "correo", unique = true, length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String phone;

    @Column(name = "direccion", length = 255)
    private String address;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private CustomerStatus status;

    /**
     * Vincula este Customer con el User que se registró en el sistema.
     * Solo se popula cuando el Customer fue creado automáticamente durante
     * el registro de un User con rol CLIENTE.
     * Es nullable para los Customers creados manualmente por el admin/empleado.
     */
    @Column(name = "user_id", unique = true)
    private Long userId;

    // Relación 1:1 con Carrito
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    // Relación 1:N con Pedidos
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = CustomerStatus.ACTIVE;
        }
    }
}
