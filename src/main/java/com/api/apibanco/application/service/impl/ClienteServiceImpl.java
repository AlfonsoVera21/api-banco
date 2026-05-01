package com.api.apibanco.application.service.impl;

import com.api.apibanco.application.dto.request.ClienteRequest;
import com.api.apibanco.application.dto.response.ClienteResponse;
import com.api.apibanco.application.mapper.ClienteMapper;
import com.api.apibanco.application.service.ClienteService;
import com.api.apibanco.domain.model.Cliente;
import com.api.apibanco.domain.model.Cuenta;
import com.api.apibanco.domain.repository.ClienteRepository;
import com.api.apibanco.domain.repository.CuentaRepository;
import com.api.apibanco.infrastructure.exception.ConflictException;
import com.api.apibanco.infrastructure.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarDuplicados(request);
        Cliente cliente = ClienteMapper.toEntity(request);
        return ClienteMapper.TO_RESPONSE.apply(clienteRepository.save(cliente));
    }

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findByEstadoTrue().stream()
                .map(ClienteMapper.TO_RESPONSE)
                .toList();
    }

    @Override
    public Page<ClienteResponse> listar(String q, Pageable pageable) {
        return clienteRepository.buscarActivos(normalizarBusqueda(q), pageable)
                .map(ClienteMapper.TO_RESPONSE);
    }

    @Override
    public ClienteResponse obtenerPorId(Long id) {
        return clienteRepository.findByIdAndEstadoTrue(id)
                .map(ClienteMapper.TO_RESPONSE)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        validarDuplicados(id, request);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));

        ClienteMapper.copyClienteFields(cliente, request);

        return ClienteMapper.TO_RESPONSE.apply(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));
        List<Cuenta> cuentas = cuentaRepository.findByClienteIdAndEstadoTrue(id);
        for (Cuenta cuenta : cuentas) {
            cuenta.setEstado(Boolean.FALSE);
            cuenta.marcarEliminado();
        }
        cuentaRepository.saveAll(cuentas);
        cliente.setEstado(Boolean.FALSE);
        cliente.marcarEliminado();
        clienteRepository.save(cliente);
    }

    private void validarDuplicados(ClienteRequest request) {
        if (clienteRepository.existsByIdentificacion(request.identificacion())) {
            throw new ConflictException("Ya existe una persona con identificacion " + request.identificacion());
        }
    }

    private void validarDuplicados(Long id, ClienteRequest request) {
        if (clienteRepository.existsByIdentificacionAndIdNot(request.identificacion(), id)) {
            throw new ConflictException("Ya existe una persona con identificacion " + request.identificacion());
        }
    }

    private String normalizarBusqueda(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim().toLowerCase();
    }
}
