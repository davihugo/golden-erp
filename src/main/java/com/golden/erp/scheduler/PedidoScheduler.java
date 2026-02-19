package com.golden.erp.scheduler;

import com.golden.erp.service.PedidoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PedidoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PedidoScheduler.class);
    
    private final PedidoService pedidoService;

    public PedidoScheduler(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Tarefa agendada para verificar pedidos atrasados a cada 1 hora
     * Pedidos CREATED com mais de 48h são marcados como LATE
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em milissegundos
    public void verificarPedidosAtrasados() {
        logger.info("Iniciando verificação de pedidos atrasados");
        pedidoService.processarPedidosAtrasados();
        logger.info("Verificação de pedidos atrasados concluída");
    }
}
