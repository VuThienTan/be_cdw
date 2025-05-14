package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.RoleCreateRequest;
import com.cdw.cdw.domain.dto.response.RoleResponse;
import com.cdw.cdw.domain.entity.Role;
import com.cdw.cdw.mapper.RoleMapper;
import com.cdw.cdw.repository.PermissionRepository;
import com.cdw.cdw.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse createRole(RoleCreateRequest request) {
        var role = roleMapper.toRole(request);
        var permission = permissionRepository.findAllById(request.getPermissions());

        log.info("Creating permission: {}", permission);

        role.setPermissions(new HashSet<>(permission));
        log.info("Creating role: {}", role);

        role = roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        var role = roleRepository.findAll();
        return role.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRole(String roleId) {
        roleRepository.deleteById(roleId);
    }



}
