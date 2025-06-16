package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.dto.request.MenuItemUpdateRequest;
import com.cdw.cdw.domain.dto.response.InfoMenuItemResponse;
import com.cdw.cdw.domain.dto.response.MenuItemPageResponse;
import com.cdw.cdw.domain.dto.response.MenuItemResponse;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.service.MenuItemService;
import com.cdw.cdw.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping("/menuItem")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class MenuItemController {
    MenuItemService menuItemService;

    @GetMapping
    public ApiResponse<MenuItemPageResponse> getAllMenuItems(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Integer ration,
            @RequestParam(required = false) Double priceFrom,
            @RequestParam(required = false) Double priceTo
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        ApiResponse<MenuItemPageResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.getMenuItems(keyword, page, size, sortBy, direction, ration, priceFrom, priceTo));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<InfoMenuItemResponse> getMenuItem(@PathVariable Long id) {
        ApiResponse<InfoMenuItemResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.getMenuItemById(id));
        return response;
    }

    @GetMapping("/random")
    public ApiResponse<List<MenuItemResponse>> getRandomMenuItem() {
        ApiResponse<List<MenuItemResponse>> response = new ApiResponse<>();
        response.setResult(menuItemService.randomMenuItem());
        return response;
    }

    @PostMapping
    public ApiResponse<MenuItem> addMenuItem(@RequestBody MenuItemCreateRequest menuItem) {
        ApiResponse<MenuItem> response = new ApiResponse<>();
        response.setResult(menuItemService.createMenuItem(menuItem));
        return response;
    }

    @PutMapping
    public ApiResponse<MenuItemResponse> updateMenuItem(@RequestBody MenuItemUpdateRequest menuItem) {
        ApiResponse<MenuItemResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.updateMenuItem(menuItem));
        return response;
    }
    
    //tìm kiếm
    @GetMapping("/search")
    public ApiResponse<MenuItemPageResponse> searchMenuItems(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Integer ration,
            @RequestParam(required = false) Double priceFrom,
            @RequestParam(required = false) Double priceTo) {

        ApiResponse<MenuItemPageResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.getMenuItems(keyword, page, size, sortBy, direction, ration, priceFrom, priceTo));
        return response;
    }

    @GetMapping("/check/{id}")
    public ApiResponse<Integer> checkAvailableQuantityForProduct(@PathVariable Long id) {
        return ApiResponse.<Integer>builder().result(menuItemService.checkAvailableQuantityForProduct(id)).build();
    }

}