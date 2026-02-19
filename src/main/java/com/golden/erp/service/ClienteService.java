package com.golden.erp.service;

import com.golden.erp.dto.request.ClienteRequest;
import com.golden.erp.dto.response.ClienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {
    
    ClienteResponse criar(ClienteRequest request);
    
    ClienteResponse buscarPorId(Long id);
    
    ClienteResponse atualizar(Long id, ClienteRequest request);
    
    void excluir(Long id);
    
    Page<ClienteResponse> listar(Pageable pageable);
    
    Page<ClienteResponse> buscarPorNome(String nome, Pageable pageable);
    
    Page<ClienteResponse> buscarPorEmail(String email, Pageable pageable);
}
