package com.golden.erp.service;

import com.golden.erp.domain.enums.StatusPedido;
import com.golden.erp.dto.request.PedidoRequest;
import com.golden.erp.dto.response.PedidoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {
    
    PedidoResponse criar(PedidoRequest request);
    
    PedidoResponse buscarPorId(Long id);
    
    Page<PedidoResponse> listar(Pageable pageable);
    
    Page<PedidoResponse> listarPorStatus(StatusPedido status, Pageable pageable);
    
    Page<PedidoResponse> listarPorCliente(Long clienteId, Pageable pageable);
    
    PedidoResponse pagar(Long id);
    
    PedidoResponse cancelar(Long id);
    
    void processarPedidosAtrasados();
}
