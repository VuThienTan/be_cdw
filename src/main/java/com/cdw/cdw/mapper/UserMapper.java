package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest userCreateRequest);
    UserResponse toUserResponse(User user);
    List<UserResponse> toUserResponse(List<User> users);
}
