package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.CartItem;
import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserAndMenuItem(User user, MenuItem menuItem);
}
