package com.wk.server.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * List<String> 与逗号分隔字符串互转：数据库只存一列 varchar，接口层暴露数组。
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

	@Override
	public String convertToDatabaseColumn(List<String> list) {
		return list == null || list.isEmpty() ? null : String.join(",", list);
	}

	@Override
	public List<String> convertToEntityAttribute(String dbValue) {
		return dbValue == null || dbValue.isBlank() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(dbValue.split(",")));
	}

}
