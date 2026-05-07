package com.api.apibanco.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferenciaRequest(
        LocalDateTime fecha,
        @NotNull(message = "La cuentaOrigenId es obligatoria")
        Long cuentaOrigenId,
        @NotNull(message = "La cuentaDestinoId es obligatoria")
        Long cuentaDestinoId,
        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "0.01", message = "El valor de la transferencia debe ser mayor a cero")
        BigDecimal valor
) {
}
