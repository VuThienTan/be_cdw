package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.MenuItem;
import com.cdw.cdw.domain.entity.MenuItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MenuItemIngredientRepository extends JpaRepository<MenuItemIngredient, Integer> {

    List<MenuItemIngredient> findByMenuItem_Id(Long menuItemId);

    @Query("SELECT mi FROM MenuItemIngredient mi JOIN FETCH mi.ingredient WHERE mi.menuItem.id = :menuItemId")
    List<MenuItemIngredient> findByMenuItem_IdWithIngredients(@Param("menuItemId") Long menuItemId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM MenuItemIngredient mi WHERE mi.menuItem = :menuItem")
    void deleteByMenuItem(@Param("menuItem") MenuItem menuItem);
}
