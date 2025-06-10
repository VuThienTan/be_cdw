package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.StockInRequest;
import com.cdw.cdw.domain.dto.response.InventoryResponse;
import com.cdw.cdw.domain.dto.response.PageResponse;
import com.cdw.cdw.domain.dto.response.StockInBatchResponse;
import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.entity.Inventory;
import com.cdw.cdw.domain.entity.StockInBatch;
import com.cdw.cdw.exception.AppException;
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

    public List<StockInBatchResponse> getStockBatchesByIngredientId(Integer ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> AppException.notFound("ingredient.not.found"));

        List<StockInBatch> stockBatches = stockInBatchRepository.findByIngredient_IdOrderByImportedAtDesc(ingredientId);

        return stockBatches.stream()
                .map(batch -> StockInBatchResponse.builder()
                        .batchId(batch.getId())
                        .ingredientId(batch.getIngredient().getId())
                        .ingredientName(batch.getIngredient().getName())
                        .quantity(batch.getQuantity())
                        .unitPrice(batch.getUnitPrice())
                        .totalPrice(batch.getTotalPrice())
                        .supplier(batch.getSupplier())
                        .note(batch.getNote())
                        .expiryDate(batch.getExpiryDate())
                        .importedAt(batch.getImportedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deductIngredientsFromNearestExpiry(Integer ingredientId, BigDecimal requiredQuantity) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> AppException.notFound("ingredient.not.found"));

        // Get stock batches ordered by expiry date (nearest expiry first)
        List<StockInBatch> stockBatches = stockInBatchRepository.findByIngredient_IdOrderByExpiryDateAsc(ingredientId);
        
        if (stockBatches.isEmpty()) {
            throw AppException.badRequest("No stock available for ingredient: " + ingredient.getName());
        }

        BigDecimal remainingQuantity = requiredQuantity;
        List<StockInBatch> batchesToUpdate = new ArrayList<>();

        // Deduct from batches starting with the nearest expiry date
        for (StockInBatch batch : stockBatches) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal availableInBatch = batch.getQuantity();
            BigDecimal deductFromBatch = remainingQuantity.compareTo(availableInBatch) > 0 
                ? availableInBatch 
                : remainingQuantity;

            batch.setQuantity(availableInBatch.subtract(deductFromBatch));
            batchesToUpdate.add(batch);
            remainingQuantity = remainingQuantity.subtract(deductFromBatch);
        }

        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            throw AppException.badRequest("Insufficient stock for ingredient: " + ingredient.getName() + 
                ". Required: " + requiredQuantity + ", Available: " + 
                (requiredQuantity.subtract(remainingQuantity)));
        }

        // Save updated batches
        stockInBatchRepository.saveAll(batchesToUpdate);

        // Update inventory
        Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                .orElseThrow(() -> AppException.notFound("inventory.not.found"));

        inventory.setQuantity(inventory.getQuantity().subtract(requiredQuantity));
        inventory.setLastRefilledAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    public InventoryResponse getInventoryByIngredientId(Integer ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> AppException.notFound("ingradient.not.found"));

        Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                .orElseThrow(() -> AppException.notFound("inventory.not.found"));

        return InventoryResponse.builder()
                .ingredientId(inventory.getIngredient().getId())
                .ingredientName(inventory.getIngredient().getName())
                .baseUnit(inventory.getIngredient().getBaseUnit().name())
                .quantity(inventory.getQuantity())
                .lastUpdatedAt(inventory.getLastRefilledAt())
                .build();
    }

}
