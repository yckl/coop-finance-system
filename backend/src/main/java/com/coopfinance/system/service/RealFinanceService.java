package com.coopfinance.system.service;

import com.coopfinance.system.common.JwtUtil;
import com.coopfinance.system.mapper.CommonMapper;
import com.coopfinance.system.mapper.DashboardMapper;
import com.coopfinance.system.mapper.SysUserMapper;
import com.coopfinance.system.model.TransactionRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RealFinanceService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SysUserMapper sysUserMapper;
    private final CommonMapper commonMapper;
    private final DashboardMapper dashboardMapper;
    private final JwtUtil jwtUtil;

    public RealFinanceService(SysUserMapper sysUserMapper, CommonMapper commonMapper, DashboardMapper dashboardMapper, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.commonMapper = commonMapper;
        this.dashboardMapper = dashboardMapper;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> user = sysUserMapper.login(username, password);
        if (user != null) {
            String roleName = (String) user.get("role_code");
            String realName = (String) user.get("real_name");
            String role = "admin";
            if ("FINANCE".equals(roleName)) role = "finance";
            if ("USER".equals(roleName)) role = "user";

            String token = jwtUtil.generateToken(username, role, roleName, realName);
            return buildLoginProfile(token, role, roleName, realName, "/" + role + "/dashboard");
        }
        throw new IllegalArgumentException("用户名或密码错误");
    }

    public Map<String, Object> registerUser(String username, String name, String phone) {
        throw new IllegalArgumentException("暂不支持在线注册，请联系柜面核实身份后开户");
    }

    public Map<String, Object> getDashboard(String role, String username) {
        if ("admin".equals(role)) {
            BigDecimal dep = dashboardMapper.getTodayDepositTotal();
            BigDecimal wit = dashboardMapper.getTodayWithdrawTotal();
            BigDecimal net = dep.subtract(wit);
            
            return Map.of(
                "metrics", List.of(
                    Map.of("label", "今日存款总额", "value", dep.toPlainString(), "unit", "元", "trend", "今日实时"),
                    Map.of("label", "今日取款总额", "value", wit.toPlainString(), "unit", "元", "trend", "今日实时"),
                    Map.of("label", "今日净流入", "value", net.toPlainString(), "unit", "元", "trend", "今日实时"),
                    Map.of("label", "本月累计存款", "value", dashboardMapper.getMonthDepositTotal().toPlainString(), "unit", "元", "trend", "本月实时"),
                    Map.of("label", "当前用户总数", "value", String.valueOf(dashboardMapper.getUserTotal()), "unit", "户", "trend", "总计"),
                    Map.of("label", "财务人员总数", "value", String.valueOf(dashboardMapper.getFinanceStaffTotal()), "unit", "人", "trend", "总计"),
                    Map.of("label", "待审核报销数", "value", String.valueOf(dashboardMapper.getPendingReimbursementsTotal()), "unit", "单", "trend", "需处理"),
                    Map.of("label", "异常预警数", "value", String.valueOf(dashboardMapper.getPendingRiskWarningsTotal()), "unit", "条", "trend", "需关注")
                ), 
                "trend", getAnalytics(role),
                // Recent notices, logins, operations can be mapped through common mapper
                "notices", commonMapper.executeSelect("SELECT notice_title as 标题, notice_type as 类型 FROM sys_notice ORDER BY created_at DESC LIMIT 5"), 
                "todo", commonMapper.executeSelect("SELECT concat('系统预警: ', warning_content) as item FROM risk_warning WHERE process_status='未处理' LIMIT 5").stream().map(m -> m.get("item")).collect(Collectors.toList()),
                "warnings", commonMapper.executeSelect("SELECT concat('操作人[', operator_name, ']: ', action_content) as item FROM audit_log ORDER BY created_at DESC LIMIT 5").stream().map(m -> m.get("item")).collect(Collectors.toList())
            );
        }
        if ("user".equals(role)) {
            List<Map<String, Object>> bal = commonMapper.executeSelect("SELECT available_balance FROM account_balance WHERE user_no='" + username + "'");
            String myBalance = bal.isEmpty() ? "0.00" : bal.get(0).get("available_balance").toString();
            return Map.of(
                "metrics", List.of(
                    Map.of("label", "当前可用余额", "value", myBalance, "unit", "元", "trend", "账户总计"),
                    Map.of("label", "最新收到通知", "value", "1", "unit", "条", "trend", "未读消息")
                ), 
                "trend", getAnalytics(role), 
                "todo", List.of("需跟进: 您的存款记录已更新"), 
                "notices", commonMapper.executeSelect("SELECT notice_title as 标题, notice_type as 类型 FROM sys_notice ORDER BY created_at DESC LIMIT 5"), 
                "warnings", List.of()
            );
        }
        return Map.of("metrics", List.of(), "trend", getAnalytics(role), "todo", List.of(), "notices", List.of(), "warnings", List.of());
    }

    public Map<String, Object> getModuleData(String role, String module, String username) {
        if ("dictionaries".equals(module)) {
            return Map.of(
                "dictTypes", commonMapper.executeSelect("SELECT * FROM dictionary_type ORDER BY id"),
                "dictItems", commonMapper.executeSelect("SELECT * FROM dictionary_item ORDER BY type_code, item_sort"),
                "configs", commonMapper.executeSelect("SELECT * FROM system_config ORDER BY config_group, id")
            );
        }
        if ("announcements".equals(module)) {
            return Map.of("records", commonMapper.executeSelect("SELECT * FROM sys_notice ORDER BY CASE top_flag WHEN '是' THEN 1 ELSE 2 END, created_at DESC"));
        }
        if ("messages".equals(module)) {
            if ("user".equals(role)) {
                return Map.of("records", commonMapper.executeSelect("SELECT * FROM message_center WHERE user_id='" + username + "' ORDER BY created_at DESC"));
            }
            return Map.of("records", commonMapper.executeSelect("SELECT * FROM message_center ORDER BY FIELD(process_status, '未处理', '处理中', '已处理'), created_at DESC"));
        }
        if ("security".equals(module)) {
            return Map.of(
                "auditLogs", commonMapper.executeSelect("SELECT * FROM audit_log ORDER BY created_at DESC"),
                "riskWarnings", commonMapper.executeSelect("SELECT * FROM risk_warning ORDER BY created_at DESC")
            );
        }
        String sql = "";
        String userCondition = "user".equals(role) ? " WHERE user_no='" + username + "'" : "";
        String transUserCondition = "user".equals(role) ? " AND user_no='" + username + "'" : "";
        String reimUserCondition = "user".equals(role) ? " WHERE applicant_name=(SELECT real_name FROM sys_user WHERE username='" + username + "')" : "";

        if ("users".equals(module)) sql = "SELECT * FROM sys_user WHERE role_code='USER'";
        else if ("finance-staff".equals(module)) sql = "SELECT * FROM finance_staff_profile";
        else if ("admin-accounts".equals(module)) sql = "SELECT * FROM sys_user WHERE role_code='ADMIN'";
        else if ("dictionaries".equals(module)) sql = "SELECT * FROM dictionary_item";
        else if ("announcements".equals(module)) sql = "SELECT * FROM sys_notice";
        else if ("messages".equals(module)) sql = "SELECT * FROM message_center";
        else if ("reimbursements".equals(module)) sql = "SELECT * FROM reimbursement_order" + reimUserCondition;
        else if ("balances".equals(module) || "balance".equals(module)) sql = "SELECT * FROM account_balance" + userCondition;
        else if ("transactions".equals(module)) sql = "SELECT * FROM transaction_record" + userCondition;
        else if ("deposits".equals(module)) sql = "SELECT * FROM transaction_record WHERE transaction_type='存款'" + transUserCondition;
        else if ("withdrawals".equals(module)) sql = "SELECT * FROM transaction_record WHERE transaction_type='取款'" + transUserCondition;
        else if ("profile".equals(module)) sql = "SELECT * FROM user_profile" + userCondition;
        else if ("notifications".equals(module)) sql = "SELECT * FROM sys_notice";
        else sql = "SELECT 1 as empty_data";

        List<Map<String, Object>> records = commonMapper.executeSelect(sql);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("title", module);
        response.put("description", module);
        response.put("records", records);
        response.put("columns", inferColumns(records));
        response.put("summary", Map.of("total", records.size()));
        return response;
    }

    public Map<String, Object> saveModuleData(String role, String module, Map<String, Object> payload) {
        // Placeholder for real save using named parameter jdbc template
        return payload;
    }

    public void deleteModuleData(String role, String module, String id) {
        // Placeholder
    }

    public Map<String, Object> getAnalytics(String role) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trendLabels", List.of("一月", "二月", "三月", "四月", "五月", "六月"));
        result.put("depositTrend", List.of(12000, 14500, 16600, 18100, 21000, 24500));
        result.put("withdrawTrend", List.of(8000, 9500, 11000, 12300, 13200, 16000));
        result.put("netInflow", List.of(4000, 5000, 5600, 5800, 7800, 8500));
        result.put("pieData", List.of(
                Map.of("name", "农业贷款", "value", 36),
                Map.of("name", "合作社存款", "value", 28),
                Map.of("name", "农资报销", "value", 18),
                Map.of("name", "其他业务", "value", 18)
        ));
        result.put("rankings", List.of(
                Map.of("name", "北郊网点", "value", 98),
                Map.of("name", "中心网点", "value", 88),
                Map.of("name", "西区网点", "value", 81)
        ));
        result.put("role", role);
        return result;
    }

    public Map<String, Object> globalSearch(String role, String keyword) {
        return Map.of("keyword", keyword, "results", List.of());
    }

    public Map<String, Object> askAi(String role, String question) {
        return Map.of("question", question, "answer", "AI服务已连接", "suggestions", List.of());
    }

    public Map<String, Object> queryAccount(String userNo) {
        Map<String, Object> bal = sysUserMapper.getAccountBalance(userNo);
        if (bal == null) throw new IllegalArgumentException("未查询到该用户账户");
        Map<String, Object> res = new HashMap<>(bal);
        res.put("recentTransactions", List.of());
        return res;
    }

    public Map<String, Object> submitTransaction(String transactionKind, TransactionRequest request) {
        Map<String, Object> bal = sysUserMapper.getAccountBalance(request.userNo());
        if (bal == null) throw new IllegalArgumentException("用户账户不存在");
        
        BigDecimal currentBalance = new BigDecimal(bal.get("available_balance").toString());
        BigDecimal amount = request.amount();
        BigDecimal nextBalance = "deposit".equals(transactionKind) ? currentBalance.add(amount) : currentBalance.subtract(amount);
        
        if ("withdraw".equals(transactionKind) && currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        
        sysUserMapper.updateAccountBalance(request.userNo(), nextBalance);

        // Fetch User Name for transaction log (assuming user exists from account balance)
        List<Map<String, Object>> users = commonMapper.executeSelect("SELECT real_name FROM sys_user WHERE id = (SELECT id FROM user_profile WHERE user_no = '" + request.userNo() + "' LIMIT 1)");
        String userName = users.isEmpty() ? "未知用户" : (String) users.get(0).get("real_name");

        String warningLevel = "正常";
        if ("withdraw".equals(transactionKind) && amount.compareTo(new BigDecimal("50000")) > 0) {
            warningLevel = "高";
            commonMapper.executeSelect("INSERT INTO risk_warning (warning_code, warning_type, warning_level, warning_object, warning_content, process_status) VALUES ('WR_" + System.currentTimeMillis() + "', '大额取款预警', '高', '" + userName + "', '单日取款 " + amount + " 元', '未处理')");
        }

        String serialNo = "TX" + System.currentTimeMillis();
        Map<String, Object> transMap = new HashMap<>();
        transMap.put("serialNo", serialNo);
        transMap.put("userNo", request.userNo());
        transMap.put("userName", userName);
        transMap.put("transactionType", "deposit".equals(transactionKind) ? "存款" : "取款");
        transMap.put("businessType", request.type() != null ? request.type() : "常规业务");
        transMap.put("amount", amount);
        transMap.put("balanceAfter", nextBalance);
        transMap.put("operatorName", request.operator() != null ? request.operator() : "系统经办");
        transMap.put("warningLevel", warningLevel);

        sysUserMapper.insertTransactionRecord(transMap);

        return Map.of("message", "业务办理成功", "serialNo", serialNo, "balanceAfter", nextBalance);
    }

    public Map<String, Object> exportModule(String role, String module) {
        return Map.of("message", "导出成功");
    }

    private Map<String, Object> buildLoginProfile(String token, String role, String roleName, String name, String homePath) {
        return Map.of(
                "token", token,
                "user", Map.of(
                        "username", role,
                        "name", name,
                        "role", role,
                        "roleName", roleName,
                        "badge", roleName,
                        "homePath", homePath
                )
        );
    }

    private List<Map<String, Object>> inferColumns(List<Map<String, Object>> records) {
        if (records.isEmpty()) return List.of();
        return records.get(0).keySet().stream()
                .map(key -> Map.<String, Object>of("prop", key, "label", key))
                .collect(Collectors.toList());
    }
}
