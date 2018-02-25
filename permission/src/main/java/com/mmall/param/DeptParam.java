package com.mmall.param;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * model相对应接受的参数，有时候直接用model对象接受这些参数
 * 
 * @author Administrator
 *
 */
@Getter
@Setter
@ToString
public class DeptParam {

	private Integer id;
	@NotNull(message = "不能名称不能为空")
	@Length(max = 15, min = 2, message = "不能名称长度在2-15字符之间")
	private String name;
	private Integer parentId = 0;
	@NotNull(message = "展示顺序不可以为空")
	private Integer seq;
	@Length(max = 150, message = "备注长度在150字之内")
	private String remark;
}
