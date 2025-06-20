package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
    List<Notification> findByReadFalseOrderByCreatedAtDesc();
    long countByReadFalse();

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.read = false")
    void markAllAsRead();

} 