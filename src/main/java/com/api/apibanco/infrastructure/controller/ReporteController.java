package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.response.ReporteEstadoCuentaResponse;
import com.api.apibanco.application.service.ReporteService;
import com.api.apibanco.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public ResponseEntity<ApiResponse<ReporteEstadoCuentaResponse>> generarEstadoCuenta(
            @RequestParam Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Reporte generado correctamente",
                reporteService.generarEstadoCuenta(clienteId, fechaInicio, fechaFin)
        ));
    }
}
