package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.response.IngredientResponse;
import com.cdw.cdw.domain.entity.Ingredient;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientResponse toIngredientResponse(Ingredient ingredient);
    List<IngredientResponse> toIngredientResponseList(List<Ingredient> ingredients);
} 