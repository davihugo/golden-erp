package com.golden.erp.scheduler;

import com.golden.erp.dto.response.ProdutoResponse;
import com.golden.erp.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProdutoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoScheduler.class);
    
    private final ProdutoService produtoService;

    public ProdutoScheduler(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Tarefa agendada para verificar produtos com estoque abaixo do mínimo
     * Executa todos os dias às 03:00
     */
    @Scheduled(cron = "0 0 3 * * ?") // Todos os dias às 03:00
    public void verificarProdutosComEstoqueBaixo() {
        logger.info("Iniciando verificação de produtos com estoque abaixo do mínimo");
        
        List<ProdutoResponse> produtosEstoqueBaixo = produtoService.listarProdutosComEstoqueBaixo();
        
        if (produtosEstoqueBaixo.isEmpty()) {
            logger.info("Nenhum produto com estoque abaixo do mínimo encontrado");
        } else {
            logger.warn("Encontrados {} produtos com estoque abaixo do mínimo:", produtosEstoqueBaixo.size());
            
            for (ProdutoResponse produto : produtosEstoqueBaixo) {
                logger.warn("Produto com estoque baixo: ID={}, SKU={}, Nome={}, Estoque={}, EstoqueMinimo={}",
                        produto.getId(), produto.getSku(), produto.getNome(), produto.getEstoque(), produto.getEstoqueMinimo());
            }
        }
        
        logger.info("Verificação de produtos com estoque abaixo do mínimo concluída");
    }
}
