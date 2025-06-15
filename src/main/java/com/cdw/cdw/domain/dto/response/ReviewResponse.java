package com.cdw.cdw.domain.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    Long id;
    Long menuItemId;
    String menuItemName;
    UserReviewResponse user;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
