package com.mmall.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.CobarShowStatus;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.AclDto;
import com.mmall.dto.AclModuleLevelDto;
import com.mmall.dto.DeptLevelDto;
import com.mmall.model.SysAcl;
import com.mmall.model.SysAclModule;
import com.mmall.model.SysDept;
import com.mmall.util.LevelUtil;

@Service
public class SysTreeService {

	@Resource
	private SysDeptMapper sysDeptMapper;
	@Resource
    private SysAclModuleMapper sysAclModuleMapper;
	@Resource
	private SysCoreService sysCoreService;
	@Resource
	private SysAclMapper sysAclMapper;

	/*public List<AclModuleLevelDto> userAclTree(int userId) {
        List<SysAcl> userAclList = sysCoreService.getUserAclList(userId);
        List<AclDto> aclDtoList = Lists.newArrayList();
        for (SysAcl acl : userAclList) {
            AclDto dto = AclDto.adapt(acl);
            dto.setHasAcl(true);
            dto.setChecked(true);
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }*/
	/*------------生成权限树------------------*/
	
	public List<AclModuleLevelDto> roleTree(int roleId) {
        // 1、当前用户已分配的权限点
        List<SysAcl> userAclList = sysCoreService.getCurrentUserAclList();
        // 2、当前角色分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        
        List<AclDto> aclDtoList = Lists.newArrayList();
        Set<Integer> userAclIdSet = userAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        Set<Integer> roleAclIdSet = roleAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        // 3、当前系统所有权限点  需求：前端会展示系统所有权限
        List<SysAcl> allAclList = sysAclMapper.getAll();
        for (SysAcl acl : allAclList) {
            AclDto dto = AclDto.adapt(acl);
            if (userAclIdSet.contains(acl.getId())) {
                dto.setHasAcl(true);
            }
            if (roleAclIdSet.contains(acl.getId())) {
                dto.setChecked(true);
            }
            aclDtoList.add(dto);
        }
        return aclListToTree(aclDtoList);
    }
	/**
	 * 构建权限树结构
	 * @param aclDtoList
	 * @return
	 */
	public List<AclModuleLevelDto> aclListToTree(List<AclDto> aclDtoList) {
        if (CollectionUtils.isEmpty(aclDtoList)) {
            return Lists.newArrayList();
        }
        // 构建权限模块树结构
        List<AclModuleLevelDto> aclModuleLevelList = aclModuleTree();

        Multimap<Integer, AclDto> moduleIdAclMap = ArrayListMultimap.create();
        // 获取系统所有有效权限
        for(AclDto acl : aclDtoList) {
            if (acl.getStatus() == 1) {
                moduleIdAclMap.put(acl.getAclModuleId(), acl);
            }
        }
        // 权限绑定到权限模块树
        bindAclsWithOrder(aclModuleLevelList, moduleIdAclMap);
        return aclModuleLevelList;
    }
	/**
	 * 递归绑定权限到权限模块树
	 * @param aclModuleLevelList
	 * @param moduleIdAclMap
	 */
	public void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelList, Multimap<Integer, AclDto> moduleIdAclMap) {
        if (CollectionUtils.isEmpty(aclModuleLevelList)) {
            return;
        }
        for (AclModuleLevelDto dto : aclModuleLevelList) {
            List<AclDto> aclDtoList = (List<AclDto>)moduleIdAclMap.get(dto.getId());
            if (CollectionUtils.isNotEmpty(aclDtoList)) {
                Collections.sort(aclDtoList, aclSeqComparator);
                dto.setAclList(aclDtoList);
            }
            bindAclsWithOrder(dto.getAclModuleList(), moduleIdAclMap);
        }
    }
	/*------------------------------------------*/
	
	
	/**
	 * 通过level属性构建树，可以参考 deptTree() 一模一样
	 * @return
	 */
	public List<AclModuleLevelDto> aclModuleTree() {
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for (SysAclModule aclModule : aclModuleList) {
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Lists.newArrayList();
        }
        // level -> [aclmodule1, aclmodule2, ...] Map<String, List<Object>>
        Multimap<String, AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();

        for (AclModuleLevelDto dto : dtoList) {
            levelAclModuleMap.put(dto.getLevel(), dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        Collections.sort(rootList, aclModuleSeqComparator);
        transformAclModuleTree(rootList, LevelUtil.ROOT, levelAclModuleMap);
        return rootList;
    }

    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for (int i = 0; i < dtoList.size(); i++) {
            AclModuleLevelDto dto = dtoList.get(i);
            String nextLevel = LevelUtil.calculateLevel(level, dto.getId());
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if (CollectionUtils.isNotEmpty(tempList)) {
                Collections.sort(tempList, aclModuleSeqComparator);
                dto.setAclModuleList(tempList);
                transformAclModuleTree(tempList, nextLevel, levelAclModuleMap);
            }
        }
    }
    
    
    /**
     * 根据level属性 构建树
     * @return
     */
	public List<DeptLevelDto> deptTree() {
		List<SysDept> deptList = sysDeptMapper.getAllDept();
		ArrayList<DeptLevelDto> dtoList = Lists.newArrayList();
		for (SysDept dept : deptList) {
			DeptLevelDto dto = DeptLevelDto.adapt(dept);
			dtoList.add(dto);
		}
		return deptListToTree(dtoList);
	}

	private List<DeptLevelDto> deptListToTree(ArrayList<DeptLevelDto> deptLevelList) {
		if (CollectionUtils.isEmpty(deptLevelList)) {
			return Lists.newArrayList();
		}
		Multimap<String, DeptLevelDto> levelDeptMap = ArrayListMultimap.create();
		// level == 0 部门
		List<DeptLevelDto> rootList = Lists.newArrayList();
		for (DeptLevelDto dto : deptLevelList) {
			levelDeptMap.put(dto.getLevel(), dto);
			if (LevelUtil.ROOT.equals(dto.getLevel())) {
				rootList.add(dto);
			}
		}
		// 按照seq从小到大排序
		Collections.sort(rootList, new Comparator<DeptLevelDto>() {
			@Override
			public int compare(DeptLevelDto o1, DeptLevelDto o2) {
				return o1.getSeq() - o2.getSeq();
			}
		});
		// 递归生成树
		transformDeptTree(rootList, LevelUtil.ROOT, levelDeptMap);
		return rootList;
	}

	private void transformDeptTree(List<DeptLevelDto> deptLevelList, String level,
			Multimap<String, DeptLevelDto> levelDeptMap) {
		for (int i = 0; i < deptLevelList.size(); i++) {
			// 遍历该层的每一个节点
			DeptLevelDto deptLevelDto = deptLevelList.get(i);
			// 处理当前层级的数据 nextLevel -> level.deptId
			String nextLevel = LevelUtil.calculateLevel(level, deptLevelDto.getId());
			// 处理下一层
			List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>) levelDeptMap.get(nextLevel);
			if (CollectionUtils.isNotEmpty(tempDeptList)) {
				// 排序
				Collections.sort(tempDeptList, deptSeqComparator);
				// 设置下一层部门
				deptLevelDto.setDeptList(tempDeptList);
				// 进入到下一层处理
				transformDeptTree(tempDeptList, nextLevel, levelDeptMap);
			}
		}

	}

	private Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
		@Override
		public int compare(DeptLevelDto o1, DeptLevelDto o2) {
			return o1.getSeq() - o2.getSeq();
		}
	};
	public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

}
