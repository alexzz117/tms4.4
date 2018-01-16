/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.dao.ConditionUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;

/**
 * 角色管理实现类
 * 
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Transactional
@Service("cmcRoleService")
public class RoleService {

	@Autowired
	private SimpleDao cmcSimpleDao;

	public void setCmcSimpleDao(SimpleDao cmcSimpleDao) {
		this.cmcSimpleDao = cmcSimpleDao;
	}

	/**
	 * 分页查询角色
	 * 
	 * @param conds
	 *            条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> listRole(Map<String, String> conds) {
		String sql = "select * from " + DBConstant.CMC_ROLE;
		conds.put("ROLE_NAME", ConditionUtil.like(conds.get("role_name")));
		String where = ConditionUtil.and(conds,
				new String[][] { { "like", "ROLE_NAME", "ROLE_NAME" }, { "=", "FLAG", "flag" }, });
		sql += ConditionUtil.where(where);
		Order order = new Order().asc("ROLE_ID");// 默认用主键排序
		return cmcSimpleDao.pageQuery(sql, conds, order);
	}

	/**
	 * 更新一个角色记录
	 * 
	 * @param role
	 *            角色信息（Map<列名称, 列值>）
	 */
	public void updateRole(Map<String, Object> role) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put("ROLE_ID", role.get("ROLE_ID"));

		cmcSimpleDao.update(DBConstant.CMC_ROLE, role, conds);

	}

	/**
	 * 删除一个角色记录；
	 * 
	 * @param roleId
	 *            角色ID
	 */
	public void deleteRole(String roleId) {
		cmcSimpleDao.delete(DBConstant.CMC_OPERATOR_ROLE_REL, MapWrap.map("ROLE_ID", roleId).getMap());
		cmcSimpleDao.delete(DBConstant.CMC_ROLE_FUNC_REL, MapWrap.map("ROLE_ID", roleId).getMap());
		cmcSimpleDao.delete(DBConstant.CMC_ROLE, MapWrap.map("ROLE_ID", roleId).getMap());
	}

	/**
	 * 新建一个角色记录
	 * 
	 * @param role
	 *            角色信息（Map<列名称, 列值>）
	 * @return 角色信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createRole(Map<String, Object> role) {
		role.put("ROLE_ID", Stringz.randomUUID().toUpperCase());
		cmcSimpleDao.create(DBConstant.CMC_ROLE, role);

		return role;
	}

	/**
	 * 获取一个角色记录
	 * 
	 * @param roleId
	 *            角色ID
	 * @return 角色信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getRole(String roleId) {
		return cmcSimpleDao.retrieve(DBConstant.CMC_ROLE, MapWrap.map("ROLE_ID", roleId).getMap());
	}

	/**
	 * 获取角色权限功能树
	 * 
	 * @param roleId
	 *            角色ID
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public List<Map<String, Object>> getFuncsForGrant(String roleId) {

		String grantedFuncsSql = "SELECT * FROM " + DBConstant.CMC_ROLE_FUNC_REL + " WHERE ROLE_ID = ? ";
		String funcsSql = "SELECT * FROM " + DBConstant.CMC_FUNC
				+ " WHERE FLAG='1' AND (FUNC_TYPE in('0','1') OR (FUNC_TYPE = '2' AND GRANT_FLAG='1')) ";

		List<Map<String, Object>> allFuncs = cmcSimpleDao.queryForList(funcsSql);
		List<Map<String, Object>> grantedFuncs = cmcSimpleDao.queryForList(grantedFuncsSql, roleId);
		Set<String> funcIds = new HashSet<String>();
		for (Map<String, Object> grantedFunc : grantedFuncs) {
			funcIds.add((String) grantedFunc.get("FUNC_ID"));
		}
		for (Map<String, Object> func : allFuncs) {
			String funcId = (String) func.get("FUNC_ID");
			if (funcIds.contains(funcId)) {
				func.put("GRANT", "1");
			} else {
				func.put("GRANT", "0");
			}
		}
		return allFuncs;
	}

	/**
	 * 修改角色权限
	 * 
	 * @param roleId
	 *            角色ID
	 * @param funcIds
	 *            数组，角色对应的权限ID
	 */
	public void updateRoleGrant(String roleId, ArrayList<String> funcIds) {
		cmcSimpleDao.delete(DBConstant.CMC_ROLE_FUNC_REL, MapWrap.map("ROLE_ID", roleId).getMap());
		if (funcIds != null && funcIds.size() > 0) {
			for (int i = 0; i < funcIds.size(); i++) {
				String f = (String) funcIds.get(i);
				if (f.equals(""))
					break;
				Map<String, String> role_fun = new HashMap<String, String>();
				role_fun.put("ROLE_ID", roleId);
				role_fun.put("FUNC_ID", f);
				cmcSimpleDao.create(DBConstant.CMC_ROLE_FUNC_REL, role_fun);
			}
		}
	}

	/**
	 * 按角色名称获取角色列表
	 * 
	 * @param conds
	 *            条件参数 （Map<列名称, 列值>）
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public boolean listRoleName(Map<String, String> conds) {
		String sql = "select * from " + DBConstant.CMC_ROLE + " where 1=1";
		if (conds.get("role_name") != null && !"".equals(conds.get("role_name"))) {
			sql += " and role_name = ? ";
		}
		List<Map<String, Object>> list = cmcSimpleDao.queryForList(sql, conds.get("role_name"));
		if (list != null && list.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 查询全部角色信息
	 */
	public List<Map<String, Object>> listAllRole(Map<String, String> conds) {
		String sql = "select * from cmc_role";
		return cmcSimpleDao.queryForList(sql);
	}

	/**
	 * 查询未停用角色信息
	 */
	public List<Map<String, Object>> listNormalRole(Map<String, String> conds) {
		String sql = "select * from cmc_role t where t.flag !='0'";
		return cmcSimpleDao.queryForList(sql);
	}

	/**
	 * 判断角色是否被分配
	 */
	public boolean delRole(String roleId) {
		// String roleId = conds.get("roleId");
		String sql = "select * from CMC_OPERATOR_ROLE_REL where role_id = ?";
		List<Map<String, Object>> list = cmcSimpleDao.queryForList(sql, roleId);
		if (list != null && list.size() > 0) {
			return true;
		}
		return false;
	}
}
