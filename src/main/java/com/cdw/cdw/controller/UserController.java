package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.request.UserUpdateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.createUser(request));
        return response;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable ("userId") String id) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.getUser(id));
        return response;
    }




    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> myInfo () {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.getMyInfo());
        return response;
    }
    @GetMapping()
    public List<UserResponse> getAll() {
        return userService.getAllUser();
    }
    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable("userId") String id,
                                                @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.updateUser(id, request));
        return response;
    }
    @DeleteMapping("/{userId}")
    public ApiResponse<UserResponse> deleteUser(@PathVariable("userId") String id) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.deleteUser(id));
        return response;
    }







}
