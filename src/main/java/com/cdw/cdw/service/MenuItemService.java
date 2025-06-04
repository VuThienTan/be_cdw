package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.response.InfoMenuItemResponse;
import com.cdw.cdw.domain.dto.response.MenuItemIngredientResponse;
import com.cdw.cdw.domain.dto.response.MenuItemPageResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
import com.cdw.cdw.domain.entity.Category;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.MenuItemIngredient;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.mapper.MenuItemIngredientMapper;
import com.cdw.cdw.mapper.MenuItemMapper;
import com.cdw.cdw.repository.*;
import com.cdw.cdw.repository.spec.MenuItemSpecifications;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    IngredientRepository ingredientRepository;
    MenuItemIngredientRepositoy itemsIngredientRepository;
    MenuItemMapper menuItemMapper;
    MenuItemIngredientMapper menuItemIngredientMapper;

    public MenuItemPageResponse getMenuItems(String keyword, int page, int size, String sortBy, String direction,
                                             Integer ration, Double priceFrom, Double priceTo) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<MenuItem> spec = Specification
                .where(MenuItemSpecifications.hasKeyword(keyword))
                .and(MenuItemSpecifications.hasRation(ration))
                .and(MenuItemSpecifications.hasPriceBetween(priceFrom, priceTo));

        Page<MenuItem> data = menuItemRepository.findAll(spec, pageRequest);

        return MenuItemPageResponse.builder()
                .menuItems(data.getContent().stream().map(menuItemMapper::toMenuItemResponse).toList())
                .currentPage(data.getNumber())
                .totalPages(data.getTotalPages())
                .totalItems(data.getTotalElements())
                .pageSize(data.getSize())
                .build();
    }

    public List<MenuItemResponse> randomMenuItem() {
        return menuItemRepository.getRandomMenuItem().stream().map(menuItemMapper::toMenuItemResponse).toList();
    }

    public InfoMenuItemResponse getMenuItemById(Long menuItemId) {
        // 1. Lấy menu item
        var menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> AppException.notFound("menu.item.not.found"));

        var infoMenuItemResponse = menuItemMapper.toInfoMenuItemResponse(menuItem);

        List<MenuItemIngredient> menuItemIngredients = itemsIngredientRepository.findByMenuItem_Id(menuItemId);
        List<MenuItemIngredientResponse> ingredientDtos = menuItemIngredients.stream()
                .map(menuItemIngredient -> {
                    var ingredient = ingredientRepository.findById(menuItemIngredient.getIngredient().getId())
                            .orElseThrow(() -> AppException.notFound("ingredient.not.found"));

                    return menuItemIngredientMapper.toMenuItemIngredient(menuItemIngredient, ingredient);
                })
                .toList();

        infoMenuItemResponse.setIngredients(ingredientDtos);

        return infoMenuItemResponse;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public MenuItem createMenuItem(MenuItemCreateRequest request) {
        // Kiểm tra trùng tên
        if (!menuItemRepository.findByName(request.getName()).isEmpty()) {
            throw AppException.badRequest("menu.item.exist");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> AppException.notFound("category.not.found"));

        MenuItem menuItem = menuItemMapper.toMenuItem(request);
        menuItem.setCategory(category);

        try {
            return menuItemRepository.save(menuItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save MenuItem", e);
        }
    }


//    tìm kiếm
    public MenuItemPageResponse searchMenuItemsByName(String keyword, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<MenuItem> data = menuItemRepository.findByNameContainingIgnoreCase(keyword, pageRequest);

        return MenuItemPageResponse.builder()
                .menuItems(data.getContent().stream().map(menuItemMapper::toMenuItemResponse).toList())
                .currentPage(data.getNumber())
                .totalPages(data.getTotalPages())
                .totalItems(data.getTotalElements())
                .pageSize(data.getSize())
                .build();
    }

}
