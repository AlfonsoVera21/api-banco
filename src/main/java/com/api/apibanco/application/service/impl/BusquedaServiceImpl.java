package com.api.apibanco.application.service.impl;

import com.api.apibanco.application.dto.response.BusquedaGlobalResponse;
import com.api.apibanco.application.dto.response.ClienteResponse;
import com.api.apibanco.application.dto.response.CuentaResponse;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import com.api.apibanco.application.service.BusquedaService;
import com.api.apibanco.application.service.ClienteService;
import com.api.apibanco.application.service.CuentaService;
import com.api.apibanco.application.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusquedaServiceImpl implements BusquedaService {

    private static final int SIZE_DEFAULT = 5;

    private final ClienteService clienteService;
    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;

    @Override
    public BusquedaGlobalResponse buscar(String q) {
        PageRequest pageRequest = PageRequest.of(0, SIZE_DEFAULT, Sort.by("id").ascending());
        Page<ClienteResponse> clientes = clienteService.listar(q, pageRequest);
        Page<CuentaResponse> cuentas = cuentaService.listar(q, pageRequest);
        Page<MovimientoResponse> movimientos = movimientoService.listar(q, pageRequest);

        return new BusquedaGlobalResponse(
                normalizarTermino(q),
                clientes.getTotalElements(),
                cuentas.getTotalElements(),
                movimientos.getTotalElements(),
                clientes.getContent(),
                cuentas.getContent(),
                movimientos.getContent()
        );
    }

    private String normalizarTermino(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }
}
