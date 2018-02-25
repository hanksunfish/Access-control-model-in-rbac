package com.mmall.param;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestVo {
	@NotBlank
	private String msg;
	@NotNull
	private Integer id;
	@NotEmpty
	private List<String> str;
}
