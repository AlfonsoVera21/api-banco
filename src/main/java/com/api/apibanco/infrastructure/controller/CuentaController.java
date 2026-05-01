package com.api.apibanco.infrastructure.controller;

import com.api.apibanco.application.dto.request.CuentaRequest;
import com.api.apibanco.application.dto.response.CuentaResponse;
import com.api.apibanco.application.dto.response.PageResponse;
import com.api.apibanco.application.service.CuentaService;
import com.api.apibanco.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private static final int PAGE_DEFAULT = 0;
    private static final int SIZE_DEFAULT = 20;

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CuentaResponse>> crear(@Valid @RequestBody CuentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Cuenta creada correctamente", cuentaService.crear(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CuentaResponse>>> listar(
            @RequestParam(defaultValue = "") String busqueda
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Cuentas consultadas correctamente",
                PageResponse.from(cuentaService.listar(busqueda, PageRequest.of(PAGE_DEFAULT, SIZE_DEFAULT, Sort.by("id").ascending())))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CuentaResponse>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Cuenta consultada correctamente", cuentaService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CuentaResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody CuentaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cuenta actualizada correctamente", cuentaService.actualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cuentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
