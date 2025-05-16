package com.cdw.cdw.domain.dto.response;

import com.cdw.cdw.domain.entity.MenuItem;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemPageResponse {
    int currentPage;
    int pageSize;
    long totalItems;
    int totalPages;
    List<MenuItemResponse> menuItems;
}
