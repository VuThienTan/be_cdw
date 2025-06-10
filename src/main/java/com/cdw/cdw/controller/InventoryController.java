package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.StockInRequest;
import com.cdw.cdw.domain.dto.response.InventoryResponse;
import com.cdw.cdw.domain.dto.response.PageResponse;
import com.cdw.cdw.domain.dto.response.StockInBatchResponse;
import com.cdw.cdw.service.StockInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final StockInService stockInService;

    @GetMapping
    public ApiResponse<PageResponse<InventoryResponse>> getAllInventories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        return ApiResponse.<PageResponse<InventoryResponse>>builder()
                .result(stockInService.getAllInventories(keyword, page, size, sort))
                .build();
    }

    @GetMapping("/{ingredientId}")
    public ApiResponse<InventoryResponse> getInventoryByIngredientId(@PathVariable Integer ingredientId) {
        try {
            InventoryResponse inventory = stockInService.getInventoryByIngredientId(ingredientId);
            return ApiResponse.<InventoryResponse>builder()
                    .result(inventory)
                    .success(true)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<InventoryResponse>builder()
                    .result(null)
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/import")
    public ApiResponse<String> importStock(@RequestBody StockInRequest request) {
        try {
            stockInService.importStock(request);
            return ApiResponse.<String>builder()
                    .result("Nhập kho thành công")
                    .success(true)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .result("Lỗi nhập kho: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    @GetMapping("/{ingredientId}/stock-batches")
    public ApiResponse<List<StockInBatchResponse>> getStockBatchesByIngredientId(
            @PathVariable Integer ingredientId,
            @RequestParam(name = "sort", defaultValue = "importedAt,desc") String sortParam
    ) {
        try {
            List<StockInBatchResponse> stockBatches = stockInService.getStockBatchesByIngredientId(ingredientId, sortParam);
            return ApiResponse.<List<StockInBatchResponse>>builder()
                    .result(stockBatches)
                    .success(true)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<List<StockInBatchResponse>>builder()
                    .result(null)
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/init")
    public ApiResponse<String> initializeStockData() {
        try {
            stockInService.initializeStockData();
            return ApiResponse.<String>builder()
                    .result("Khởi tạo dữ liệu kho thành công")
                    .success(true)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .result("Lỗi khởi tạo dữ liệu kho: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }
}
