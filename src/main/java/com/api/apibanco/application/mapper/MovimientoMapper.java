package com.api.apibanco.application.mapper;

import com.api.apibanco.application.dto.request.MovimientoRequest;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import com.api.apibanco.domain.model.Cuenta;
import com.api.apibanco.domain.model.Movimiento;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class MovimientoMapper {

    public static final BiFunction<MovimientoRequest, Cuenta, Movimiento> TO_ENTITY = (request, cuenta) -> Movimiento.builder()
            .fecha(request.fecha())
            .tipoMovimiento(request.tipoMovimiento())
            .valor(request.valor())
            .cuenta(cuenta)
            .build();

    public static void copyFields(Movimiento movimiento, MovimientoRequest request, Cuenta cuenta) {
        movimiento.setFecha(request.fecha());
        movimiento.setTipoMovimiento(request.tipoMovimiento());
        movimiento.setValor(request.valor());
        movimiento.setCuenta(cuenta);
    }

    public static final Function<Movimiento, MovimientoResponse> TO_RESPONSE = movimiento -> new MovimientoResponse(
            movimiento.getId(),
            movimiento.getFecha(),
            movimiento.getTipoMovimiento(),
            movimiento.getValor(),
            movimiento.getSaldo(),
            movimiento.getEstado(),
            movimiento.getCuenta().getId()
    );

    private MovimientoMapper() {
    }
}
