package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.entity.Notification;
import com.cdw.cdw.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotifyController {
    NotificationRepository notificationRepository;


    @GetMapping()
    public ApiResponse<List<Notification>> getAllNotifications() {
        notificationRepository.markAllAsRead();
        List<Notification> notifications = notificationRepository.findAllByOrderByCreatedAtDesc();
        return ApiResponse.<List<Notification>>builder().result(notifications).success(true).build();
    }

    @PutMapping("{id}/read")
    public ApiResponse<Notification> markNotificationRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
        return ApiResponse.<Notification>builder().result(notification).success(true).build();
    }

    @GetMapping("/not-read")
    public ApiResponse<Long> countByReadFalse() {
        long countByReadFalse = notificationRepository.countByReadFalse();
        return ApiResponse.<Long>builder()
                .result(countByReadFalse)
                .success(true)
                .build();
    }


}
