package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.createUser(request));
        return response;
    }

    @GetMapping()
    public List<UserResponse> getUsers() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable("userId") String id) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.getUser(id));
        return response;
    }


    @DeleteMapping("/{userId}")
    public ApiResponse<UserResponse> deleteUser(@PathVariable("userId") String id) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.deleteUser(id));
        return response;
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> myInfo () {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.getMyInfo());
        return response;
    }
}
