package com.api.apibanco.domain.repository;

import com.api.apibanco.domain.model.Movimiento;
import com.api.apibanco.domain.model.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByEstadoTrue();

    List<Movimiento> findByEstadoTrueAndCuentaEstadoTrueAndCuentaClienteEstadoTrue();

    @Query("""
            select m
            from Movimiento m
            join m.cuenta cuenta
            join cuenta.cliente cliente
            where m.estado = true
              and cuenta.estado = true
              and cliente.estado = true
              and (
                  :q is null
                  or lower(cast(m.tipoMovimiento as string)) like concat('%', :q, '%')
                  or lower(cuenta.numeroCuenta) like concat('%', :q, '%')
                  or lower(cliente.nombre) like concat('%', :q, '%')
                  or lower(cliente.identificacion) like concat('%', :q, '%')
                  or lower(cast(m.fecha as string)) like concat('%', :q, '%')
                  or str(m.id) like concat('%', :q, '%')
                  or str(m.valor) like concat('%', :q, '%')
                  or str(m.saldo) like concat('%', :q, '%')
              )
            """)
    Page<Movimiento> buscarActivos(@Param("q") String q, Pageable pageable);

    Optional<Movimiento> findByIdAndEstadoTrueAndCuentaEstadoTrueAndCuentaClienteEstadoTrue(Long id);

    List<Movimiento> findByCuentaIdAndEstadoTrue(Long cuentaId);

    List<Movimiento> findByCuentaIdAndEstadoTrueOrderByFechaAscIdAsc(Long cuentaId);

    Optional<Movimiento> findFirstByCuentaIdAndEstadoTrueOrderByFechaDescIdDesc(Long cuentaId);

    @Query("""
            select coalesce(sum(abs(m.valor)), 0)
            from Movimiento m
            where m.cuenta.cliente.id = :clienteId
              and m.estado = true
              and m.cuenta.estado = true
              and m.cuenta.cliente.estado = true
              and m.tipoMovimiento = :tipoMovimiento
              and m.fecha >= :fechaInicio
              and m.fecha < :fechaFin
              and (:movimientoId is null or m.id <> :movimientoId)
            """)
    BigDecimal totalMovimientosPorClienteYFecha(
            @Param("clienteId") Long clienteId,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("movimientoId") Long movimientoId
    );

    @Query("""
            select m
            from Movimiento m
            where m.cuenta.cliente.id = :clienteId
              and m.estado = true
              and m.fecha >= :fechaInicio
              and m.fecha <= :fechaFin
            order by m.fecha asc, m.id asc
            """)
    List<Movimiento> findReporteMovimientos(
            @Param("clienteId") Long clienteId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
