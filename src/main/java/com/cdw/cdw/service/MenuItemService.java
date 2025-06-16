package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.request.MenuItemUpdateRequest;
import com.cdw.cdw.domain.dto.response.InfoMenuItemResponse;
import com.cdw.cdw.domain.dto.response.MenuItemIngredientResponse;
import com.cdw.cdw.domain.dto.response.MenuItemPageResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
import com.cdw.cdw.domain.entity.*;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    MenuItemIngredientRepository itemsIngredientRepository;
    InventoryRepository inventoryRepository;

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
                .menuItems(data.getContent().stream()
                        .map(menuItem -> {
                            MenuItemResponse res = menuItemMapper.toMenuItemResponse(menuItem);
                            res.setInStock(checkAvailableQuantityForProduct(menuItem.getId())); // 👈 thêm dòng này
                            return res;
                        })
                        .toList())
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

        infoMenuItemResponse.setInStock(checkAvailableQuantityForProduct(menuItemId));
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

    @Transactional
    public MenuItemResponse updateMenuItem(MenuItemUpdateRequest request) {
        MenuItem menuItem = menuItemRepository.findById(request.getId())
                .orElseThrow(() -> AppException.notFound("menu.item.not.found"));

        menuItemMapper.updateMenuItemFromRequest(request, menuItem);

        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            itemsIngredientRepository.deleteByMenuItem(menuItem);

            for (var ingredientRequest : request.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findByName(ingredientRequest.getName())
                        .orElseGet(() -> {
                            Ingredient newIngredient = new Ingredient();
                            newIngredient.setName(ingredientRequest.getName());
                            newIngredient.setBaseUnit(ingredientRequest.getBaseUnit());
                            newIngredient.setDescription("Auto-created ingredient");
                            return ingredientRepository.save(newIngredient);
                        });

                MenuItemIngredient menuItemIngredient = new MenuItemIngredient();
                menuItemIngredient.setMenuItem(menuItem);
                menuItemIngredient.setIngredient(ingredient);
                menuItemIngredient.setQuantityRequired(ingredientRequest.getQuantityRequired());
                itemsIngredientRepository.save(menuItemIngredient);
            }
        }

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return menuItemMapper.toMenuItemResponse(savedMenuItem);
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

    public Integer checkAvailableQuantityForProduct(Long menuItemId) {
        List<MenuItemIngredient> ingredients = itemsIngredientRepository.findByMenuItem_Id(menuItemId);

        int maxProduct = Integer.MAX_VALUE;

        for (MenuItemIngredient mi : ingredients) {
            Ingredient ingredient = mi.getIngredient();
            BigDecimal requiredQty = mi.getQuantityRequired();

            Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                    .orElseThrow(() -> new RuntimeException("Không có tồn kho cho nguyên liệu " + ingredient.getName()));

            BigDecimal availableQty = inventory.getQuantity();

            int possibleProductCount = availableQty.divide(requiredQty, RoundingMode.DOWN).intValue();

            if (possibleProductCount < maxProduct) {
                maxProduct = possibleProductCount;
            }
        }

        return maxProduct == Integer.MAX_VALUE ? 0 : maxProduct;
    }

}
