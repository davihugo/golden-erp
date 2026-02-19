package com.golden.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PedidoItemRequest {

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto não pode ser negativo")
    private BigDecimal descontoOpcional;

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getDescontoOpcional() {
        return descontoOpcional != null ? descontoOpcional : BigDecimal.ZERO;
    }

    public void setDescontoOpcional(BigDecimal descontoOpcional) {
        this.descontoOpcional = descontoOpcional;
    }
}
