package com.coopfinance.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {

    @Select("${sql}")
    List<Map<String, Object>> executeSelect(String sql);

}
