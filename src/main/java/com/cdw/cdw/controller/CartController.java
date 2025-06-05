package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.CartItemRequest;
import com.cdw.cdw.domain.dto.response.CartItemResponse;
import com.cdw.cdw.domain.dto.response.CartResponse;
import com.cdw.cdw.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

//Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    // Cập nhật số lượng
    @PutMapping("/{id}")
    public ResponseEntity<CartItemResponse> updateCartItemQuantity(
            @PathVariable Long id,
            @RequestParam int quantity) {
        CartItemResponse response = cartService.updateCartItemQuantity(id, quantity);
        return ResponseEntity.ok(response);
    }


}
