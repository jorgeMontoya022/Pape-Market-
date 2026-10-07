package com.papeleriafundadores.papeleria.entity;

import com.papeleriafundadores.papeleria.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 100)
    private String names;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String surnames;

    @Column(name = "documento", nullable = false, unique = true, length = 10)
    private String document;

    @Column(name = "correo", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "contrasena", nullable = false)
    private String password;

    @Column(name = "telefono", length = 20)
    private String phone;

    @Column(name = "direccion", length = 255)
    private String address;

    @Column(name = "foto_perfil")
    private String profilePhoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private UserStatus state;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "ultimo_acceso")
    private LocalDateTime lastAccess;

    @PrePersist
    public void onCreate() {
        this.registrationDate = LocalDateTime.now();
        this.state = UserStatus.ACTIVE;
    }

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Role role;

    @Column(name = "token_recuperacion", length = 100)
    private String resetToken;

    @Column(name = "token_expiracion")
    private LocalDateTime tokenExpiration;






}
