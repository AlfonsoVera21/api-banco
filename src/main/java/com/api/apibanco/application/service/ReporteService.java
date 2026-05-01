package com.api.apibanco.application.service;

import com.api.apibanco.application.dto.response.ReporteEstadoCuentaResponse;

import java.time.LocalDate;

public interface ReporteService {

    ReporteEstadoCuentaResponse generarEstadoCuenta(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
