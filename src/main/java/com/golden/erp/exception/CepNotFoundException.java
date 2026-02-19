package com.golden.erp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CepNotFoundException extends RuntimeException {

    public CepNotFoundException(String cep) {
        super(String.format("CEP não encontrado: %s", cep));
    }
}
