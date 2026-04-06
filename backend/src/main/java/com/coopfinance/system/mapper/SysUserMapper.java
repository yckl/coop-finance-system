package com.coopfinance.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface SysUserMapper {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND password = #{password}")
    Map<String, Object> login(String username, String password);
    
    @Update("UPDATE account_balance SET total_balance = #{balance}, available_balance = #{balance} WHERE user_no = #{userNo}")
    int updateAccountBalance(String userNo, java.math.BigDecimal balance);

    @Select("SELECT * FROM account_balance WHERE user_no = #{userNo}")
    Map<String, Object> getAccountBalance(String userNo);

    @org.apache.ibatis.annotations.Insert("INSERT INTO transaction_record (serial_no, user_no, user_name, transaction_type, business_type, amount, balance_after, operator_name, warning_level) " +
            "VALUES (#{serialNo}, #{userNo}, #{userName}, #{transactionType}, #{businessType}, #{amount}, #{balanceAfter}, #{operatorName}, #{warningLevel})")
    int insertTransactionRecord(Map<String, Object> params);
}
