package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.InfoMenuItemResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.User;
import org.mapstruct.Mapper;

import java.awt.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {
    InfoMenuItemResponse toInfoMenuItemResponse(MenuItem menuItem);
    MenuItem toMenuItem(MenuItemCreateRequest menuItemCreateRequest);
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);
}
