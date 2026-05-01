package com.api.apibanco.application.dto.response;

public record ClienteResponse(
        Long clienteId,
        String nombre,
        String genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado
) {
}
