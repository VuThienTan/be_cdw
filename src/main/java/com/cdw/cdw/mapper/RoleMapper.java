package com.cdw.cdw.mapper;

import com.cdw.cdw.domain.dto.request.RoleCreateRequest;
import com.cdw.cdw.domain.dto.response.RoleResponse;
import com.cdw.cdw.domain.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleCreateRequest request);

    RoleResponse toRoleResponse(Role role);
}
