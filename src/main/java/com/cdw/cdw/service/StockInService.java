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
import java.time.LocalDate;
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

    public List<StockInBatchResponse> getStockBatchesByIngredientId(Integer ingredientId, String sortParam) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> AppException.notFound("ingredient.not.found"));

        Sort sort = buildSort(sortParam);

        List<StockInBatch> stockBatches = stockInBatchRepository.findByIngredient_Id(ingredientId, sort);

        return stockBatches.stream()
                .map(batch -> StockInBatchResponse.builder()
                        .batchId(batch.getId())
                        .ingredientId(batch.getIngredient().getId())
                        .ingredientName(batch.getIngredient().getName())
                        .quantity(batch.getQuantity())
                        .used(batch.getUsed())
                        .remainingQuantity(batch.getRemainingQuantity())
                        .unitPrice(batch.getUnitPrice())
                        .totalPrice(batch.getTotalPrice())
                        .supplier(batch.getSupplier())
                        .note(batch.getNote())
                        .expiryDate(batch.getExpiryDate())
                        .importedAt(batch.getImportedAt())
                        .build())
                .toList();
    }

    private String mapSortProperty(String sortBy) {
        return switch (sortBy == null ? "" : sortBy.toLowerCase()) {
            case "expirydate" -> "expiryDate";
            case "quantity" -> "quantity";
            case "remainingquantity" -> "remainingQuantity";
            case "used" -> "used";
            case "unitprice" -> "unitPrice";
            case "supplier" -> "supplier";
            case "importedat", "" -> "importedAt";
            default -> throw AppException.badRequest("Invalid sort property");
        };
    }


    @Transactional
    public void deductIngredientsFromNearestExpiry(Integer ingredientId, BigDecimal requiredQuantity) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> AppException.notFound("ingredient.not.found"));

        // Get stock batches with remaining quantity ordered by expiry date (nearest expiry first)
        List<StockInBatch> stockBatches = stockInBatchRepository.findAvailableBatchesByIngredientOrderByExpiryDate(ingredientId);
        
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

            BigDecimal availableInBatch = batch.getRemainingQuantity();
            BigDecimal deductFromBatch = remainingQuantity.compareTo(availableInBatch) > 0 
                ? availableInBatch 
                : remainingQuantity;

            batch.setUsed(batch.getUsed().add(deductFromBatch));
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

    @Transactional
    public void initializeStockData() {
        List<Ingredient> allIngredients = ingredientRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Ingredient ingredient : allIngredients) {
            // Create or get inventory
            Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                    .orElseGet(() -> {
                        Inventory newInventory = new Inventory();
                        newInventory.setIngredient(ingredient);
                        newInventory.setQuantity(BigDecimal.ZERO);
                        return newInventory;
                    });

            // Generate realistic stock data based on ingredient type
            BigDecimal totalQuantity = generateInitialQuantity(ingredient).multiply(BigDecimal.valueOf(10));
            BigDecimal unitPrice = generateUnitPrice(ingredient);
            
            // Create multiple stock batches with different expiry dates
            createStockBatches(ingredient, totalQuantity, unitPrice, now);
            
            // Update inventory
            inventory.setQuantity(totalQuantity);
            inventory.setLastRefilledAt(now);
            inventoryRepository.save(inventory);
        }
    }

    private BigDecimal generateInitialQuantity(Ingredient ingredient) {
        // Generate realistic quantities based on ingredient name or type
        String name = ingredient.getName().toLowerCase();
        
        if (name.contains("flour") || name.contains("rice") || name.contains("pasta")) {
            return BigDecimal.valueOf(10000); // 10kg for bulk ingredients
        } else if (name.contains("sauce") || name.contains("oil") || name.contains("vinegar")) {
            return BigDecimal.valueOf(5000); // 5L for liquids
        } else if (name.contains("cheese") || name.contains("meat") || name.contains("chicken")) {
            return BigDecimal.valueOf(2000); // 2kg for proteins
        } else if (name.contains("vegetable") || name.contains("tomato") || name.contains("onion")) {
            return BigDecimal.valueOf(3000); // 3kg for vegetables
        } else if (name.contains("spice") || name.contains("herb") || name.contains("salt")) {
            return BigDecimal.valueOf(1000); // 1kg for spices
        } else if (name.contains("egg")) {
            return BigDecimal.valueOf(500); // 500 eggs
        } else {
            return BigDecimal.valueOf(1500); // Default 1.5kg
        }
    }

    private BigDecimal generateUnitPrice(Ingredient ingredient) {
        // Generate realistic unit prices
        String name = ingredient.getName().toLowerCase();
        
        if (name.contains("flour") || name.contains("rice")) {
            return BigDecimal.valueOf(15); // 15k VND per kg
        } else if (name.contains("cheese")) {
            return BigDecimal.valueOf(120); // 120k VND per kg
        } else if (name.contains("meat") || name.contains("chicken")) {
            return BigDecimal.valueOf(80); // 80k VND per kg
        } else if (name.contains("vegetable")) {
            return BigDecimal.valueOf(25); // 25k VND per kg
        } else if (name.contains("sauce") || name.contains("oil")) {
            return BigDecimal.valueOf(50); // 50k VND per L
        } else if (name.contains("spice") || name.contains("herb")) {
            return BigDecimal.valueOf(200); // 200k VND per kg
        } else if (name.contains("egg")) {
            return BigDecimal.valueOf(3); // 3k VND per egg
        } else {
            return BigDecimal.valueOf(30); // Default 30k VND per unit
        }
    }

    private void createStockBatches(Ingredient ingredient, BigDecimal totalQuantity, BigDecimal unitPrice, LocalDateTime now) {
        // Create 3-5 stock batches with different expiry dates
        int numberOfBatches = 3 + (int)(Math.random() * 3); // 3-5 batches
        BigDecimal quantityPerBatch = totalQuantity.divide(BigDecimal.valueOf(numberOfBatches), 2, BigDecimal.ROUND_HALF_UP);
        
        String[] suppliers = {"Nhà cung cấp A", "Nhà cung cấp B", "Nhà cung cấp C", "Nhà cung cấp D"};
        
        for (int i = 0; i < numberOfBatches; i++) {
            StockInBatch batch = new StockInBatch();
            batch.setIngredient(ingredient);
            batch.setQuantity(quantityPerBatch);
            batch.setUnitPrice(unitPrice);
            batch.setTotalPrice(quantityPerBatch.multiply(unitPrice));
            batch.setSupplier(suppliers[i % suppliers.length]);
            batch.setNote("Khởi tạo dữ liệu ban đầu");
            
            // Set expiry dates: 30, 60, 90, 120, 150 days from now
            LocalDate expiryDate = now.toLocalDate().plusDays(5 + (i * 5));
            batch.setExpiryDate(expiryDate);
            
            stockInBatchRepository.save(batch);
        }
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

}
