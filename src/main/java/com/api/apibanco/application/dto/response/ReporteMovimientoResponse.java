package com.api.apibanco.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.api.apibanco.domain.model.enums.TipoCuenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteMovimientoResponse(
        @JsonProperty("Fecha")
        LocalDateTime fecha,
        @JsonProperty("Cliente")
        String cliente,
        @JsonProperty("Numero Cuenta")
        String numeroCuenta,
        @JsonProperty("Tipo")
        TipoCuenta tipo,
        @JsonProperty("Saldo Inicial")
        BigDecimal saldoInicial,
        @JsonProperty("Estado")
        Boolean estado,
        @JsonProperty("Movimiento")
        BigDecimal movimiento,
        @JsonProperty("Saldo Disponible")
        BigDecimal saldoDisponible
) {
}
