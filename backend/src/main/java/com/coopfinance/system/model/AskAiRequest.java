package com.coopfinance.system.model;

import jakarta.validation.constraints.NotBlank;

public record AskAiRequest(@NotBlank(message = "问题不能为空") String question) {
}
