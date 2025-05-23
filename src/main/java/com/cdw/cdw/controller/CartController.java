package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.CartItemRequest;
import com.cdw.cdw.domain.dto.request.CartItemUpdateRequest;
import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.dto.response.CartResponse;
import com.cdw.cdw.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {
    CartService cartService;

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@RequestBody CartItemRequest request) {
        CartItemResponse response = cartService.addItemToCartForCurrentUser(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse response = cartService.getCurrentUserCart();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{itemId}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        ApiResponse<CartItemResponse> response = new ApiResponse<>();
        response.setResult(cartService.updateCartItem(userId, itemId, request));
        return response;
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCartItem(@PathVariable("itemId") Long itemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        cartService.removeCartItem(userId, itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        cartService.clearCart(userId);
    }
}
