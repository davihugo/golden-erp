package com.golden.erp.controller;

import com.golden.erp.dto.request.ClienteRequest;
import com.golden.erp.dto.response.ClienteResponse;
import com.golden.erp.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(clienteService.listar(pageable));
    }

    @GetMapping("/por-nome")
    public ResponseEntity<Page<ClienteResponse>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscarPorNome(nome, pageable));
    }

    @GetMapping("/por-email")
    public ResponseEntity<Page<ClienteResponse>> buscarPorEmail(
            @RequestParam String email,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscarPorEmail(email, pageable));
    }
}
