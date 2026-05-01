package com.api.apibanco.application.mapper;

import com.api.apibanco.application.dto.request.CuentaRequest;
import com.api.apibanco.application.dto.response.CuentaResponse;
import com.api.apibanco.domain.model.Cliente;
import com.api.apibanco.domain.model.Cuenta;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class CuentaMapper {

    public static final BiFunction<CuentaRequest, Cliente, Cuenta> TO_ENTITY = (request, cliente) -> Cuenta.builder()
            .numeroCuenta(request.numeroCuenta())
            .tipoCuenta(request.tipoCuenta())
            .saldoInicial(request.saldoInicial())
            .estado(request.estado() == null ? Boolean.TRUE : request.estado())
            .cliente(cliente)
            .build();

    public static void copyFields(Cuenta cuenta, CuentaRequest request, Cliente cliente) {
        cuenta.setNumeroCuenta(request.numeroCuenta());
        cuenta.setTipoCuenta(request.tipoCuenta());
        cuenta.setSaldoInicial(request.saldoInicial());
        cuenta.setEstado(request.estado() == null ? Boolean.TRUE : request.estado());
        cuenta.setCliente(cliente);
    }

    public static final Function<Cuenta, CuentaResponse> TO_RESPONSE = cuenta -> new CuentaResponse(
            cuenta.getId(),
            cuenta.getNumeroCuenta(),
            cuenta.getTipoCuenta(),
            cuenta.getSaldoInicial(),
            cuenta.getEstado(),
            cuenta.getCliente().getId()
    );

    private CuentaMapper() {
    }
}
