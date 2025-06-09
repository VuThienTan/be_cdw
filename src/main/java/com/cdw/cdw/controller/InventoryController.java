package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.response.InventoryResponse;
import com.cdw.cdw.domain.dto.response.PageResponse;
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

    @PostMapping("/reset-all")
    public ResponseEntity<?> resetAllQuantities() {
        stockInService.updateAllToTenThousandIncludingMissing();
        return ResponseEntity.ok("Tất cả tồn kho đã được cập nhật thành 10.000 đơn vị");
    }

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
}
