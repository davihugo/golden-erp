package com.golden.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PedidoRequest {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;

    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    @Valid
    private List<PedidoItemRequest> itens;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<PedidoItemRequest> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItemRequest> itens) {
        this.itens = itens;
    }
}
