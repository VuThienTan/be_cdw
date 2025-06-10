package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.StockInBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockInBatchRepository extends JpaRepository<StockInBatch, Integer> {
    List<StockInBatch> findByIngredient_IdOrderByImportedAtDesc(Integer ingredientId);
    List<StockInBatch> findByIngredient_IdOrderByExpiryDateAsc(Integer ingredientId);
}
