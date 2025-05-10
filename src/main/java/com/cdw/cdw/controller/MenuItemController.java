package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.MenuItemCreateRequest;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.service.MenuItemService;
import com.cdw.cdw.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menuItem")
@Slf4j
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return menuItemService.getMenuItems();
    }

    @PostMapping
    public ApiResponse<MenuItem> addMenuItem(@RequestBody MenuItemCreateRequest menuItem) {
        ApiResponse<MenuItem> response = new ApiResponse<>();
        response.setResult(menuItemService.createMenuItem(menuItem));
        return response;
    }
}