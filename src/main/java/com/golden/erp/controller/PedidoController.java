package com.golden.erp.controller;

import com.golden.erp.domain.enums.StatusPedido;
import com.golden.erp.dto.request.PedidoRequest;
import com.golden.erp.dto.response.PedidoResponse;
import com.golden.erp.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<PedidoResponse>> listar(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listar(pageable));
    }

    @GetMapping("/por-status")
    public ResponseEntity<Page<PedidoResponse>> listarPorStatus(
            @RequestParam StatusPedido status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorStatus(status, pageable));
    }

    @GetMapping("/por-cliente/{clienteId}")
    public ResponseEntity<Page<PedidoResponse>> listarPorCliente(
            @PathVariable Long clienteId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId, pageable));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<PedidoResponse> pagar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.pagar(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }
}
