package com.api.apibanco.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombre,
        @NotBlank(message = "El genero es obligatorio")
        @Size(max = 30, message = "El genero no puede superar 30 caracteres")
        String genero,
        @NotNull(message = "La edad es obligatoria")
        @Min(value = 0, message = "La edad no puede ser negativa")
        Integer edad,
        @NotBlank(message = "La identificacion es obligatoria")
        @Size(max = 30, message = "La identificacion no puede superar 30 caracteres")
        String identificacion,
        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 180, message = "La direccion no puede superar 180 caracteres")
        String direccion,
        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "El telefono no tiene un formato valido")
        String telefono,
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 4, max = 100, message = "La contrasena debe tener entre 4 y 100 caracteres")
        @JsonAlias("contraseña")
        String contrasena,
        Boolean estado
) {
}
