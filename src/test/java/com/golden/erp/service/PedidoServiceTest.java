package com.golden.erp.service;

import com.golden.erp.domain.Cliente;
import com.golden.erp.domain.Pedido;
import com.golden.erp.domain.PedidoItem;
import com.golden.erp.domain.Produto;
import com.golden.erp.domain.enums.StatusPedido;
import com.golden.erp.dto.request.PedidoItemRequest;
import com.golden.erp.dto.request.PedidoRequest;
import com.golden.erp.dto.response.PedidoItemResponse;
import com.golden.erp.dto.response.PedidoResponse;
import com.golden.erp.exception.EstoqueInsuficienteException;
import com.golden.erp.exception.ResourceNotFoundException;
import com.golden.erp.mapper.PedidoItemMapper;
import com.golden.erp.mapper.PedidoMapper;
import com.golden.erp.repository.ClienteRepository;
import com.golden.erp.repository.PedidoRepository;
import com.golden.erp.repository.ProdutoRepository;
import com.golden.erp.service.impl.PedidoServiceImpl;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoService produtoService;

    @Mock
    private PedidoMapper pedidoMapper;

    @Mock
    private PedidoItemMapper pedidoItemMapper;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private PedidoRequest pedidoRequest;
    private PedidoItemRequest pedidoItemRequest;
    private Cliente cliente;
    private Produto produto;
    private Pedido pedido;
    private PedidoItem pedidoItem;
    private PedidoResponse pedidoResponse;
    private PedidoItemResponse pedidoItemResponse;

    @BeforeEach
    void setUp() {
        // Configurar objetos de teste
        pedidoItemRequest = new PedidoItemRequest();
        pedidoItemRequest.setProdutoId(1L);
        pedidoItemRequest.setQuantidade(2);
        pedidoItemRequest.setDescontoOpcional(BigDecimal.ZERO);

        pedidoRequest = new PedidoRequest();
        pedidoRequest.setClienteId(1L);
        pedidoRequest.setItens(Collections.singletonList(pedidoItemRequest));

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setEmail("joao@email.com");

        produto = new Produto();
        produto.setId(1L);
        produto.setSku("SKU123");
        produto.setNome("Produto Teste");
        produto.setPrecoBruto(new BigDecimal("100.00"));
        produto.setEstoque(10);
        produto.setEstoqueMinimo(5);
        produto.setAtivo(true);

        pedidoItem = new PedidoItem();
        pedidoItem.setId(1L);
        pedidoItem.setProduto(produto);
        pedidoItem.setQuantidade(2);
        pedidoItem.setPrecoUnitario(new BigDecimal("100.00"));
        pedidoItem.setDesconto(BigDecimal.ZERO);
        pedidoItem.setSubtotal(new BigDecimal("200.00"));

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.CREATED);
        pedido.setSubtotal(new BigDecimal("200.00"));
        pedido.setDescontoTotal(BigDecimal.ZERO);
        pedido.setTotal(new BigDecimal("200.00"));
        pedido.setItens(Collections.singletonList(pedidoItem));
        pedidoItem.setPedido(pedido);

        pedidoItemResponse = new PedidoItemResponse();
        pedidoItemResponse.setId(1L);
        pedidoItemResponse.setProdutoId(1L);
        pedidoItemResponse.setProdutoNome("Produto Teste");
        pedidoItemResponse.setProdutoSku("SKU123");
        pedidoItemResponse.setQuantidade(2);
        pedidoItemResponse.setPrecoUnitario(new BigDecimal("100.00"));
        pedidoItemResponse.setDesconto(BigDecimal.ZERO);
        pedidoItemResponse.setSubtotal(new BigDecimal("200.00"));

        pedidoResponse = new PedidoResponse();
        pedidoResponse.setId(1L);
        pedidoResponse.setClienteId(1L);
        pedidoResponse.setClienteNome("João Silva");
        pedidoResponse.setStatus(StatusPedido.CREATED);
        pedidoResponse.setSubtotal(new BigDecimal("200.00"));
        pedidoResponse.setDescontoTotal(BigDecimal.ZERO);
        pedidoResponse.setTotal(new BigDecimal("200.00"));
        pedidoResponse.setItens(Collections.singletonList(pedidoItemResponse));
    }

    @Test
    void criar_DeveRetornarPedidoResponse_QuandoDadosValidos() {
        // Arrange
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(pedidoMapper.toEntityWithCliente(any(PedidoRequest.class), any(Cliente.class))).thenReturn(pedido);
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.of(produto));
        when(pedidoItemMapper.toEntityWithProduto(any(PedidoItemRequest.class), any(Produto.class))).thenReturn(pedidoItem);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);
        doNothing().when(produtoService).atualizarEstoque(anyLong(), anyInt());

        // Act
        PedidoResponse result = pedidoService.criar(pedidoRequest);

        // Assert
        assertNotNull(result);
        assertEquals(pedidoResponse.getId(), result.getId());
        assertEquals(pedidoResponse.getClienteId(), result.getClienteId());
        assertEquals(pedidoResponse.getStatus(), result.getStatus());
        
        verify(clienteRepository).findById(pedidoRequest.getClienteId());
        verify(pedidoMapper).toEntityWithCliente(eq(pedidoRequest), eq(cliente));
        verify(produtoRepository).findById(pedidoItemRequest.getProdutoId());
        verify(pedidoItemMapper).toEntityWithProduto(eq(pedidoItemRequest), eq(produto));
        verify(produtoService).atualizarEstoque(produto.getId(), -pedidoItemRequest.getQuantidade());
        verify(pedidoRepository).save(pedido);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    void criar_DeveLancarResourceNotFoundException_QuandoClienteNaoExiste() {
        // Arrange
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            pedidoService.criar(pedidoRequest);
        });
        
        verify(clienteRepository).findById(pedidoRequest.getClienteId());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void criar_DeveLancarResourceNotFoundException_QuandoProdutoNaoExiste() {
        // Arrange
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(pedidoMapper.toEntityWithCliente(any(PedidoRequest.class), any(Cliente.class))).thenReturn(pedido);
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            pedidoService.criar(pedidoRequest);
        });
        
        verify(clienteRepository).findById(pedidoRequest.getClienteId());
        verify(produtoRepository).findById(pedidoItemRequest.getProdutoId());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void criar_DeveLancarEstoqueInsuficienteException_QuandoEstoqueInsuficiente() {
        // Arrange
        produto.setEstoque(1); // Estoque menor que a quantidade solicitada (2)
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(pedidoMapper.toEntityWithCliente(any(PedidoRequest.class), any(Cliente.class))).thenReturn(pedido);
        when(produtoRepository.findById(anyLong())).thenReturn(Optional.of(produto));

        // Act & Assert
        assertThrows(EstoqueInsuficienteException.class, () -> {
            pedidoService.criar(pedidoRequest);
        });
        
        verify(clienteRepository).findById(pedidoRequest.getClienteId());
        verify(produtoRepository).findById(pedidoItemRequest.getProdutoId());
        verify(produtoService, never()).atualizarEstoque(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void pagar_DeveRetornarPedidoResponseComStatusPAID_QuandoPedidoExiste() {
        // Arrange
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        
        PedidoResponse paidResponse = new PedidoResponse();
        paidResponse.setId(1L);
        paidResponse.setStatus(StatusPedido.PAID);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(paidResponse);

        // Act
        PedidoResponse result = pedidoService.pagar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(StatusPedido.PAID, result.getStatus());
        assertEquals(StatusPedido.PAID, pedido.getStatus());
        
        verify(pedidoRepository).findById(1L);
        verify(pedidoRepository).save(pedido);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    void pagar_DeveLancarIllegalStateException_QuandoPedidoJaEstaPago() {
        // Arrange
        pedido.setStatus(StatusPedido.PAID);
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            pedidoService.pagar(1L);
        });
        
        verify(pedidoRepository).findById(1L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void cancelar_DeveRetornarPedidoResponseComStatusCANCELLED_QuandoPedidoExiste() {
        // Arrange
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        
        PedidoResponse cancelledResponse = new PedidoResponse();
        cancelledResponse.setId(1L);
        cancelledResponse.setStatus(StatusPedido.CANCELLED);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(cancelledResponse);
        doNothing().when(produtoService).atualizarEstoque(anyLong(), anyInt());

        // Act
        PedidoResponse result = pedidoService.cancelar(1L);

        // Assert
        assertNotNull(result);
        assertEquals(StatusPedido.CANCELLED, result.getStatus());
        assertEquals(StatusPedido.CANCELLED, pedido.getStatus());
        
        verify(pedidoRepository).findById(1L);
        verify(produtoService).atualizarEstoque(produto.getId(), pedidoItem.getQuantidade());
        verify(pedidoRepository).save(pedido);
        verify(pedidoMapper).toResponse(pedido);
    }

    @Test
    void cancelar_DeveLancarIllegalStateException_QuandoPedidoJaEstaCancelado() {
        // Arrange
        pedido.setStatus(StatusPedido.CANCELLED);
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(pedido));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            pedidoService.cancelar(1L);
        });
        
        verify(pedidoRepository).findById(1L);
        verify(produtoService, never()).atualizarEstoque(anyLong(), anyInt());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void processarPedidosAtrasados_DeveMarcarPedidosComoLATE() {
        // Arrange
        LocalDateTime limitDate = LocalDateTime.now().minusHours(48);
        List<Pedido> pedidosAtrasados = new ArrayList<>();
        pedidosAtrasados.add(pedido);
        
        when(pedidoRepository.findByStatusAndCreatedAtBefore(eq(StatusPedido.CREATED), any(LocalDateTime.class)))
                .thenReturn(pedidosAtrasados);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // Act
        pedidoService.processarPedidosAtrasados();

        // Assert
        assertEquals(StatusPedido.LATE, pedido.getStatus());
        
        verify(pedidoRepository).findByStatusAndCreatedAtBefore(eq(StatusPedido.CREATED), any(LocalDateTime.class));
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void listarPorStatus_DeveRetornarPaginaDePedidos() {
        // Arrange
        Page<Pedido> pedidoPage = new PageImpl<>(Collections.singletonList(pedido));
        when(pedidoRepository.findByStatus(any(StatusPedido.class), any(Pageable.class))).thenReturn(pedidoPage);
        when(pedidoMapper.toResponse(any(Pedido.class))).thenReturn(pedidoResponse);

        // Act
        Page<PedidoResponse> result = pedidoService.listarPorStatus(StatusPedido.CREATED, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(pedidoResponse.getId(), result.getContent().get(0).getId());
        assertEquals(pedidoResponse.getStatus(), result.getContent().get(0).getStatus());
        
        verify(pedidoRepository).findByStatus(StatusPedido.CREATED, Pageable.unpaged());
        verify(pedidoMapper).toResponse(pedido);
    }
}
