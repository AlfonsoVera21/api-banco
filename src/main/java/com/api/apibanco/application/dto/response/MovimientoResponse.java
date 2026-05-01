package com.api.apibanco.application.dto.response;

import com.api.apibanco.domain.model.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        LocalDateTime fecha,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo,
        Boolean estado,
        Long cuentaId
) {
}
