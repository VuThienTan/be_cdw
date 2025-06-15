package com.cdw.cdw.domain.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReviewResponse {
    String id;
    String fullName;
    String username;
}
