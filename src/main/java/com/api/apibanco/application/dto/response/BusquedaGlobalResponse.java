package com.api.apibanco.application.dto.response;

import java.util.List;

public record BusquedaGlobalResponse(
        String termino,
        long totalClientes,
        long totalCuentas,
        long totalMovimientos,
        List<ClienteResponse> clientes,
        List<CuentaResponse> cuentas,
        List<MovimientoResponse> movimientos
) {
}
