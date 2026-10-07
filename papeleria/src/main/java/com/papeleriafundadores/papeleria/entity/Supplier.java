package com.papeleriafundadores.papeleria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 150)
    private String companyName;

    @Column(name = "nit", nullable = false, unique = true, length = 20)
    private String nit;

    @Column(name = "representante_legal", length = 150)
    private String legalRepresentative;

    @Column(name = "correo", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String phone;

    @Column(name = "direccion", length = 255)
    private String address;

    @Column(name = "ciudad", length = 100)
    private String city;

    @Column(name = "estado", nullable = false)
    private Boolean status;

    @OneToMany(mappedBy = "supplier")
    private List<Product> products;

    @OneToMany(mappedBy = "supplier")
    private List<Purchase> purchases;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = true;
        }
    }
}
