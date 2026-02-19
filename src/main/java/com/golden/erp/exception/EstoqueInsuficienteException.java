package com.golden.erp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String message) {
        super(message);
    }

    public EstoqueInsuficienteException(Long produtoId, String produtoNome, int quantidadeDisponivel, int quantidadeSolicitada) {
        super(String.format("Estoque insuficiente para o produto %s (ID: %d). Disponível: %d, Solicitado: %d", 
                produtoNome, produtoId, quantidadeDisponivel, quantidadeSolicitada));
    }
}
