package com.coopfinance.system.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "用户编号不能为空") String userNo,
        @NotNull(message = "交易金额不能为空") BigDecimal amount,
        @NotBlank(message = "交易类型不能为空") String type,
        @NotBlank(message = "经办人不能为空") String operator,
        String remark
) {
}
