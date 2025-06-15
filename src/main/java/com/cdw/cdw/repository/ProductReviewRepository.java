package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    Page<ProductReview> findByMenuItemId(Long menuItemId, Pageable pageable);

    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.menuItem.id = :menuItemId")
    Double getAverageRatingByMenuItemId(Long menuItemId);

    Optional<ProductReview> findByUserIdAndMenuItemId(String userId, Long menuItemId);
}
