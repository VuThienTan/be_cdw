package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer>, JpaSpecificationExecutor<Ingredient> {
    Optional<Ingredient> findByName(String name);
    List<Ingredient> findByNameContainingIgnoreCase(String name);
}
