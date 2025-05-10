package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.Category;
import com.cdw.cdw.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
}
