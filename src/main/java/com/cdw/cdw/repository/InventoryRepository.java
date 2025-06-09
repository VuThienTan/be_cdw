package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.Ingredient;
import com.cdw.cdw.domain.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer>, JpaSpecificationExecutor<Inventory> {
    Optional<Inventory> findByIngredient(Ingredient ingredient);
}
