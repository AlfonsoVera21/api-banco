package com.api.apibanco.application.service.impl;

import com.api.apibanco.application.dto.request.CuentaRequest;
import com.api.apibanco.application.dto.response.CuentaResponse;
import com.api.apibanco.application.mapper.CuentaMapper;
import com.api.apibanco.application.service.CuentaService;
import com.api.apibanco.domain.model.Cliente;
import com.api.apibanco.domain.model.Cuenta;
import com.api.apibanco.domain.model.Movimiento;
import com.api.apibanco.domain.repository.ClienteRepository;
import com.api.apibanco.domain.repository.CuentaRepository;
import com.api.apibanco.domain.repository.MovimientoRepository;
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
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.numeroCuenta())) {
            throw new ConflictException("Ya existe una cuenta con el numero " + request.numeroCuenta());
        }

        Cliente cliente = clienteRepository.findByIdAndEstadoTrue(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + request.clienteId()));

        return CuentaMapper.TO_RESPONSE.apply(
                cuentaRepository.save(CuentaMapper.TO_ENTITY.apply(request, cliente))
        );
    }

    @Override
    public List<CuentaResponse> listar() {
        return cuentaRepository.findByEstadoTrueAndClienteEstadoTrue().stream()
                .map(CuentaMapper.TO_RESPONSE)
                .toList();
    }

    @Override
    public Page<CuentaResponse> listar(String q, Pageable pageable) {
        return cuentaRepository.buscarActivas(normalizarBusqueda(q), pageable)
                .map(CuentaMapper.TO_RESPONSE);
    }

    @Override
    public CuentaResponse obtenerPorId(Long id) {
        return cuentaRepository.findByIdAndEstadoTrueAndClienteEstadoTrue(id)
                .map(CuentaMapper.TO_RESPONSE)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + id));
    }

    @Override
    @Transactional
    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuentaAndIdNot(request.numeroCuenta(), id)) {
            throw new ConflictException("Ya existe una cuenta con el numero " + request.numeroCuenta());
        }

        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + id));
        Cliente cliente = clienteRepository.findByIdAndEstadoTrue(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + request.clienteId()));

        CuentaMapper.copyFields(cuenta, request, cliente);
        return CuentaMapper.TO_RESPONSE.apply(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + id));
        List<Movimiento> movimientos = movimientoRepository.findByCuentaIdAndEstadoTrue(id);
        for (Movimiento movimiento : movimientos) {
            movimiento.setEstado(Boolean.FALSE);
            movimiento.marcarEliminado();
        }
        movimientoRepository.saveAll(movimientos);
        cuenta.setEstado(Boolean.FALSE);
        cuenta.marcarEliminado();
        cuentaRepository.save(cuenta);
    }

    private String normalizarBusqueda(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim().toLowerCase();
    }
}
