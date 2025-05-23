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
    List<CartItem> findByUser(User user);

    @Query("SELECT ci FROM CartItem ci WHERE ci.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") String userId);

    Optional<CartItem> findByUserAndMenuItem(User user, MenuItem menuItem);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.user.id = :userId")
    Integer countItemsByUserId(@Param("userId") String userId);

    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM cart_items WHERE user_id = :userId AND created_at < DATE_SUB(NOW(), INTERVAL :days DAY)",
            nativeQuery = true)
    List<CartItem> findOldCartItems(@Param("userId") String userId, @Param("days") int days);
}
