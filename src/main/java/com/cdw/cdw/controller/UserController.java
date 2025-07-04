package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.UpdateUserRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.request.UserUpdateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import com.cdw.cdw.exception.AppException;
import org.springframework.web.bind.annotation.*;
import com.cdw.cdw.validation.UserValidator;
import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;
    UserValidator userValidator;
    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request, BindingResult bindingResult) {
        // Áp dụng validator
        userValidator.validate(request, bindingResult);

        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            String errorCode = bindingResult.getFieldError().getCode();
            throw AppException.badRequest(errorCode);
        }
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

    @PutMapping("/info/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.updateUser(id, request));
        return response;
    }

}
