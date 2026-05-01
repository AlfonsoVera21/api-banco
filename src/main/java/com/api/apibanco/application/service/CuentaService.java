package com.api.apibanco.application.service;

import com.api.apibanco.application.dto.request.CuentaRequest;
import com.api.apibanco.application.dto.response.CuentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CuentaService {

    CuentaResponse crear(CuentaRequest request);

    List<CuentaResponse> listar();

    Page<CuentaResponse> listar(String q, Pageable pageable);

    CuentaResponse obtenerPorId(Long id);

    CuentaResponse actualizar(Long id, CuentaRequest request);

    void eliminar(Long id);
}
