package com.api.apibanco.application.service.impl;

import com.api.apibanco.application.dto.request.MovimientoRequest;
import com.api.apibanco.application.dto.request.TransferenciaRequest;
import com.api.apibanco.application.dto.response.MovimientoResponse;
import com.api.apibanco.application.dto.response.TransferenciaResponse;
import com.api.apibanco.application.mapper.MovimientoMapper;
import com.api.apibanco.application.service.MovimientoService;
import com.api.apibanco.domain.model.Cuenta;
import com.api.apibanco.domain.model.Movimiento;
import com.api.apibanco.domain.model.enums.TipoMovimiento;
import com.api.apibanco.domain.repository.CuentaRepository;
import com.api.apibanco.domain.repository.MovimientoRepository;
import com.api.apibanco.infrastructure.exception.BusinessException;
import com.api.apibanco.infrastructure.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private static final BigDecimal CERO = BigDecimal.ZERO;

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Value("${banco.movimientos.limite-diario-retiro:1000}")
    private BigDecimal limiteDiarioRetiro;

    @Override
    @Transactional
    public MovimientoResponse crear(MovimientoRequest request) {
        Cuenta cuenta = cuentaRepository.findByIdAndEstadoTrueAndClienteEstadoTrue(request.cuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + request.cuentaId()));

        validarCuentaActiva(cuenta);
        validarValor(request.valor());
        validarSaldoDisponible(cuenta, request, null);
        validarLimiteDiario(cuenta, request, null);

        Movimiento movimiento = MovimientoMapper.TO_ENTITY.apply(request, cuenta);
        movimiento.setFecha(request.fecha() == null ? LocalDateTime.now() : request.fecha());
        movimiento.setValor(normalizarValor(request.tipoMovimiento(), request.valor()));
        movimiento.setSaldo(calcularSaldoProvisional(cuenta, movimiento.getValor()));
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);
        recalcularSaldos(cuenta.getId());
        return MovimientoMapper.TO_RESPONSE.apply(movimientoGuardado);
    }

    @Override
    @Transactional
    public TransferenciaResponse transferir(TransferenciaRequest request) {
        validarTransferencia(request);
        Cuenta cuentaOrigen = cuentaRepository.findByIdAndEstadoTrueAndClienteEstadoTrue(request.cuentaOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta origen no encontrada con id " + request.cuentaOrigenId()));
        Cuenta cuentaDestino = cuentaRepository.findByIdAndEstadoTrueAndClienteEstadoTrue(request.cuentaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta destino no encontrada con id " + request.cuentaDestinoId()));

        validarCuentaActiva(cuentaOrigen);
        validarCuentaActiva(cuentaDestino);

        LocalDateTime fechaTransferencia = request.fecha() == null ? LocalDateTime.now() : request.fecha();
        MovimientoRequest retiroRequest = new MovimientoRequest(
                fechaTransferencia,
                TipoMovimiento.RETIRO,
                request.valor(),
                cuentaOrigen.getId()
        );

        validarSaldoDisponible(cuentaOrigen, retiroRequest, null);
        validarLimiteDiario(cuentaOrigen, retiroRequest, null);

        Movimiento movimientoOrigen = Movimiento.builder()
                .fecha(fechaTransferencia)
                .tipoMovimiento(TipoMovimiento.RETIRO)
                .valor(request.valor().abs().negate())
                .saldo(calcularSaldoProvisional(cuentaOrigen, request.valor().abs().negate()))
                .cuenta(cuentaOrigen)
                .build();
        Movimiento movimientoDestino = Movimiento.builder()
                .fecha(fechaTransferencia)
                .tipoMovimiento(TipoMovimiento.DEPOSITO)
                .valor(request.valor().abs())
                .saldo(calcularSaldoProvisional(cuentaDestino, request.valor().abs()))
                .cuenta(cuentaDestino)
                .build();

        Movimiento movimientoOrigenGuardado = movimientoRepository.save(movimientoOrigen);
        Movimiento movimientoDestinoGuardado = movimientoRepository.save(movimientoDestino);

        recalcularSaldos(cuentaOrigen.getId());
        recalcularSaldos(cuentaDestino.getId());

        return new TransferenciaResponse(
                MovimientoMapper.TO_RESPONSE.apply(movimientoOrigenGuardado),
                MovimientoMapper.TO_RESPONSE.apply(movimientoDestinoGuardado)
        );
    }

    @Override
    public List<MovimientoResponse> listar() {
        return movimientoRepository.findByEstadoTrueAndCuentaEstadoTrueAndCuentaClienteEstadoTrue().stream()
                .map(MovimientoMapper.TO_RESPONSE)
                .toList();
    }

    @Override
    public Page<MovimientoResponse> listar(String q, Pageable pageable) {
        return movimientoRepository.buscarActivos(normalizarBusqueda(q), pageable)
                .map(MovimientoMapper.TO_RESPONSE);
    }

    @Override
    public MovimientoResponse obtenerPorId(Long id) {
        return movimientoRepository.findByIdAndEstadoTrueAndCuentaEstadoTrueAndCuentaClienteEstadoTrue(id)
                .map(MovimientoMapper.TO_RESPONSE)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id " + id));
    }

    @Override
    @Transactional
    public MovimientoResponse actualizar(Long id, MovimientoRequest request) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id " + id));
        Cuenta cuenta = cuentaRepository.findByIdAndEstadoTrueAndClienteEstadoTrue(request.cuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + request.cuentaId()));

        validarCuentaActiva(cuenta);
        validarValor(request.valor());
        validarSaldoDisponible(cuenta, request, id);
        validarLimiteDiario(cuenta, request, id);
        Long cuentaAnteriorId = movimiento.getCuenta().getId();

        MovimientoMapper.copyFields(movimiento, request, cuenta);
        movimiento.setFecha(request.fecha() == null ? LocalDateTime.now() : request.fecha());
        movimiento.setValor(normalizarValor(request.tipoMovimiento(), request.valor()));
        movimiento.setSaldo(calcularSaldoProvisional(cuenta, movimiento.getValor()));
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);
        recalcularSaldos(cuentaAnteriorId);
        if (!cuentaAnteriorId.equals(cuenta.getId())) {
            recalcularSaldos(cuenta.getId());
        }
        return MovimientoMapper.TO_RESPONSE.apply(movimientoGuardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id " + id));
        Long cuentaId = movimiento.getCuenta().getId();
        movimiento.setEstado(Boolean.FALSE);
        movimiento.marcarEliminado();
        movimientoRepository.save(movimiento);
        recalcularSaldos(cuentaId);
    }

    private void validarCuentaActiva(Cuenta cuenta) {
        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessException("La cuenta se encuentra inactiva");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(CERO) == 0) {
            throw new BusinessException("El valor del movimiento debe ser diferente de cero");
        }
    }

    private void validarTransferencia(TransferenciaRequest request) {
        validarValor(request.valor());
        if (request.valor().compareTo(CERO) < 0) {
            throw new BusinessException("El valor de la transferencia debe ser mayor a cero");
        }
        if (request.cuentaOrigenId() == null || request.cuentaDestinoId() == null) {
            throw new BusinessException("Las cuentas origen y destino son obligatorias");
        }
        if (request.cuentaOrigenId().equals(request.cuentaDestinoId())) {
            throw new BusinessException("La cuenta origen y la cuenta destino deben ser diferentes");
        }
    }

    private BigDecimal calcularSaldoProvisional(Cuenta cuenta, BigDecimal valor) {
        return cuenta.getSaldoInicial().add(valor);
    }

    private void validarSaldoDisponible(Cuenta cuenta, MovimientoRequest request, Long movimientoId) {
        if (request.tipoMovimiento() != TipoMovimiento.RETIRO) {
            return;
        }
        BigDecimal saldoDisponible = movimientoRepository.findFirstByCuentaIdAndEstadoTrueOrderByFechaDescIdDesc(cuenta.getId())
                .filter(movimiento -> movimientoId == null || !movimiento.getId().equals(movimientoId))
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
        if (saldoDisponible.subtract(request.valor().abs()).compareTo(CERO) < 0) {
            throw new BusinessException("Saldo no disponible");
        }
    }

    private void validarLimiteDiario(Cuenta cuenta, MovimientoRequest request, Long movimientoId) {
        if (request.tipoMovimiento() != TipoMovimiento.RETIRO) {
            return;
        }
        LocalDateTime fechaMovimiento = request.fecha() == null ? LocalDateTime.now() : request.fecha();
        LocalDateTime fechaInicio = fechaMovimiento.toLocalDate().atStartOfDay();
        LocalDateTime fechaFin = fechaInicio.plusDays(1);
        BigDecimal totalRetirado = movimientoRepository.totalMovimientosPorClienteYFecha(
                cuenta.getCliente().getId(),
                TipoMovimiento.RETIRO,
                fechaInicio,
                fechaFin,
                movimientoId
        );
        BigDecimal nuevoTotal = totalRetirado.add(request.valor().abs());
        if (nuevoTotal.compareTo(limiteDiarioRetiro) > 0) {
            throw new BusinessException("Cupo diario Excedido");
        }
    }

    private BigDecimal normalizarValor(TipoMovimiento tipoMovimiento, BigDecimal valor) {
        return switch (tipoMovimiento) {
            case DEPOSITO -> valor.abs();
            case RETIRO -> valor.abs().negate();
        };
    }

    private void recalcularSaldos(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con id " + cuentaId));
        BigDecimal saldo = cuenta.getSaldoInicial();
        List<Movimiento> movimientos = movimientoRepository.findByCuentaIdAndEstadoTrueOrderByFechaAscIdAsc(cuentaId);
        for (Movimiento movimiento : movimientos) {
            saldo = saldo.add(movimiento.getValor());
            if (saldo.compareTo(CERO) < 0) {
                throw new BusinessException("Saldo no disponible");
            }
            movimiento.setSaldo(saldo);
        }
    }

    private String normalizarBusqueda(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim().toLowerCase();
    }
}
