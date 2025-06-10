package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.StockInBatch;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StockInBatchRepository extends JpaRepository<StockInBatch, Integer> {
    @Query("SELECT sb FROM StockInBatch sb WHERE sb.ingredient.id = :ingredientId AND sb.quantity > sb.used ORDER BY sb.expiryDate ASC")
    List<StockInBatch> findAvailableBatchesByIngredientOrderByExpiryDate(@Param("ingredientId") Integer ingredientId);
    
    // Sorting methods with direction parameter
    List<StockInBatch> findByIngredient_Id(Integer ingredientId, Sort sort);

}
