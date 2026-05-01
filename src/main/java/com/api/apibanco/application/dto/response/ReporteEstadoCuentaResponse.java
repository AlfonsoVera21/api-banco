package com.api.apibanco.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteEstadoCuentaResponse(
        @JsonProperty("Cliente Id")
        Long clienteId,
        @JsonProperty("Cliente")
        String cliente,
        @JsonProperty("Fecha Inicio")
        LocalDate fechaInicio,
        @JsonProperty("Fecha Fin")
        LocalDate fechaFin,
        @JsonProperty("Total Creditos")
        BigDecimal totalCreditos,
        @JsonProperty("Total Debitos")
        BigDecimal totalDebitos,
        @JsonProperty("Movimientos")
        List<ReporteMovimientoResponse> movimientos,
        @JsonProperty("Reporte PDF Base64")
        String reportePdfBase64
) {
}
