package com.golden.erp.mapper;

import com.golden.erp.domain.Produto;
import com.golden.erp.dto.request.ProdutoRequest;
import com.golden.erp.dto.response.ProdutoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProdutoMapper {

    ProdutoResponse toResponse(Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Produto toEntity(ProdutoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntityFromRequest(ProdutoRequest request, @MappingTarget Produto produto);
}
