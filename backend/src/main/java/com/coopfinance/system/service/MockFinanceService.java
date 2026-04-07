package com.coopfinance.system.service;

import com.coopfinance.system.model.TransactionRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MockFinanceService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("50000.00");

    private final Map<String, List<Map<String, Object>>> modules = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> accounts = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    public MockFinanceService() {
        initBaseData();
    }

    public Map<String, Object> login(String username, String password) {
        if ("admin".equals(username) && "123456".equals(password)) {
            return buildLoginProfile("admin", "管理员", "系统管理员", "/admin/dashboard", "管理员");
        }
        if ("caiwu".equals(username) && "123456".equals(password)) {
            return buildLoginProfile("finance", "财务人员", "张会计", "/finance/dashboard", "财务人员");
        }
        if ("user".equals(username) && "123456".equals(password)) {
            return buildLoginProfile("user", "普通用户", "李玉兰", "/user/dashboard", "普通用户");
        }
        throw new IllegalArgumentException("用户名或密码错误");
    }

    public Map<String, Object> registerUser(String username, String name, String phone) {
        Map<String, Object> user = new LinkedHashMap<>();
        String userNo = "U" + sequence.incrementAndGet();
        user.put("id", userNo);
        user.put("userNo", userNo);
        user.put("name", name);
        user.put("gender", "女");
        user.put("phone", phone);
        user.put("idCard", "4101************");
        user.put("address", "河南省郑州市");
        user.put("status", "正常");
        user.put("registerTime", now());
        user.put("balance", number(new BigDecimal("8000")));
        user.put("riskLevel", "低");
        modules.get("admin-users").add(0, user);
        accounts.put(userNo, buildAccount(userNo, name, new BigDecimal("8000"), "正常", "低"));
        return buildLoginProfile("user", "普通用户", name, "/user/dashboard", "普通用户");
    }

    public Map<String, Object> getDashboard(String role) {
        return switch (role) {
            case "admin" -> buildAdminDashboard();
            case "finance" -> buildFinanceDashboard();
            case "user" -> buildUserDashboard();
            default -> throw new IllegalArgumentException("不支持的角色");
        };
    }

    public Map<String, Object> getModuleData(String role, String module) {
        List<Map<String, Object>> records = new ArrayList<>(modules.getOrDefault(key(role, module), List.of()));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("title", moduleTitle(role, module));
        response.put("description", moduleDescription(role, module));
        response.put("records", records);
        response.put("columns", inferColumns(records));
        response.put("summary", buildModuleSummary(role, module, records));
        return response;
    }

    public Map<String, Object> saveModuleData(String role, String module, Map<String, Object> payload) {
        String key = key(role, module);
        List<Map<String, Object>> records = modules.computeIfAbsent(key, item -> new ArrayList<>());
        Map<String, Object> row = new LinkedHashMap<>(payload == null ? Map.of() : payload);
        String id = Objects.toString(row.getOrDefault("id", ""), "");
        if (id.isBlank()) {
            id = module.substring(0, Math.min(module.length(), 3)).toUpperCase(Locale.ROOT) + sequence.incrementAndGet();
            row.put("id", id);
            row.putIfAbsent("创建时间", now());
            records.add(0, row);
        } else {
            String finalId = id;
            records.removeIf(item -> Objects.equals(Objects.toString(item.get("id"), ""), finalId));
            row.putIfAbsent("更新时间", now());
            records.add(0, row);
        }
        if ("reimbursements".equals(module) && "finance".equals(role)) {
            row.putIfAbsent("状态", "已审核");
            modules.get("admin-reimbursements").add(0, row);
        }
        return row;
    }

    public void deleteModuleData(String role, String module, String id) {
        List<Map<String, Object>> records = modules.getOrDefault(key(role, module), new ArrayList<>());
        records.removeIf(item -> Objects.equals(Objects.toString(item.get("id"), ""), id));
    }

    public Map<String, Object> getAnalytics(String role) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trendLabels", List.of("一月", "二月", "三月", "四月", "五月", "六月"));
        result.put("depositTrend", List.of(120, 140, 166, 181, 210, 245));
        result.put("withdrawTrend", List.of(80, 95, 110, 123, 132, 160));
        result.put("netInflow", List.of(40, 45, 56, 58, 78, 85));
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
        String normalized = keyword == null ? "" : keyword.trim();
        List<Map<String, Object>> matches = modules.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(role + "-"))
                .flatMap(entry -> entry.getValue().stream().map(item -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("module", entry.getKey().substring(role.length() + 1));
                    result.put("title", moduleTitle(role, entry.getKey().substring(role.length() + 1)));
                    result.put("text", item.values().stream().map(String::valueOf).collect(Collectors.joining(" / ")));
                    result.put("id", item.get("id"));
                    return result;
                }))
                .filter(item -> normalized.isBlank() || Objects.toString(item.get("text"), "").contains(normalized))
                .limit(12)
                .toList();
        return Map.of("keyword", normalized, "results", matches);
    }

    public Map<String, Object> askAi(String role, String question) {
        BigDecimal totalBalance = accounts.values().stream()
                .map(item -> new BigDecimal(Objects.toString(item.get("balance"), "0")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String answer = switch (role) {
            case "admin" -> "根据当前经营数据，系统总余额为 " + totalBalance + " 元，建议重点关注大额取款预警、异常报销预警与未读公告覆盖率。本月净流入整体向上，北郊网点和中心网点业务量较高。";
            case "finance" -> "根据近六个月走势，本周取款高峰集中在周三和周五，大额取款主要来自重点客户。建议先查看统一流水与异常预警，再处理待审核报销。";
            case "user" -> "根据您的账户概览，当前余额稳定，本月收入高于支出。若您要提交报销申请，请准备票据附件并在“报销申请”页面填写金额、类型和用途说明。";
            default -> "AI 助手已收到问题：" + question;
        };
        return Map.of(
                "question", question,
                "answer", answer,
                "suggestions", List.of("生成经营摘要", "解释图表数据", "定位风险预警", "输出中文简报")
        );
    }

    public Map<String, Object> queryAccount(String userNo) {
        Map<String, Object> account = accounts.get(userNo);
        if (account == null) {
            throw new IllegalArgumentException("未查询到该用户账户");
        }
        Map<String, Object> response = new LinkedHashMap<>(account);
        response.put("recentTransactions", modules.get("finance-transactions").stream()
                .filter(item -> Objects.equals(item.get("userNo"), userNo))
                .sorted(Comparator.comparing(item -> Objects.toString(item.get("time"), ""), Comparator.reverseOrder()))
                .limit(5)
                .toList());
        return response;
    }

    public Map<String, Object> submitTransaction(String transactionKind, TransactionRequest request) {
        Map<String, Object> account = accounts.get(request.userNo());
        if (account == null) {
            throw new IllegalArgumentException("用户账户不存在");
        }
        BigDecimal currentBalance = new BigDecimal(Objects.toString(account.get("balance"), "0"));
        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        if ("withdraw".equals(transactionKind) && currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("余额不足，无法办理取款");
        }
        BigDecimal nextBalance = "deposit".equals(transactionKind) ? currentBalance.add(amount) : currentBalance.subtract(amount);
        account.put("balance", number(nextBalance));
        account.put("availableBalance", number(nextBalance));

        String serialNo = "TX" + sequence.incrementAndGet();
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", serialNo);
        record.put("serialNo", serialNo);
        record.put("userNo", request.userNo());
        record.put("userName", account.get("name"));
        record.put("type", "deposit".equals(transactionKind) ? "存款" : "取款");
        record.put("amount", number(amount));
        record.put("operator", request.operator());
        record.put("time", now());
        record.put("status", "成功");
        record.put("remark", request.remark() == null ? "系统办理" : request.remark());
        record.put("warning", amount.compareTo(WARNING_THRESHOLD) >= 0 ? "大额预警" : "正常");
        modules.get("finance-transactions").add(0, record);
        modules.get("admin-analysis").add(0, Map.of(
                "id", "ANA" + sequence.incrementAndGet(),
                "分析主题", record.get("type") + "经营同步",
                "结果", record.get("amount") + " 元",
                "说明", "用户 " + record.get("userName") + " 完成 " + record.get("type"),
                "时间", record.get("time")
        ));
        modules.get("user-" + ("deposit".equals(transactionKind) ? "deposits" : "withdrawals")).add(0, new LinkedHashMap<>(record));
        modules.get("admin-security").add(0, Map.of(
                "id", "AUD" + sequence.incrementAndGet(),
                "日志类型", "业务操作",
                "操作人", request.operator(),
                "模块", "deposit".equals(transactionKind) ? "存款办理" : "取款办理",
                "内容", "办理" + record.get("type") + "，流水号 " + serialNo,
                "时间", record.get("time"),
                "风险等级", amount.compareTo(WARNING_THRESHOLD) >= 0 ? "高" : "低"
        ));
        if (amount.compareTo(WARNING_THRESHOLD) >= 0 || "withdraw".equals(transactionKind)) {
            modules.get("admin-security").add(0, Map.of(
                    "id", "WAR" + sequence.incrementAndGet(),
                    "日志类型", "风险预警",
                    "操作人", request.operator(),
                    "模块", "风险预警中心",
                    "内容", "用户 " + account.get("name") + " 触发 " + record.get("warning"),
                    "时间", record.get("time"),
                    "风险等级", amount.compareTo(WARNING_THRESHOLD) >= 0 ? "高" : "中"
            ));
        }
        return Map.of(
                "serialNo", serialNo,
                "userNo", request.userNo(),
                "userName", account.get("name"),
                "type", record.get("type"),
                "amount", number(amount),
                "balance", number(nextBalance),
                "warning", record.get("warning"),
                "message", "业务办理成功，已自动更新余额与流水"
        );
    }

    public Map<String, Object> exportModule(String role, String module) {
        return Map.of(
                "module", moduleTitle(role, module),
                "message", "已生成 Excel、PDF 与打印凭证任务，可用于演示导出打印流程",
                "time", now()
        );
    }

    private void initBaseData() {
        accounts.put("U1001", buildAccount("U1001", "李玉兰", new BigDecimal("85600.00"), "正常", "低"));
        accounts.put("U1002", buildAccount("U1002", "王建国", new BigDecimal("125000.00"), "正常", "中"));
        accounts.put("U1003", buildAccount("U1003", "赵秋梅", new BigDecimal("46800.00"), "冻结", "高"));

        modules.put("admin-admin-accounts", listOf(
                row("id", "ADM1001", "账号", "admin", "姓名", "系统管理员", "角色", "超级管理员", "状态", "启用", "最近登录", now(), "权限级别", "全部权限"),
                row("id", "ADM1002", "账号", "shenji", "姓名", "审计管理员", "角色", "安全审计员", "状态", "启用", "最近登录", now(), "权限级别", "审计权限")
        ));
        modules.put("admin-finance-staff", listOf(
                row("id", "FIN1001", "工号", "CW001", "姓名", "张会计", "性别", "女", "手机号", "13800001111", "岗位", "柜面财务", "所属网点", "中心网点", "状态", "在职"),
                row("id", "FIN1002", "工号", "CW002", "姓名", "刘出纳", "性别", "男", "手机号", "13800002222", "岗位", "报销审核", "所属网点", "北郊网点", "状态", "在职")
        ));
        modules.put("admin-users", listOf(
                row("id", "USR1001", "userNo", "U1001", "name", "李玉兰", "gender", "女", "phone", "13900001111", "idCard", "4101********0001", "address", "郑州高新区", "status", "正常", "registerTime", now(), "balance", number(new BigDecimal("85600")), "riskLevel", "低"),
                row("id", "USR1002", "userNo", "U1002", "name", "王建国", "gender", "男", "phone", "13900002222", "idCard", "4101********0002", "address", "开封兰考", "status", "正常", "registerTime", now(), "balance", number(new BigDecimal("125000")), "riskLevel", "中"),
                row("id", "USR1003", "userNo", "U1003", "name", "赵秋梅", "gender", "女", "phone", "13900003333", "idCard", "4101********0003", "address", "南阳邓州", "status", "冻结", "registerTime", now(), "balance", number(new BigDecimal("46800")), "riskLevel", "高")
        ));
        modules.put("admin-dictionaries", listOf(
                row("id", "DIC1001", "字典类型", "存款类型", "字典编码", "DEPOSIT_TYPE", "字典值", "定期存款", "状态", "启用"),
                row("id", "DIC1002", "字典类型", "取款类型", "字典编码", "WITHDRAW_TYPE", "字典值", "现金取款", "状态", "启用"),
                row("id", "DIC1003", "字典类型", "报销类型", "字典编码", "REIMBURSE_TYPE", "字典值", "农资采购", "状态", "启用")
        ));
        modules.put("admin-announcements", listOf(
                row("id", "ANN1001", "标题", "关于春耕专项资金发放安排的通知", "类型", "制度公告", "可见范围", "全部用户", "状态", "已发布", "发布时间", now(), "阅读人数", 182),
                row("id", "ANN1002", "标题", "报销票据上传规范更新", "类型", "业务公告", "可见范围", "财务人员", "状态", "已发布", "发布时间", now(), "阅读人数", 36)
        ));
        modules.put("admin-messages", listOf(
                row("id", "MSG1001", "留言人", "李玉兰", "类型", "咨询", "内容", "报销附件上传失败怎么办？", "状态", "未回复", "提交时间", now()),
                row("id", "MSG1002", "留言人", "王建国", "类型", "投诉", "内容", "取款等待时间偏长", "状态", "处理中", "提交时间", now())
        ));
        modules.put("admin-reimbursements", listOf(
                row("id", "RB1001", "单号", "BX20260401001", "申请人", "李玉兰", "报销类型", "农资采购", "金额", "1860.00", "状态", "待审核", "提交时间", now(), "审核轨迹", "待财务人员"),
                row("id", "RB1002", "单号", "BX20260401002", "申请人", "王建国", "报销类型", "差旅报销", "金额", "980.00", "状态", "已通过", "提交时间", now(), "审核轨迹", "管理员终审通过")
        ));
        modules.put("admin-analysis", listOf(
                row("id", "ANA1001", "分析主题", "本月净流入", "结果", "较上月增长 12.6%", "说明", "主要来自春耕专项补贴发放", "时间", now()),
                row("id", "ANA1002", "分析主题", "重点客户变化", "结果", "中风险客户增加 2 户", "说明", "建议复核最近大额交易", "时间", now())
        ));
        modules.put("admin-security", listOf(
                row("id", "SEC1001", "日志类型", "登录日志", "操作人", "admin", "模块", "系统登录", "内容", "管理员登录成功", "时间", now(), "风险等级", "低"),
                row("id", "SEC1002", "日志类型", "异常预警", "操作人", "系统", "模块", "风险预警中心", "内容", "检测到大额取款预警 3 条", "时间", now(), "风险等级", "高")
        ));

        modules.put("finance-balances", new ArrayList<>(accounts.values()));
        modules.put("finance-transactions", listOf(
                row("id", "TX1001", "serialNo", "TX1001", "userNo", "U1001", "userName", "李玉兰", "type", "存款", "amount", "6000.00", "operator", "张会计", "time", now(), "status", "成功", "remark", "春耕补贴到账", "warning", "正常"),
                row("id", "TX1002", "serialNo", "TX1002", "userNo", "U1002", "userName", "王建国", "type", "取款", "amount", "52000.00", "operator", "刘出纳", "time", now(), "status", "成功", "remark", "购置农机支出", "warning", "大额预警")
        ));
        modules.put("finance-reimbursements", new ArrayList<>(modules.get("admin-reimbursements")));
        modules.put("finance-announcements", new ArrayList<>(modules.get("admin-announcements")));
        modules.put("finance-messages", new ArrayList<>(modules.get("admin-messages")));
        modules.put("finance-analysis", listOf(
                row("id", "REP1001", "报表名称", "月度收支汇总报表", "统计区间", "2026-04-01 至 2026-04-07", "数据摘要", "存款增长明显，取款平稳", "导出状态", "已生成"),
                row("id", "REP1002", "报表名称", "报销审核统计报表", "统计区间", "2026-04-01 至 2026-04-07", "数据摘要", "待审核 3 单，异常 1 单", "导出状态", "可导出")
        ));

        modules.put("user-profile", listOf(
                row("id", "UP1001", "用户名", "user", "姓名", "李玉兰", "手机号", "13900001111", "地址", "郑州高新区", "账户状态", "正常", "实名信息", "已实名")
        ));
        modules.put("user-balance", listOf(
                row("id", "BAL1001", "账户编号", "U1001", "当前余额", "85600.00", "冻结余额", "0.00", "可用余额", "85600.00", "最近变动", "春耕补贴到账")
        ));
        modules.put("user-deposits", listOf(
                row("id", "UD1001", "serialNo", "TX1001", "type", "存款", "amount", "6000.00", "operator", "张会计", "time", now(), "status", "成功", "remark", "春耕补贴到账")
        ));
        modules.put("user-withdrawals", listOf(
                row("id", "UW1001", "serialNo", "TX1002", "type", "取款", "amount", "1200.00", "operator", "刘出纳", "time", now(), "status", "成功", "remark", "农资采购")
        ));
        modules.put("user-reimbursements", listOf(
                row("id", "UR1001", "单号", "BX20260401001", "报销类型", "农资采购", "金额", "1860.00", "状态", "待审核", "审核意见", "等待财务人员处理")
        ));
        modules.put("user-announcements", new ArrayList<>(modules.get("admin-announcements")));
        modules.put("user-messages", new ArrayList<>(modules.get("admin-messages")));
        modules.put("user-notifications", listOf(
                row("id", "NT1001", "通知类型", "审核结果通知", "标题", "报销申请已进入审核", "时间", now(), "状态", "未读"),
                row("id", "NT1002", "通知类型", "公告通知", "标题", "春耕专项资金发放安排已发布", "时间", now(), "状态", "已读")
        ));
    }

    private Map<String, Object> buildAdminDashboard() {
        return Map.of(
                "metrics", List.of(
                        metric("今日存款总额", "126,800", "元", "+12.5%"),
                        metric("今日取款总额", "78,300", "元", "-5.2%"),
                        metric("今日净流入", "48,500", "元", "+18.1%"),
                        metric("本月累计存款", "1,268,000", "元", "+10.4%"),
                        metric("本月累计取款", "926,000", "元", "+3.6%"),
                        metric("用户总数", "368", "户", "+6"),
                        metric("财务人员总数", "18", "人", "+1"),
                        metric("异常预警数", "5", "条", "+2")
                ),
                "trend", getAnalytics("admin"),
                "todo", List.of("待审核报销 3 单", "待处理投诉 2 条", "本周需复核大额取款 4 笔"),
                "notices", modules.get("admin-announcements"),
                "warnings", List.of("账户 U1002 连续取款频繁", "赵秋梅账户状态冻结待复核", "异常报销预警 1 条")
        );
    }

    private Map<String, Object> buildFinanceDashboard() {
        return Map.of(
                "metrics", List.of(
                        metric("今日存款笔数", "12", "笔", "+2"),
                        metric("今日取款笔数", "9", "笔", "-1"),
                        metric("今日经办金额", "205,600", "元", "+16.2%"),
                        metric("待审核报销", "3", "单", "+1"),
                        metric("待处理留言", "2", "条", "持平"),
                        metric("风险预警", "2", "条", "+1")
                ),
                "trend", getAnalytics("finance"),
                "recentRecords", modules.get("finance-transactions"),
                "todo", List.of("完成春耕专项资金批量入账", "审核本周待处理报销", "处理用户留言与咨询")
        );
    }

    private Map<String, Object> buildUserDashboard() {
        return Map.of(
                "metrics", List.of(
                        metric("当前余额", "85,600", "元", "可用余额"),
                        metric("本月收入", "16,200", "元", "+8.3%"),
                        metric("本月支出", "8,600", "元", "-3.1%"),
                        metric("待处理申请", "1", "项", "报销待审核")
                ),
                "recentTransactions", modules.get("finance-transactions").stream()
                        .filter(item -> Objects.equals(item.get("userNo"), "U1001"))
                        .toList(),
                "quickLinks", List.of("余额查询", "报销申请", "公告查看", "AI 智能问答"),
                "notifications", modules.get("user-notifications"),
                "trend", getAnalytics("user")
        );
    }

    private Map<String, Object> buildLoginProfile(String role, String roleName, String name, String homePath, String badge) {
        return Map.of(
                "token", "token-" + role,
                "user", Map.of(
                        "username", role,
                        "name", name,
                        "role", role,
                        "roleName", roleName,
                        "badge", badge,
                        "homePath", homePath
                )
        );
    }

    private Map<String, Object> buildAccount(String userNo, String name, BigDecimal balance, String status, String riskLevel) {
        return row(
                "id", userNo,
                "accountNo", userNo,
                "userNo", userNo,
                "name", name,
                "balance", number(balance),
                "frozenBalance", "0.00",
                "availableBalance", number(balance),
                "status", status,
                "riskLevel", riskLevel,
                "lastUpdateTime", now()
        );
    }

    private List<Map<String, Object>> inferColumns(List<Map<String, Object>> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        return records.get(0).keySet().stream()
                .map(key -> {
                    Map<String, Object> column = new LinkedHashMap<>();
                    column.put("prop", key);
                    column.put("label", key);
                    return column;
                })
                .toList();
    }

    private Map<String, Object> buildModuleSummary(String role, String module, List<Map<String, Object>> records) {
        return Map.of(
                "total", records.size(),
                "module", moduleTitle(role, module),
                "updatedAt", now(),
                "keywords", List.of("全局搜索", "导出打印", "审核留痕", "风险预警")
        );
    }

    private String moduleTitle(String role, String module) {
        Map<String, String> titles = new HashMap<>();
        titles.put("admin-accounts", "管理员账号与权限管理");
        titles.put("finance-staff", "财务人员管理");
        titles.put("users", "用户管理");
        titles.put("dictionaries", "字典与基础配置管理");
        titles.put("announcements", "公告管理");
        titles.put("messages", "留言与反馈管理");
        titles.put("reimbursements", "报销与审核");
        titles.put("analysis", role.equals("finance") ? "报表分析与可视化分析" : "财务总览与经营分析");
        titles.put("security", "系统安全与审计");
        titles.put("balances", "余额查询");
        titles.put("transactions", "统一交易流水");
        titles.put("profile", "个人信息管理");
        titles.put("balance", "余额查询");
        titles.put("deposits", "存款记录查询");
        titles.put("withdrawals", "取款记录查询");
        titles.put("notifications", "我的消息中心");
        return titles.getOrDefault(module, "业务模块");
    }

    private String moduleDescription(String role, String module) {
        return switch (role + "-" + module) {
            case "admin-admin-accounts" -> "支持管理员账号、角色、权限菜单、日志审计与状态控制。";
            case "admin-finance-staff" -> "覆盖工号、岗位、网点、账号绑定、批量导入导出与业务权限分配。";
            case "admin-users" -> "支持账户余额、交易记录、黑名单、重点客户和风险等级管理。";
            case "admin-security" -> "覆盖登录日志、操作日志、风险预警、会话策略与密码策略。";
            case "finance-balances" -> "提供冻结余额、可用余额、最近交易、风险标记与余额导出。";
            case "finance-transactions" -> "统一展示存款流水、取款流水、经办人、金额与风险状态。";
            case "user-balance" -> "展示当前余额、冻结余额、可用余额和资金变动摘要。";
            case "user-reimbursements" -> "支持新建报销申请、撤回申请、查看审核状态与审核意见。";
            default -> "该模块已接入中文表单、列表、导出打印、审核流与风险预警演示数据。";
        };
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    private String number(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Map<String, Object> metric(String label, String value, String unit, String trend) {
        return Map.of("label", label, "value", value, "unit", unit, "trend", trend);
    }

    @SafeVarargs
    private final List<Map<String, Object>> listOf(Map<String, Object>... rows) {
        return new ArrayList<>(List.of(rows));
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put(String.valueOf(values[index]), values[index + 1]);
        }
        return row;
    }

    private String key(String role, String module) {
        return role + "-" + module;
    }
}
