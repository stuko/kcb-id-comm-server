package com.kcb.id.comm.carrier.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarrierMapper {
	List<Map<String,Object>> selectTestMapper(Map<String,Object> parameter);
	int createTestTable(Map<String,Object> parameter);
	int insertTestData(Map<String,Object> parameter);
	int insertData(Map<String,Object> parameter);
}
