package com.cdw.cdw.domain.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String username;
    private String phoneNumber;
    private String address;
}