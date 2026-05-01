package com.api.apibanco.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.api.apibanco.domain.model.enums.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CuentaRequest(
        @NotBlank(message = "El numero de cuenta es obligatorio")
        @Size(max = 30, message = "El numero de cuenta no puede superar 30 caracteres")
        @JsonAlias("numero")
        String numeroCuenta,
        @NotNull(message = "El tipo de cuenta es obligatorio")
        @JsonAlias("tipo")
        TipoCuenta tipoCuenta,
        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial no puede ser negativo")
        BigDecimal saldoInicial,
        Boolean estado,
        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId
) {
}
