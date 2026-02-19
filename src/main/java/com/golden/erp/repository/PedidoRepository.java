package com.golden.erp.repository;

import com.golden.erp.domain.Pedido;
import com.golden.erp.domain.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    
    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);
    
    Page<Pedido> findByClienteIdAndStatus(Long clienteId, StatusPedido status, Pageable pageable);
    
    @Query("SELECT p FROM Pedido p WHERE p.status = :status AND p.createdAt <= :limitDate")
    List<Pedido> findByStatusAndCreatedAtBefore(
            @Param("status") StatusPedido status, 
            @Param("limitDate") LocalDateTime limitDate);
}
