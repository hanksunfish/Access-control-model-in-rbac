package com.mmall.controller;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.ApplicationContextHelper;
import com.mmall.common.JsonData;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysAclModule;
import com.mmall.param.TestVo;
import com.mmall.util.BeanValidator;
import com.mmall.util.JsonMapper;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/test")
@Slf4j
public class TestController {

	@RequestMapping("/hello")
	@ResponseBody
	public JsonData hello() {
		
		return JsonData.success("hello, permission");
	}
	@RequestMapping("/validate.json")
	@ResponseBody
	public JsonData validat2(TestVo vo) throws ParamException{
		SysAclModuleMapper aclModuleMapper = ApplicationContextHelper.popBean(SysAclModuleMapper.class);
		/*SysAclModule aclModule = aclModuleMapper.selectByPrimaryKey(1);
		System.out.println("查询");
		String string = JsonMapper.obj2String(aclModule);*/
		//log.info(string);
		BeanValidator.check(vo);
		return JsonData.success("test validate");
	}
}
