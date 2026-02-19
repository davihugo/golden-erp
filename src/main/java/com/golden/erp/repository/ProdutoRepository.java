package com.golden.erp.repository;

import com.golden.erp.domain.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    
    Optional<Produto> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    Page<Produto> findByAtivoTrue(Pageable pageable);
    
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    Page<Produto> findByAtivoTrueAndNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    @Query("SELECT p FROM Produto p WHERE p.estoque <= p.estoqueMinimo")
    List<Produto> findAllWithLowStock();
}
