package com.api.apibanco.application.dto.response;

public record TransferenciaResponse(
        MovimientoResponse movimientoOrigen,
        MovimientoResponse movimientoDestino
) {
}
