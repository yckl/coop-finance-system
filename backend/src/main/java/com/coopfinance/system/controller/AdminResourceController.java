package com.coopfinance.system.controller;

import com.coopfinance.system.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminResourceController {

    private final JdbcTemplate jdbcTemplate;

    public AdminResourceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/users/freeze/{id}")
    public ApiResponse<?> freezeUser(@PathVariable String id) {
        jdbcTemplate.update("UPDATE sys_user SET account_status = '冻结' WHERE id = ?", id);
        return ApiResponse.success("用户已冻结", null);
    }

    @PostMapping("/users/unfreeze/{id}")
    public ApiResponse<?> unfreezeUser(@PathVariable String id) {
        jdbcTemplate.update("UPDATE sys_user SET account_status = '正常' WHERE id = ?", id);
        return ApiResponse.success("用户已解冻", null);
    }
    
    @PostMapping("/users/reset-password/{id}")
    public ApiResponse<?> resetPassword(@PathVariable String id) {
        jdbcTemplate.update("UPDATE sys_user SET password = '123456' WHERE id = ?", id);
        return ApiResponse.success("密码已重置为: 123456", null);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable String id) {
        jdbcTemplate.update("DELETE FROM sys_user WHERE id = ?", id);
        return ApiResponse.success("删除成功", null);
    }
    
    // Add additional roles like admin-accounts and finance-staff handling if needed
    // The base info comes from /api/admin/module/users dynamically
}
