package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.PermissionCreateRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.PermissionResponse;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.Permission;
import com.cdw.cdw.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionCreateRequest permissionCreateRequest);
    PermissionResponse toPermissionResponse(Permission permission);
}
