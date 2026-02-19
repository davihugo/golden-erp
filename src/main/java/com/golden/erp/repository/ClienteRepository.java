package com.golden.erp.repository;

import com.golden.erp.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByCpf(String cpf);
    
    boolean existsByEmail(String email);
    
    boolean existsByCpf(String cpf);
    
    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    Page<Cliente> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
