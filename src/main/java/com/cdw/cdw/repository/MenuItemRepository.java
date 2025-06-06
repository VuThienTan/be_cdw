package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long>, JpaSpecificationExecutor<MenuItem> {
    List<MenuItem> findByName(String name);
    Page<MenuItem> findByRation(int ration, Pageable pageable);
    Page<MenuItem> findByPriceBetween(double form, double to, Pageable pageable);
    Page<MenuItem> findByRationAndPriceBetween(Integer ration, Double priceFrom, Double priceTo, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<MenuItem> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM menu_item ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<MenuItem> getRandomMenuItem();

    MenuItem findById(long id);
    List<MenuItem> getMenuItemsById(Long id);
}