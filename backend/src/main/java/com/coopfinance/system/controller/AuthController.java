package com.coopfinance.system.controller;

import com.coopfinance.system.common.ApiResponse;
import com.coopfinance.system.model.LoginRequest;
import com.coopfinance.system.model.RegisterRequest;
import com.coopfinance.system.service.RealFinanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RealFinanceService financeService;

    public AuthController(RealFinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", financeService.login(request.username(), request.password()));
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", financeService.registerUser(request.username(), request.name(), request.phone()));
    }
}
