package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.response.IngredientPageResponse;
import com.cdw.cdw.domain.dto.response.IngredientResponse;
import com.cdw.cdw.domain.enums.BaseUnit;
import com.cdw.cdw.service.IngredientService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredient")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IngredientController {
    
    IngredientService ingredientService;

    @GetMapping
    public ApiResponse<IngredientPageResponse> getAllIngredients(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) BaseUnit baseUnit
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }
        
        ApiResponse<IngredientPageResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.searchIngredients(keyword, page, size, sortBy, direction, baseUnit));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredientResponse> getIngredient(@PathVariable Integer id) {
        ApiResponse<IngredientResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.getIngredientById(id));
        return response;
    }

    @GetMapping("/search")
    public ApiResponse<IngredientPageResponse> searchIngredients(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) BaseUnit baseUnit) {

        ApiResponse<IngredientPageResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.searchIngredients(keyword, page, size, sortBy, direction, baseUnit));
        return response;
    }

    @GetMapping("/search/name")
    public ApiResponse<List<IngredientResponse>> searchIngredientsByName(
            @RequestParam String keyword) {
        ApiResponse<List<IngredientResponse>> response = new ApiResponse<>();
        response.setResult(ingredientService.searchIngredientsByName(keyword));
        return response;
    }

    @GetMapping("/all")
    public ApiResponse<List<IngredientResponse>> getAllIngredientsList() {
        ApiResponse<List<IngredientResponse>> response = new ApiResponse<>();
        response.setResult(ingredientService.getAllIngredients());
        return response;
    }
} 