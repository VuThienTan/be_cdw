package com.cdw.cdw.domain.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPageResponse {

    int currentPage;
    int pageSize;
    long totalItems;
    int totalPages;
    Double averageRating;
    List<ReviewResponse> reviews;
}
