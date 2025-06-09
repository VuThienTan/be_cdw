package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.StockInRequest;
import com.cdw.cdw.domain.dto.response.InventoryResponse;
import com.cdw.cdw.domain.dto.response.PageResponse;
import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.entity.Inventory;
import com.cdw.cdw.domain.entity.StockInBatch;
import com.cdw.cdw.repository.IngredientRepository;
import com.cdw.cdw.repository.InventoryRepository;
import com.cdw.cdw.repository.StockInBatchRepository;
import com.cdw.cdw.repository.spec.InventorySpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockInService {

    private final IngredientRepository ingredientRepository;
    private final InventoryRepository inventoryRepository;
    private final StockInBatchRepository stockInBatchRepository;

    public PageResponse<InventoryResponse> getAllInventories(
            String keyword,
            int page,
            int size,
            String sortParams
    ) {
        Sort sort = buildSort(sortParams);

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Inventory> spec = Specification.where(InventorySpecifications.hasKeyword(keyword));

        Page<Inventory> inventories = inventoryRepository.findAll(spec, pageRequest);

        List<InventoryResponse> mappedContent = inventories.stream()
                .map(inventory -> InventoryResponse.builder()
                        .ingredientId(inventory.getIngredient().getId())
                        .ingredientName(inventory.getIngredient().getName())
                        .baseUnit(inventory.getIngredient().getBaseUnit().name())
                        .quantity(inventory.getQuantity())
                        .lastUpdatedAt(inventory.getLastRefilledAt())
                        .build())
                .toList();

        return PageResponse.<InventoryResponse>builder()
                .content(mappedContent)
                .currentPage(inventories.getNumber())
                .totalPages(inventories.getTotalPages())
                .totalItems(inventories.getTotalElements())
                .pageSize(inventories.getSize())
                .build();
    }

    private Sort buildSort(String sortParams) {
        if (sortParams == null || sortParams.trim().isEmpty()) {
            return Sort.unsorted();
        }

        String trimmedParam = sortParams.trim();
        
        if (trimmedParam.contains(",")) {
            String[] parts = trimmedParam.split(",");
            
            if (parts.length >= 2) {
                String field = parts[0].trim();
                String direction = parts[1].trim();
                
                if (!field.isEmpty()) {
                    return Sort.by(getSortDirection(direction), field);
                }
            } else if (parts.length == 1 && !parts[0].trim().isEmpty()) {
                String field = parts[0].trim();
                return Sort.by(Sort.Direction.ASC, field);
            }
        } else {
            return Sort.by(Sort.Direction.ASC, trimmedParam);
        }

        return Sort.unsorted();
    }

    private Sort.Direction getSortDirection(String direction) {
        if (direction == null || direction.trim().isEmpty()) {
            return Sort.Direction.ASC;
        }
        String trimmedDirection = direction.trim().toLowerCase();
        if ("desc".equals(trimmedDirection)) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    @Transactional
    public void importStock(StockInRequest dto) {
        // 1. Lấy nguyên liệu
        Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                .orElseThrow(() -> new RuntimeException("Nguyên liệu không tồn tại"));

        // 2. Tính tổng tiền
        BigDecimal totalPrice = dto.getUnitPrice().multiply(dto.getQuantity());

        // 3. Tạo StockInBatch
        StockInBatch batch = new StockInBatch();
        batch.setIngredient(ingredient);
        batch.setQuantity(dto.getQuantity());
        batch.setUnitPrice(dto.getUnitPrice());
        batch.setTotalPrice(totalPrice);
        batch.setSupplier(dto.getSupplier());
        batch.setNote(dto.getNote());
        batch.setExpiryDate(dto.getExpiryDate());

        stockInBatchRepository.save(batch);

        // 4. Cập nhật hoặc tạo mới Inventory
        Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setIngredient(ingredient);
                    newInventory.setQuantity(BigDecimal.ZERO);
                    return newInventory;
                });

        BigDecimal newQuantity = inventory.getQuantity().add(dto.getQuantity());
        inventory.setQuantity(newQuantity);
        inventory.setLastRefilledAt(LocalDateTime.now());

        inventoryRepository.save(inventory);
    }

    public void updateAllInventoryToTenThousand() {
        List<Inventory> allInventories = inventoryRepository.findAll();

        for (Inventory inventory : allInventories) {
            inventory.setQuantity(BigDecimal.valueOf(10_000));
            inventory.setLastRefilledAt(LocalDateTime.now());
        }

        inventoryRepository.saveAll(allInventories);
    }

    @Transactional
    public void updateAllToTenThousandIncludingMissing() {
        List<Ingredient> allIngredients = ingredientRepository.findAll();

        for (Ingredient ingredient : allIngredients) {
            Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setIngredient(ingredient);
                        newInventory.setQuantity(BigDecimal.ZERO); // sẽ set lại ngay sau
                        return newInventory;
                    });

            inventory.setQuantity(BigDecimal.valueOf(10_000));
            inventory.setLastRefilledAt(LocalDateTime.now());

            inventoryRepository.save(inventory);
        }
    }

}
