package com.cdw.cdw.domain.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE )
public class PageResponse<T> {
      List<T> content;
      int currentPage;
      int totalPages;
      long totalItems;
      int pageSize;
}
