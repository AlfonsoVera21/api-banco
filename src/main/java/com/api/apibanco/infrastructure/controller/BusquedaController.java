package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.response.BusquedaGlobalResponse;
import com.api.apibanco.application.service.BusquedaService;
import com.api.apibanco.infrastructure.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/busqueda")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    @GetMapping
    public ResponseEntity<ApiResponse<BusquedaGlobalResponse>> buscar(
            @RequestParam(defaultValue = "") String busqueda
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Busqueda consultada correctamente", busquedaService.buscar(busqueda)));
    }
}
