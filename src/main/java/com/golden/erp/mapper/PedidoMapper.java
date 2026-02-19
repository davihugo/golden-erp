package com.golden.erp.mapper;

import com.golden.erp.domain.Cliente;
import com.golden.erp.domain.Pedido;
import com.golden.erp.dto.request.PedidoRequest;
import com.golden.erp.dto.response.PedidoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", 
        uses = {PedidoItemMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PedidoMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNome", source = "cliente.nome")
    PedidoResponse toResponse(Pedido pedido);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "itens", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "descontoTotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Pedido toEntity(PedidoRequest request);

    default Pedido toEntityWithCliente(PedidoRequest request, Cliente cliente) {
        Pedido pedido = toEntity(request);
        pedido.setCliente(cliente);
        return pedido;
    }
}
