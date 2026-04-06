package com.coopfinance.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE transaction_type = '存款' AND DATE(created_at) = CURDATE()")
    BigDecimal getTodayDepositTotal();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE transaction_type = '取款' AND DATE(created_at) = CURDATE()")
    BigDecimal getTodayWithdrawTotal();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE transaction_type = '存款' AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())")
    BigDecimal getMonthDepositTotal();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transaction_record WHERE transaction_type = '取款' AND MONTH(created_at) = MONTH(CURDATE()) AND YEAR(created_at) = YEAR(CURDATE())")
    BigDecimal getMonthWithdrawTotal();

    @Select("SELECT COUNT(*) FROM sys_user WHERE role_code = 'USER'")
    int getUserTotal();

    @Select("SELECT COUNT(*) FROM sys_user WHERE role_code = 'FINANCE'")
    int getFinanceStaffTotal();

    @Select("SELECT COUNT(*) FROM reimbursement_order WHERE current_status = '待审核'")
    int getPendingReimbursementsTotal();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE process_status = '未处理'")
    int getPendingRiskWarningsTotal();
}
