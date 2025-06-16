package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.response.IngredientPageResponse;
import com.cdw.cdw.domain.dto.response.IngredientResponse;
import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.enums.BaseUnit;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.mapper.IngredientMapper;
import com.cdw.cdw.repository.IngredientRepository;
import com.cdw.cdw.repository.spec.IngredientSpecifications;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IngredientService {
    
    IngredientRepository ingredientRepository;
    IngredientMapper ingredientMapper;

    public IngredientPageResponse searchIngredients(String keyword, int page, int size, String sortBy, String direction, BaseUnit baseUnit) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Ingredient> spec = Specification
                .where(IngredientSpecifications.hasKeyword(keyword))
                .and(IngredientSpecifications.hasBaseUnit(baseUnit));

        Page<Ingredient> data = ingredientRepository.findAll(spec, pageRequest);

        return IngredientPageResponse.builder()
                .ingredients(data.getContent().stream()
                        .map(ingredientMapper::toIngredientResponse)
                        .collect(Collectors.toList()))
                .currentPage(data.getNumber())
                .totalPages(data.getTotalPages())
                .totalItems(data.getTotalElements())
                .pageSize(data.getSize())
                .build();
    }

    public IngredientResponse getIngredientById(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("ingredient.not.found"));
        return ingredientMapper.toIngredientResponse(ingredient);
    }

    public List<IngredientResponse> getAllIngredients() {
        return ingredientMapper.toIngredientResponseList(ingredientRepository.findAll());
    }

    public List<IngredientResponse> searchIngredientsByName(String keyword) {
        return ingredientMapper.toIngredientResponseList(ingredientRepository.findByNameContainingIgnoreCase(keyword));
    }
} 