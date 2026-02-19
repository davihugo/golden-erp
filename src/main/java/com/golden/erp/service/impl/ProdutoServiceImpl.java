package com.golden.erp.service.impl;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.request.ProdutoRequest;
import com.golden.erp.dto.response.ProdutoResponse;
import com.golden.erp.exception.ResourceAlreadyExistsException;
import com.golden.erp.exception.ResourceNotFoundException;
import com.golden.erp.mapper.ProdutoMapper;
import com.golden.erp.repository.ProdutoRepository;
import com.golden.erp.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoServiceImpl.class);
    
    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository, ProdutoMapper produtoMapper) {
        this.produtoRepository = produtoRepository;
        this.produtoMapper = produtoMapper;
    }

    @Override
    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        // Verificar se já existe produto com o mesmo SKU
        if (produtoRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("Produto", "sku", request.getSku());
        }
        
        // Converter para entidade e salvar
        Produto produto = produtoMapper.toEntity(request);
        
        // Definir valores padrão se não foram fornecidos
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        
        produto = produtoRepository.save(produto);
        
        logger.info("Produto criado com sucesso: {}", produto.getSku());
        return produtoMapper.toResponse(produto);
    }

    @Override
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        
        return produtoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        
        // Verificar se já existe outro produto com o mesmo SKU
        produtoRepository.findBySku(request.getSku())
                .ifPresent(p -> {
                    if (!p.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException("Produto", "sku", request.getSku());
                    }
                });
        
        // Atualizar a entidade e salvar
        produtoMapper.updateEntityFromRequest(request, produto);
        produto.setUpdatedAt(LocalDateTime.now());
        produto = produtoRepository.save(produto);
        
        logger.info("Produto atualizado com sucesso: {}", produto.getSku());
        return produtoMapper.toResponse(produto);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto", "id", id);
        }
        
        produtoRepository.deleteById(id);
        logger.info("Produto excluído com sucesso: {}", id);
    }

    @Override
    public Page<ProdutoResponse> listar(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(produtoMapper::toResponse);
    }

    @Override
    public Page<ProdutoResponse> listarAtivos(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable)
                .map(produtoMapper::toResponse);
    }

    @Override
    public Page<ProdutoResponse> buscarPorNome(String nome, Pageable pageable) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(produtoMapper::toResponse);
    }

    @Override
    public Page<ProdutoResponse> buscarAtivosPorNome(String nome, Pageable pageable) {
        return produtoRepository.findByAtivoTrueAndNomeContainingIgnoreCase(nome, pageable)
                .map(produtoMapper::toResponse);
    }

    @Override
    @Transactional
    public void atualizarEstoque(Long id, Integer quantidade) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        
        int novoEstoque = produto.getEstoque() + quantidade;
        
        // Verificar se o estoque ficará negativo
        if (novoEstoque < 0) {
            logger.error("Tentativa de deixar estoque negativo para o produto {}: {} + {} = {}", 
                    produto.getSku(), produto.getEstoque(), quantidade, novoEstoque);
            throw new IllegalArgumentException("Estoque insuficiente para o produto " + produto.getSku());
        }
        
        produto.setEstoque(novoEstoque);
        produto.setUpdatedAt(LocalDateTime.now());
        produtoRepository.save(produto);
        
        // Logar se o estoque estiver abaixo do mínimo
        if (novoEstoque <= produto.getEstoqueMinimo()) {
            logger.warn("Estoque baixo para o produto {}: {} (mínimo: {})", 
                    produto.getSku(), novoEstoque, produto.getEstoqueMinimo());
        }
        
        logger.info("Estoque atualizado para o produto {}: {} -> {}", 
                produto.getSku(), produto.getEstoque() - quantidade, novoEstoque);
    }

    @Override
    public List<ProdutoResponse> listarProdutosComEstoqueBaixo() {
        List<Produto> produtosEstoqueBaixo = produtoRepository.findAllWithLowStock();
        
        return produtosEstoqueBaixo.stream()
                .map(produtoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
