package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.response.MenuItemPageResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MenuItemService {
    @Autowired
    MenuItemRepository menuItemRepository;
    CategoryRepository categoryRepository;
    MenuItemMapper menuItemMapper;

    public MenuItemPageResponse getMenuItems(int page, int size, String sortBy, String direction,
                                             Integer ration, Double priceFrom, Double priceTo) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<MenuItem> data;

        if (ration != null && priceFrom != null && priceTo != null) {
            data = menuItemRepository.findByRationAndPriceBetween(ration, priceFrom, priceTo, pageRequest);
        }
        else if (ration != null) {
            data = menuItemRepository.findByRation(ration, pageRequest);
        }
        else if (priceFrom != null && priceTo != null) {
            data = menuItemRepository.findByPriceBetween(priceFrom, priceTo, pageRequest);
        }
        else {
            data = menuItemRepository.findAll(pageRequest);
        }

        return MenuItemPageResponse.builder()
                .menuItems(data.getContent().stream().map(menuItemMapper::toMenuItemResponse).toList())
                .currentPage(data.getNumber())
                .totalPages(data.getTotalPages())
                .totalItems(data.getTotalElements())
                .pageSize(data.getSize())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public MenuItem createMenuItem(MenuItemCreateRequest request) {
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
