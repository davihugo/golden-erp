package com.golden.erp.service.impl;

import com.golden.erp.domain.Cliente;
import com.golden.erp.domain.Pedido;
import com.golden.erp.domain.PedidoItem;
import com.golden.erp.domain.Produto;
import com.golden.erp.domain.enums.StatusPedido;
import com.golden.erp.dto.request.PedidoItemRequest;
import com.golden.erp.dto.request.PedidoRequest;
import com.golden.erp.dto.response.PedidoResponse;
import com.golden.erp.exception.EstoqueInsuficienteException;
import com.golden.erp.exception.ResourceNotFoundException;
import com.golden.erp.mapper.PedidoItemMapper;
import com.golden.erp.mapper.PedidoMapper;
import com.golden.erp.repository.ClienteRepository;
import com.golden.erp.repository.PedidoRepository;
import com.golden.erp.repository.ProdutoRepository;
import com.golden.erp.service.PedidoService;
import com.golden.erp.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);
    
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;
    private final PedidoMapper pedidoMapper;
    private final PedidoItemMapper pedidoItemMapper;

    public PedidoServiceImpl(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            ProdutoService produtoService,
            PedidoMapper pedidoMapper,
            PedidoItemMapper pedidoItemMapper) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.produtoService = produtoService;
        this.pedidoMapper = pedidoMapper;
        this.pedidoItemMapper = pedidoItemMapper;
    }

    @Override
    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));
        
        // Criar pedido
        Pedido pedido = pedidoMapper.toEntityWithCliente(request, cliente);
        
        // Processar itens do pedido
        List<PedidoItem> itens = new ArrayList<>();
        for (PedidoItemRequest itemRequest : request.getItens()) {
            // Buscar produto
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", itemRequest.getProdutoId()));
            
            // Verificar estoque
            if (produto.getEstoque() < itemRequest.getQuantidade()) {
                throw new EstoqueInsuficienteException(
                        produto.getId(), 
                        produto.getNome(), 
                        produto.getEstoque(), 
                        itemRequest.getQuantidade());
            }
            
            // Criar item do pedido
            PedidoItem item = pedidoItemMapper.toEntityWithProduto(itemRequest, produto);
            item.setPedido(pedido);
            itens.add(item);
            
            // Baixar estoque
            produtoService.atualizarEstoque(produto.getId(), -itemRequest.getQuantidade());
        }
        
        pedido.setItens(itens);
        pedido.calcularTotais();
        
        // Salvar pedido
        pedido = pedidoRepository.save(pedido);
        
        logger.info("Pedido criado com sucesso: {}", pedido.getId());
        return pedidoMapper.toResponse(pedido);
    }

    @Override
    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        
        return pedidoMapper.toResponse(pedido);
    }

    @Override
    public Page<PedidoResponse> listar(Pageable pageable) {
        return pedidoRepository.findAll(pageable)
                .map(pedidoMapper::toResponse);
    }

    @Override
    public Page<PedidoResponse> listarPorStatus(StatusPedido status, Pageable pageable) {
        return pedidoRepository.findByStatus(status, pageable)
                .map(pedidoMapper::toResponse);
    }

    @Override
    public Page<PedidoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        // Verificar se o cliente existe
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente", "id", clienteId);
        }
        
        return pedidoRepository.findByClienteId(clienteId, pageable)
                .map(pedidoMapper::toResponse);
    }

    @Override
    @Transactional
    public PedidoResponse pagar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        
        try {
            pedido.pagar();
            pedido = pedidoRepository.save(pedido);
            logger.info("Pedido pago com sucesso: {}", pedido.getId());
            return pedidoMapper.toResponse(pedido);
        } catch (IllegalStateException e) {
            logger.error("Erro ao pagar pedido: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        
        try {
            // Se o pedido não foi pago, devolver o estoque
            if (pedido.getStatus() == StatusPedido.CREATED || pedido.getStatus() == StatusPedido.LATE) {
                for (PedidoItem item : pedido.getItens()) {
                    produtoService.atualizarEstoque(item.getProduto().getId(), item.getQuantidade());
                }
            }
            
            pedido.cancelar();
            pedido = pedidoRepository.save(pedido);
            logger.info("Pedido cancelado com sucesso: {}", pedido.getId());
            return pedidoMapper.toResponse(pedido);
        } catch (IllegalStateException e) {
            logger.error("Erro ao cancelar pedido: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void processarPedidosAtrasados() {
        // Buscar pedidos CREATED com mais de 48h
        LocalDateTime limitDate = LocalDateTime.now().minusHours(48);
        List<Pedido> pedidosAtrasados = pedidoRepository.findByStatusAndCreatedAtBefore(StatusPedido.CREATED, limitDate);
        
        if (!pedidosAtrasados.isEmpty()) {
            logger.info("Processando {} pedidos atrasados", pedidosAtrasados.size());
            
            for (Pedido pedido : pedidosAtrasados) {
                pedido.marcarComoAtrasado();
                pedidoRepository.save(pedido);
                logger.info("Pedido {} marcado como LATE", pedido.getId());
            }
        }
    }
}
