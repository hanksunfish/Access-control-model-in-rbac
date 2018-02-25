package com.mmall.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysDeptMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysDept;
import com.mmall.param.DeptParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.IpUtil;
import com.mmall.util.LevelUtil;

@Service
public class SysDeptService {

	@Resource
	private SysDeptMapper sysDeptMapper;

	public void save(DeptParam param) {
		BeanValidator.check(param);
		if (checkExist(param.getParentId(), param.getName(), param.getId())) {
			throw new ParamException("同一层级下存在相同名称的部门");
		}
		SysDept dept = SysDept.builder().name(param.getName()).parentId(param.getParentId()).seq(param.getSeq())
				.remark(param.getRemark()).build();
		// level存储 0.1.2
		dept.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
		dept.setOperator(RequestHolder.getCurrentUser().getUsername());
		dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
		dept.setOperateTime(new Date());
		// 直插入有值的值
		sysDeptMapper.insertSelective(dept);
	}

	public void update(DeptParam param) {
		BeanValidator.check(param);
		if (checkExist(param.getParentId(), param.getName(), param.getId())) {
			throw new ParamException("同一层级下存在相同名称的部门");
		}
		// 检查更新的部门是否存在
		SysDept before = sysDeptMapper.selectByPrimaryKey(param.getId());
		// TODO:
		Preconditions.checkNotNull(before, "待更新的部门不存在");
		if (checkExist(param.getParentId(), param.getName(), param.getId())) {
			throw new ParamException("同一层级下存在相同名称的部门");
		}
		SysDept after = SysDept.builder().id(param.getId()).name(param.getName()).parentId(param.getParentId())
				.seq(param.getSeq()).remark(param.getRemark()).build();
		after.setLevel(LevelUtil.calculateLevel(getLevel(param.getParentId()), param.getParentId()));
		after.setOperator(RequestHolder.getCurrentUser().getUsername());
		after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
		after.setOperateTime(new Date());
		updateWithChild(before, after);
	}

	@Transactional
	public void updateWithChild(SysDept before, SysDept after) {
		String newLevelPrefix = after.getLevel();
		String oldLevelPrefix = before.getLevel();
		if (!after.getLevel().equals(before.getLevel())) {
			// 更新子部门的level
			List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
			if (CollectionUtils.isNotEmpty(deptList)) {
				for (SysDept dept : deptList) {
					String level = dept.getLevel();
					if (level.indexOf(oldLevelPrefix) == 0) {
						// 该子部门的level前缀为 oldLevelPrefix
						// 生成新的level
						level = newLevelPrefix + level.substring(oldLevelPrefix.length());
						dept.setLevel(level);
					}
				}
				// 批量更新level
				sysDeptMapper.batchUpdateLevel(deptList);
			}
		}
		sysDeptMapper.updateByPrimaryKey(after);
	}

	/**
	 * 判断数据是否有重复
	 * 
	 * @param parentId
	 * @param deptName
	 * @param deptId
	 * @return
	 */
	private boolean checkExist(Integer parentId, String deptName, Integer deptId) {
		// TODO:
		return sysDeptMapper.countByNameAndParentId(parentId, deptName, deptId) > 0;
	}

	private String getLevel(Integer deptId) {
		SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
		if (dept == null) {
			return null;
		}
		return dept.getLevel();
	}

}
