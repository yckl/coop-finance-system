package com.coopfinance.system.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReimbursementRequest(
    @NotBlank String type,
    @NotNull BigDecimal amount,
    String url
) {}
