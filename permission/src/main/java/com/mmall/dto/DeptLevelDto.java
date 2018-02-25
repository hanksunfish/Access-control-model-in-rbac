package com.mmall.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.google.common.collect.Lists;
import com.mmall.model.SysDept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeptLevelDto extends SysDept {

	/**
	 * 部门树结构
	 */
	private List<DeptLevelDto> deptList = Lists.newArrayList();

	/**
	 * 适配
	 * 
	 * @param dept
	 * @return
	 */
	public static DeptLevelDto adapt(SysDept dept) {
		DeptLevelDto dto = new DeptLevelDto();
		BeanUtils.copyProperties(dept, dto);
		return dto;
	}
}
