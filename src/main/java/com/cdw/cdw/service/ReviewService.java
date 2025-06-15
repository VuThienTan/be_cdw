package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.ReviewRequest;
import com.cdw.cdw.domain.dto.response.ReviewPageResponse;
import com.cdw.cdw.domain.dto.response.ReviewResponse;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.dto.response.UserReviewResponse;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.ProductReview;
import com.cdw.cdw.domain.entity.User;

import com.cdw.cdw.repository.MenuItemRepository;
import com.cdw.cdw.repository.ProductReviewRepository;
import com.cdw.cdw.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Lấy thông tin người dùng hiện tại
        UserResponse currentUser = userService.getMyInfo();

        // Kiểm tra sản phẩm tồn tại
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        // Tìm user entity từ ID
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
        reviewRepository.findByUserIdAndMenuItemId(user.getId(), menuItem.getId())
                .ifPresent(review -> {
                    throw new IllegalStateException("You have already reviewed this product");
                });

        // Tạo đánh giá mới
        ProductReview review = ProductReview.builder()
                .menuItem(menuItem)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        // Lưu đánh giá
        ProductReview savedReview = reviewRepository.save(review);

        // Trả về response
        return mapToReviewResponse(savedReview);
    }

    public ReviewPageResponse getReviewsByMenuItemId(Long menuItemId, int page, int size) {
        // Tạo pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Lấy danh sách đánh giá
        Page<ProductReview> reviewPage = reviewRepository.findByMenuItemId(menuItemId, pageable);

        // Lấy điểm đánh giá trung bình
        Double averageRating = reviewRepository.getAverageRatingByMenuItemId(menuItemId);

        // Map sang DTO
        List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());

        // Tạo response
        return ReviewPageResponse.builder()
                .currentPage(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalItems(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviews(reviewResponses)
                .build();
    }

    // Helper method để map từ entity sang DTO
    private ReviewResponse mapToReviewResponse(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .menuItemId(review.getMenuItem().getId())
                .menuItemName(review.getMenuItem().getName())
                .user(UserReviewResponse.builder()
                        .id(review.getUser().getId())
                        .fullName(review.getUser().getFullName())
                        .username(review.getUser().getUsername())
                        .build())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
