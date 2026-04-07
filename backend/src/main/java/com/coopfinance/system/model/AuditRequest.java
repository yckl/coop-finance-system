package com.coopfinance.system.model;

import jakarta.validation.constraints.NotBlank;

public record AuditRequest(
    @NotBlank String reimbursementNo,
    @NotBlank String result,
    String comment
) {}
