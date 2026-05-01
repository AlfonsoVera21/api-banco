package com.api.apibanco.application.dto.response;

import com.api.apibanco.domain.model.enums.TipoCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado,
        Long clienteId
) {
}
