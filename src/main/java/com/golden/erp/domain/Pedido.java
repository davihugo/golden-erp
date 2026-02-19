package com.golden.erp.domain;

import com.golden.erp.domain.enums.StatusPedido;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> itens = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Pedido() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = StatusPedido.CREATED;
        this.subtotal = BigDecimal.ZERO;
        this.descontoTotal = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    public void adicionarItem(PedidoItem item) {
        itens.add(item);
        item.setPedido(this);
        calcularTotais();
    }

    public void removerItem(PedidoItem item) {
        itens.remove(item);
        item.setPedido(null);
        calcularTotais();
    }

    public void calcularTotais() {
        this.subtotal = itens.stream()
                .map(PedidoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.descontoTotal = itens.stream()
                .map(PedidoItem::getDesconto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = this.subtotal.subtract(this.descontoTotal);
    }

    public void pagar() {
        if (this.status != StatusPedido.CREATED) {
            throw new IllegalStateException("Pedido não pode ser pago pois está com status " + this.status);
        }
        this.status = StatusPedido.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status != StatusPedido.CREATED) {
            throw new IllegalStateException("Pedido não pode ser cancelado pois está com status " + this.status);
        }
        this.status = StatusPedido.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void marcarComoAtrasado() {
        if (this.status != StatusPedido.CREATED) {
            return;
        }
        this.status = StatusPedido.LATE;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescontoTotal() {
        return descontoTotal;
    }

    public void setDescontoTotal(BigDecimal descontoTotal) {
        this.descontoTotal = descontoTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<PedidoItem> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItem> itens) {
        this.itens = itens;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente=" + cliente.getId() +
                ", status=" + status +
                ", total=" + total +
                '}';
    }
}
