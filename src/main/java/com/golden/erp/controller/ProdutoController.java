package com.golden.erp.controller;

import com.golden.erp.dto.request.ProdutoRequest;
import com.golden.erp.dto.response.ProdutoResponse;
import com.golden.erp.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(produtoService.listar(pageable));
    }

    @GetMapping("/ativos")
    public ResponseEntity<Page<ProdutoResponse>> listarAtivos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarAtivos(pageable));
    }

    @GetMapping("/por-nome")
    public ResponseEntity<Page<ProdutoResponse>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produtoService.buscarPorNome(nome, pageable));
    }

    @GetMapping("/ativos/por-nome")
    public ResponseEntity<Page<ProdutoResponse>> buscarAtivosPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(produtoService.buscarAtivosPorNome(nome, pageable));
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoResponse>> listarProdutosComEstoqueBaixo() {
        return ResponseEntity.ok(produtoService.listarProdutosComEstoqueBaixo());
    }
}
