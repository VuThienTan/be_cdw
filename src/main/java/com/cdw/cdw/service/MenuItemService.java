package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.entity.Category;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.exception.ErrorCode;
import com.cdw.cdw.mapper.MenuItemMapper;
import com.cdw.cdw.mapper.UserMapper;
import com.cdw.cdw.repository.CategoryRepository;
import com.cdw.cdw.repository.MenuItemRepository;
import com.cdw.cdw.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MenuItemService {
    @Autowired
    MenuItemRepository menuItemRepository;
    CategoryRepository categoryRepository;
    MenuItemMapper menuItemMapper;

    public List<MenuItem> getMenuItems(){
        return menuItemRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public MenuItem createMenuItem( MenuItemCreateRequest request) {
        // Kiểm tra trùng tên
        if (!menuItemRepository.findByName(request.getName()).isEmpty()) {
            throw new AppException(ErrorCode.MENU_ITEM_ALREADY_EXISTS);
        }

        // Lấy Category từ ID
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Mapping DTO -> Entity
        MenuItem menuItem = menuItemMapper.toMenuItem(request);
        menuItem.setCategory(category);

        try {
            return menuItemRepository.save(menuItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save MenuItem", e);
        }
    }





}
