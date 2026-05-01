package com.api.apibanco.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.api.apibanco.domain.model.enums.TipoMovimiento;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovimientoRequest(
        LocalDateTime fecha,
        @NotNull(message = "El tipo de movimiento es obligatorio")
        @JsonAlias("tipo")
        TipoMovimiento tipoMovimiento,
        @NotNull(message = "El valor es obligatorio")
        @JsonAlias("monto")
        BigDecimal valor,
        @NotNull(message = "La cuentaId es obligatoria")
        Long cuentaId
) {
}
