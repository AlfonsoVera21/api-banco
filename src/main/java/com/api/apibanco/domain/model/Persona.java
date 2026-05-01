package com.api.apibanco.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Persona extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank(message = "El genero es obligatorio")
    @Size(max = 30, message = "El genero no puede superar 30 caracteres")
    @Column(nullable = false, length = 30)
    private String genero;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    @Column(nullable = false)
    private Integer edad;

    @NotBlank(message = "La identificacion es obligatoria")
    @Size(max = 30, message = "La identificacion no puede superar 30 caracteres")
    @Column(nullable = false, unique = true, length = 30)
    private String identificacion;

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 180, message = "La direccion no puede superar 180 caracteres")
    @Column(nullable = false, length = 180)
    private String direccion;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "El telefono no tiene un formato valido")
    @Column(nullable = false, length = 20)
    private String telefono;

}
