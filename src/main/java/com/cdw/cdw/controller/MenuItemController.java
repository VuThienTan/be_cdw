package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
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
    public ApiResponse<MenuItemPageResponse> getAllMenuItems(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "createdAt") String sortBy,
                                                             @RequestParam(defaultValue = "desc") String direction,
                                                             @RequestParam(required = false) Integer ration,
                                                             @RequestParam(required = false) Double priceForm,
                                                             @RequestParam(required = false) Double priceTo

    ) {
        ApiResponse<MenuItemPageResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.getMenuItems(page, size, sortBy, direction, ration, priceForm, priceTo));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<InfoMenuItemResponse> getMenuItem(@PathVariable Long id) {
        ApiResponse<InfoMenuItemResponse> response = new ApiResponse<>();
        response.setResult(menuItemService.getMenuItemById(id));
        return response;
    }

    @GetMapping("/random")
    public ApiResponse<List<MenuItemResponse>> getRandomMenuItem(){
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
}