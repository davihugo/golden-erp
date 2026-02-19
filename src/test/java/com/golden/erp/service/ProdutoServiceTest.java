package com.golden.erp.service;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.request.ProdutoRequest;
import com.golden.erp.dto.response.ProdutoResponse;
import com.golden.erp.exception.ResourceAlreadyExistsException;
import com.golden.erp.exception.ResourceNotFoundException;
import com.golden.erp.mapper.ProdutoMapper;
import com.golden.erp.repository.ProdutoRepository;
import com.golden.erp.service.impl.ProdutoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private ProdutoServiceImpl produtoService;

    private ProdutoRequest produtoRequest;
    private Produto produto;
    private ProdutoResponse produtoResponse;

    @BeforeEach
    void setUp() {
        produtoRequest = new ProdutoRequest();
        produtoRequest.setSku("SKU123");
        produtoRequest.setNome("Produto Teste");
        produtoRequest.setPrecoBruto(new BigDecimal("100.00"));
        produtoRequest.setEstoque(10);
        produtoRequest.setEstoqueMinimo(5);
        produtoRequest.setAtivo(true);

        produto = new Produto();
        produto.setId(1L);
        produto.setSku("SKU123");
        produto.setNome("Produto Teste");
        produto.setPrecoBruto(new BigDecimal("100.00"));
        produto.setEstoque(10);
        produto.setEstoqueMinimo(5);
        produto.setAtivo(true);

        produtoResponse = new ProdutoResponse();
        produtoResponse.setId(1L);
        produtoResponse.setSku("SKU123");
        produtoResponse.setNome("Produto Teste");
        produtoResponse.setPrecoBruto(new BigDecimal("100.00"));
        produtoResponse.setEstoque(10);
        produtoResponse.setEstoqueMinimo(5);
        produtoResponse.setAtivo(true);
    }

    @Test
    void criar_DeveRetornarProdutoResponse_QuandoDadosValidos() {
        // Arrange
        when(produtoRepository.existsBySku(anyString())).thenReturn(false);
        when(produtoMapper.toEntity(any(ProdutoRequest.class))).thenReturn(produto);
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);
        when(produtoMapper.toResponse(any(Produto.class))).thenReturn(produtoResponse);

        // Act
        ProdutoResponse result = produtoService.criar(produtoRequest);

        // Assert
        assertNotNull(result);
        assertEquals(produtoResponse.getId(), result.getId());
        assertEquals(produtoResponse.getSku(), result.getSku());
        assertEquals(produtoResponse.getNome(), result.getNome());
        
        verify(produtoRepository).existsBySku(produtoRequest.getSku());
        verify(produtoMapper).toEntity(produtoRequest);
        verify(produtoRepository).save(produto);
        verify(produtoMapper).toResponse(produto);
    }

    @Test
    void criar_DeveLancarResourceAlreadyExistsException_QuandoSkuJaExiste() {
        // Arrange
        when(produtoRepository.existsBySku(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            produtoService.criar(produtoRequest);
        });
        
        verify(produtoRepository).existsBySku(produtoRequest.getSku());
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void buscarPorId_DeveRetornarProdutoResponse_QuandoProdutoExiste() {
        // Arrange
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponse(any(Produto.class))).thenReturn(produtoResponse);

        // Act
        ProdutoResponse result = produtoService.buscarPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(produtoResponse.getId(), result.getId());
        assertEquals(produtoResponse.getSku(), result.getSku());
        
        verify(produtoRepository).findById(1L);
        verify(produtoMapper).toResponse(produto);
    }

    @Test
    void buscarPorId_DeveLancarResourceNotFoundException_QuandoProdutoNaoExiste() {
        // Arrange
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            produtoService.buscarPorId(1L);
        });
        
        verify(produtoRepository).findById(1L);
        verify(produtoMapper, never()).toResponse(any(Produto.class));
    }

    @Test
    void listarAtivos_DeveRetornarPaginaDeProdutosAtivos() {
        // Arrange
        Page<Produto> produtoPage = new PageImpl<>(Collections.singletonList(produto));
        when(produtoRepository.findByAtivoTrue(any(Pageable.class))).thenReturn(produtoPage);
        when(produtoMapper.toResponse(any(Produto.class))).thenReturn(produtoResponse);

        // Act
        Page<ProdutoResponse> result = produtoService.listarAtivos(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(produtoResponse.getId(), result.getContent().get(0).getId());
        
        verify(produtoRepository).findByAtivoTrue(any(Pageable.class));
        verify(produtoMapper).toResponse(produto);
    }

    @Test
    void atualizarEstoque_DeveAtualizarEstoque_QuandoProdutoExiste() {
        // Arrange
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        // Act
        produtoService.atualizarEstoque(1L, 5);

        // Assert
        assertEquals(15, produto.getEstoque()); // 10 + 5
        verify(produtoRepository).findById(1L);
        verify(produtoRepository).save(produto);
    }

    @Test
    void atualizarEstoque_DeveLancarIllegalArgumentException_QuandoEstoqueFicaNegativo() {
        // Arrange
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.of(produto));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            produtoService.atualizarEstoque(1L, -15); // 10 - 15 = -5
        });
        
        verify(produtoRepository).findById(1L);
        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void listarProdutosComEstoqueBaixo_DeveRetornarListaDeProdutos() {
        // Arrange
        Produto produtoEstoqueBaixo = new Produto();
        produtoEstoqueBaixo.setId(2L);
        produtoEstoqueBaixo.setSku("SKU456");
        produtoEstoqueBaixo.setNome("Produto Estoque Baixo");
        produtoEstoqueBaixo.setPrecoBruto(new BigDecimal("50.00"));
        produtoEstoqueBaixo.setEstoque(3);
        produtoEstoqueBaixo.setEstoqueMinimo(5);
        produtoEstoqueBaixo.setAtivo(true);

        ProdutoResponse produtoEstoqueBaixoResponse = new ProdutoResponse();
        produtoEstoqueBaixoResponse.setId(2L);
        produtoEstoqueBaixoResponse.setSku("SKU456");
        produtoEstoqueBaixoResponse.setNome("Produto Estoque Baixo");
        produtoEstoqueBaixoResponse.setPrecoBruto(new BigDecimal("50.00"));
        produtoEstoqueBaixoResponse.setEstoque(3);
        produtoEstoqueBaixoResponse.setEstoqueMinimo(5);
        produtoEstoqueBaixoResponse.setAtivo(true);

        List<Produto> produtosEstoqueBaixo = Arrays.asList(produtoEstoqueBaixo);
        when(produtoRepository.findAllWithLowStock()).thenReturn(produtosEstoqueBaixo);
        when(produtoMapper.toResponse(produtoEstoqueBaixo)).thenReturn(produtoEstoqueBaixoResponse);

        // Act
        List<ProdutoResponse> result = produtoService.listarProdutosComEstoqueBaixo();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(produtoEstoqueBaixoResponse.getId(), result.get(0).getId());
        assertEquals(produtoEstoqueBaixoResponse.getSku(), result.get(0).getSku());
        
        verify(produtoRepository).findAllWithLowStock();
        verify(produtoMapper).toResponse(produtoEstoqueBaixo);
    }
}
