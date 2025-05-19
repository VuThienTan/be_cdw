package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.response.MenuItemIngredientResponse;
import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.entity.MenuItemIngredient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuItemIngredientMapper {
    MenuItemIngredientResponse toMenuItemIngredient(MenuItemIngredient menuItemIngredient, Ingredient ingredient);
}
