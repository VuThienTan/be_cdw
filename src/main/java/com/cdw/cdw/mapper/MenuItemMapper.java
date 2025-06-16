package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.request.MenuItemUpdateRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.InfoMenuItemResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.awt.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {
    InfoMenuItemResponse toInfoMenuItemResponse(MenuItem menuItem);
    MenuItem toMenuItem(MenuItemCreateRequest menuItemCreateRequest);
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "menuItemIngredients", ignore = true)
    void updateMenuItemFromRequest(MenuItemUpdateRequest request, @MappingTarget MenuItem menuItem);
}
