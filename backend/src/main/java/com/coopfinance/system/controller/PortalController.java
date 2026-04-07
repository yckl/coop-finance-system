package com.coopfinance.system.controller;

import com.coopfinance.system.common.ApiResponse;
import com.coopfinance.system.model.AskAiRequest;
import com.coopfinance.system.model.ModuleSaveRequest;
import com.coopfinance.system.model.TransactionRequest;
import com.coopfinance.system.service.RealFinanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import com.coopfinance.system.model.ReimbursementRequest;
import com.coopfinance.system.model.AuditRequest;

@RestController
@RequestMapping("/api")
public class PortalController {

    private final RealFinanceService financeService;

    public PortalController(RealFinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/{role}/dashboard")
    public ApiResponse<?> dashboard(@PathVariable String role, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResponse.success(financeService.getDashboard(role, username));
    }

    @GetMapping("/{role}/module/{module}")
    public ApiResponse<?> moduleData(@PathVariable String role, @PathVariable String module, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return ApiResponse.success(financeService.getModuleData(role, module, username));
    }

    @PostMapping("/{role}/module/{module}")
    public ApiResponse<?> saveModuleData(@PathVariable String role,
                                         @PathVariable String module,
                                         @RequestBody ModuleSaveRequest request) {
        return ApiResponse.success("保存成功", financeService.saveModuleData(role, module, request.payload()));
    }

    @DeleteMapping("/{role}/module/{module}/{id}")
    public ApiResponse<?> deleteModuleData(@PathVariable String role, @PathVariable String module, @PathVariable String id) {
        financeService.deleteModuleData(role, module, id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{role}/analytics")
    public ApiResponse<?> analytics(@PathVariable String role) {
        return ApiResponse.success(financeService.getAnalytics(role));
    }

    @GetMapping("/search")
    public ApiResponse<?> search(@RequestParam String role, @RequestParam(required = false) String keyword) {
        return ApiResponse.success(financeService.globalSearch(role, keyword));
    }

    @PostMapping("/ai/{role}")
    public ApiResponse<?> askAi(@PathVariable String role, @Valid @RequestBody AskAiRequest request) {
        return ApiResponse.success(financeService.askAi(role, request.question()));
    }

    @GetMapping("/{role}/account/{userNo}")
    public ApiResponse<?> queryAccount(@PathVariable String role, @PathVariable String userNo) {
        return ApiResponse.success(financeService.queryAccount(userNo));
    }

    @PostMapping("/{role}/transaction/{kind}")
    public ApiResponse<?> submitTransaction(@PathVariable String role, @PathVariable String kind, @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success("业务办理成功", financeService.submitTransaction(kind, request));
    }

    @PostMapping("/{role}/reimbursement/apply")
    public ApiResponse<?> applyReimbursement(@PathVariable String role, @Valid @RequestBody ReimbursementRequest request, HttpServletRequest hr) {
        String username = (String) hr.getAttribute("username");
        String fullName = (String) hr.getAttribute("name");
        return ApiResponse.success(financeService.applyReimbursement(role, username, fullName, request));
    }

    @PostMapping("/{role}/reimbursement/audit")
    public ApiResponse<?> auditReimbursement(@PathVariable String role, @Valid @RequestBody AuditRequest request, HttpServletRequest hr) {
        String fullName = (String) hr.getAttribute("name");
        return ApiResponse.success(financeService.auditReimbursement(role, fullName, request));
    }

    @GetMapping("/{role}/export/{module}")
    public ApiResponse<?> export(@PathVariable String role, @PathVariable String module) {
        return ApiResponse.success(financeService.exportModule(role, module));
    }
}
