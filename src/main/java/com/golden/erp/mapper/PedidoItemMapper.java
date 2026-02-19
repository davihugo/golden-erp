package com.golden.erp.mapper;

import com.golden.erp.domain.PedidoItem;
import com.golden.erp.domain.Produto;
import com.golden.erp.dto.request.PedidoItemRequest;
import com.golden.erp.dto.response.PedidoItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PedidoItemMapper {

    @Mapping(target = "produtoId", source = "produto.id")
    @Mapping(target = "produtoNome", source = "produto.nome")
    @Mapping(target = "produtoSku", source = "produto.sku")
    PedidoItemResponse toResponse(PedidoItem pedidoItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pedido", ignore = true)
    @Mapping(target = "produto", ignore = true)
    @Mapping(target = "precoUnitario", ignore = true)
    @Mapping(target = "desconto", source = "descontoOpcional")
    @Mapping(target = "subtotal", ignore = true)
    PedidoItem toEntity(PedidoItemRequest request);

    default PedidoItem toEntityWithProduto(PedidoItemRequest request, Produto produto) {
        PedidoItem pedidoItem = toEntity(request);
        pedidoItem.setProduto(produto);
        pedidoItem.setPrecoUnitario(produto.getPrecoBruto());
        pedidoItem.calcularSubtotal();
        return pedidoItem;
    }
}
