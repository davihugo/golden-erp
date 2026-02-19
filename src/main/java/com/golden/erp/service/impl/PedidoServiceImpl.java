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
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));
        
        Pedido pedido = pedidoMapper.toEntityWithCliente(request, cliente);
        
        List<PedidoItem> itens = new ArrayList<>();
        for (PedidoItemRequest itemRequest : request.getItens()) {
            Produto produto = produtoRepository.findById(itemRequest.getProdutoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", itemRequest.getProdutoId()));
            
            if (produto.getEstoque() < itemRequest.getQuantidade()) {
                throw new EstoqueInsuficienteException(
                        produto.getId(), 
                        produto.getNome(), 
                        produto.getEstoque(), 
                        itemRequest.getQuantidade());
            }
            
            PedidoItem item = pedidoItemMapper.toEntityWithProduto(itemRequest, produto);
            item.setPedido(pedido);
            itens.add(item);
            
            produtoService.atualizarEstoque(produto.getId(), -itemRequest.getQuantidade());
        }
        
        pedido.setItens(itens);
        pedido.calcularTotais();
        
        pedido = pedidoRepository.save(pedido);
        
        logger.info("Pedido criado com sucesso: {}", pedido.getId());
        return pedidoMapper.toResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        
        pedido.getCliente().getNome();
        
        return pedidoMapper.toResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(Pageable pageable) {
        Page<Pedido> pedidos = pedidoRepository.findAll(pageable);
        pedidos.forEach(p -> p.getCliente().getNome());
        return pedidos.map(pedidoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPorStatus(StatusPedido status, Pageable pageable) {
        Page<Pedido> pedidos = pedidoRepository.findByStatus(status, pageable);
        pedidos.forEach(p -> p.getCliente().getNome());
        return pedidos.map(pedidoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente", "id", clienteId);
        }
        
        Page<Pedido> pedidos = pedidoRepository.findByClienteId(clienteId, pageable);
        pedidos.forEach(p -> p.getCliente().getNome());
        return pedidos.map(pedidoMapper::toResponse);
    }

    @Override
    @Transactional
    public PedidoResponse pagar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        
        pedido.getCliente().getNome();
        
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
        
        pedido.getCliente().getNome();
        
        try {
            if (pedido.getStatus() == StatusPedido.CREATED || pedido.getStatus() == StatusPedido.LATE) {
                for (PedidoItem item : pedido.getItens()) {
                    item.getProduto().getNome();
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
