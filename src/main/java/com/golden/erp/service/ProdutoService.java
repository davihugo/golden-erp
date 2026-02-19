package com.golden.erp.service;

import com.golden.erp.dto.request.ProdutoRequest;
import com.golden.erp.dto.response.ProdutoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProdutoService {
    
    ProdutoResponse criar(ProdutoRequest request);
    
    ProdutoResponse buscarPorId(Long id);
    
    ProdutoResponse atualizar(Long id, ProdutoRequest request);
    
    void excluir(Long id);
    
    Page<ProdutoResponse> listar(Pageable pageable);
    
    Page<ProdutoResponse> listarAtivos(Pageable pageable);
    
    Page<ProdutoResponse> buscarPorNome(String nome, Pageable pageable);
    
    Page<ProdutoResponse> buscarAtivosPorNome(String nome, Pageable pageable);
    
    void atualizarEstoque(Long id, Integer quantidade);
    
    List<ProdutoResponse> listarProdutosComEstoqueBaixo();
}
