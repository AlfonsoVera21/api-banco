package com.api.apibanco.application.service;

import com.api.apibanco.application.dto.request.ClienteRequest;
import com.api.apibanco.application.dto.response.ClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    List<ClienteResponse> listar();

    Page<ClienteResponse> listar(String q, Pageable pageable);

    ClienteResponse obtenerPorId(Long id);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    void eliminar(Long id);
}
