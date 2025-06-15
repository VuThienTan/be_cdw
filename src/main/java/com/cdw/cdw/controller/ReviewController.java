package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.ReviewRequest;
import com.cdw.cdw.domain.dto.response.ReviewPageResponse;
import com.cdw.cdw.domain.dto.response.ReviewResponse;
import com.cdw.cdw.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewRequest request) {
        ApiResponse<ReviewResponse> response = new ApiResponse<>();
        response.setResult(reviewService.createReview(request));
        return response;
    }

    @GetMapping("/menu-item/{menuItemId}")
    public ApiResponse<ReviewPageResponse> getReviewsByMenuItemId(
            @PathVariable Long menuItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ApiResponse<ReviewPageResponse> response = new ApiResponse<>();
        response.setResult(reviewService.getReviewsByMenuItemId(menuItemId, page, size));
        return response;
    }
}
