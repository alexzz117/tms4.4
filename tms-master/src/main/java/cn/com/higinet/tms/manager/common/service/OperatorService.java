/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.base.entity.common.Page;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;

/**
 * 操作员实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
//TODO:需要考虑事务传播机制
//和事务等级以提高性能
@Transactional
@Service("cmcOperatorService")
public class OperatorService {

	@Autowired
	@Qualifier("cmcSimpleDao")
	private SimpleDao cmcSimpleDao;
	
	/**
	 *  获取操作员列表
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> listOperator( Map<String, String> conds ) {
		//TODO: SQL 语句需要放到外部的配额文件中
		//TODO: 过滤字段，不是所有的字段都返回到前端
		String sql = "select op.*,s.role_name from CMC_OPERATOR op left join cmc_operator_role_rel m on m.operator_id=op.operator_id left join cmc_role s on m.role_id=s.role_id where op.operator_id!='1' and op.flag!='9' ";
		if( conds.get( "login_name" ) != null && !"".equals( conds.get( "login_name" ) ) ) {
			sql += " and op.LOGIN_NAME like '%" + conds.get( "login_name" ) + "%'";
		}
		if( conds.get( "real_name" ) != null && !"".equals( conds.get( "real_name" ) ) ) {
			sql += " and op.REAL_NAME like '%" + conds.get( "real_name" ) + "%'";
		}
		if( conds.get( "flag" ) != null && !"".equals( conds.get( "flag" ) ) ) {
			sql += " and op.FLAG ='" + conds.get( "flag" ) + "'";
		}
		if( conds.get( "role" ) != null && !"".equals( conds.get( "role" ) ) ) {
			sql += " and s.ROLE_ID ='" + conds.get( "role" ) + "'";
		}
		Order order = new Order().asc( "op.OPERATOR_ID" );//默认用主键排序
		return cmcSimpleDao.pageQuery( sql, conds, order );
	}

	/**
	 * 根据条件查询操作员列表
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> searchListOperator( Map<String, String> conds ) {
		String sql = "select * from " + DBConstant.CMC_OPERATOR + " op where op.operator_id!='1' and op.flag!='9' ";
		if( conds.get( "login_name" ) != null && !"".equals( conds.get( "login_name" ) ) ) {
			sql += " and op.login_name like '%" + conds.get( "login_name" ) + "%'";
		}
		if( conds.get( "flag" ) != null && !"".equals( conds.get( "flag" ) ) ) {
			sql += " and op.flag='" + conds.get( "flag" ) + "'";
		}
		if( conds.get( "real_name" ) != null && !"".equals( conds.get( "real_name" ) ) ) {
			sql += " and op.real_name ='" + conds.get( "real_name" ) + "' ";
		}
		Order order = new Order();
		return cmcSimpleDao.pageQuery( sql, conds, order );
	}

	/**
	 * 更新操作员
	 * @param operator 操作员信息（Map<列名称, 列值>）
	 */
	public void updateOperator( Map<String, Object> operator ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		String operatorId = (String) operator.get( "OPERATOR_ID" );
		conds.put( "OPERATOR_ID", operatorId );
		String role = (String) operator.get( "ROLE" );
		if( role != null && !"".equals( role ) ) {
			String sql = "update CMC_OPERATOR_ROLE_REL set ROLE_ID = ? where OPERATOR_ID = ?";
			cmcSimpleDao.executeUpdate( sql, role, operatorId );
		}
		operator.remove( "ROLE" );
		operator.remove( "ROLE_ID" );
		cmcSimpleDao.update( DBConstant.CMC_OPERATOR, operator, conds );
	}

	/**
	 * 删除选中的操作员,假删,将操作员的状态改为9(若以后操作员的状态要扩展，可以保证与当前状态值的连续性,9为最终状态)
	 * @param operatorId 操作员ID
	 */
	public void deleteOperator( String operatorId ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		Map<String, Object> opr = getOperator( operatorId );
		opr.remove( "ROLE" );
		opr.put( "FLAG", "9" );
		conds.put( "OPERATOR_ID", operatorId );
		cmcSimpleDao.delete( DBConstant.CMC_OPERATOR_ROLE_REL, MapWrap.map( "OPERATOR_ID", operatorId ).getMap() );//删除与角色的关联关系
		cmcSimpleDao.update( DBConstant.CMC_OPERATOR, opr, conds );
	}

	/**
	 * 创建操作员
	 * @param operator 操作员信息（Map<列名称, 列值>）
	 * @return 操作员信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createOperator( Map<String, Object> reqs ) {
		Map<String, Object> operator = new HashMap<String, Object>();
		Map<String, Object> role = new HashMap<String, Object>();
		String id = Stringz.randomUUID().toUpperCase();
		operator.put( "OPERATOR_ID", id );
		operator.put( "LOGIN_NAME", reqs.get( "login_name" ) );
		operator.put( "PASSWORD", reqs.get( "password" ) );
		operator.put( "FLAG", reqs.get( "flag" ) );
		operator.put( "REAL_NAME", reqs.get( "real_name" ) );
		operator.put( "PHONE", reqs.get( "phone" ) );
		operator.put( "MOBILE", reqs.get( "mobile" ) );
		operator.put( "EMAIL", reqs.get( "email" ) );
		operator.put( "ADDRESS", reqs.get( "address" ) );
		operator.put( "MEMO", reqs.get( "memo" ) );
		operator.put( "CONF", "pagesize:25;maxpanel:0" );
		operator.put( "CREDENTIALTYPE", reqs.get( "credentialtype" ) );
		operator.put( "CREDENTIALNUM", reqs.get( "credentialnum" ) );
		role.put( "ROLE_ID", reqs.get( "role" ) );
		role.put( "OPERATOR_ID", id );
		//		System.out.println(reqs.get("role"));
		cmcSimpleDao.create( DBConstant.CMC_OPERATOR, operator );
		cmcSimpleDao.create( "CMC_OPERATOR_ROLE_REL", role );
		return operator;
	}

	/**
	 * 获取操作员对象
	 * @param operatorId 操作员ID
	 * @return 操作员信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getOperator( String operatorId ) {
		Map<String, Object> map = cmcSimpleDao.retrieve( DBConstant.CMC_OPERATOR, MapWrap.map( "OPERATOR_ID", operatorId ).getMap() );
		if( !map.isEmpty() ) {
			String sql = "select * from CMC_OPERATOR_ROLE_REL where operator_id = ?";
			List<Map<String, Object>> list = cmcSimpleDao.queryForList( sql, operatorId );
			map.put( "ROLE", list.size() > 0 ? list.get( 0 ).get( "ROLE_ID" ) : "" );
		}
		return map;
	}

	/**
	 * 获取操作员未分配角色列表
	 * @param operatorId
	 * @return
	 */
	public List<Map<String, Object>> listOperatorNotAssignRole( String operatorId ) {
		String sql = "select role.ROLE_ID,role.ROLE_NAME from " + DBConstant.CMC_ROLE + " role " + " where role.flag = '1' and role.role_id not in "
				+ " (select o.role_id from CMC_OPERATOR_ROLE_REL o where o.operator_id=? )";
		return cmcSimpleDao.queryForList( sql, operatorId );
	}

	/**
	 * 获取操作员已分配角色列表
	 * @param operatorId 操作员ID
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public List<Map<String, Object>> listOperatorAssignRole( String operatorId ) {
		String sql = "select role.ROLE_ID,role.ROLE_NAME from " + DBConstant.CMC_ROLE + " role " + " where role.role_id  in "
				+ " (select o.role_id from CMC_OPERATOR_ROLE_REL o where o.operator_id=? )";
		return cmcSimpleDao.queryForList( sql, operatorId );
	}

	/**
	 * 删除为操作员已分配的角色
	 * @param operatorId 
	 */
	public void delAssignOperator( String operatorId ) {
		cmcSimpleDao.delete( DBConstant.CMC_OPERATOR_ROLE_REL, MapWrap.map( "OPERATOR_ID", operatorId ).getMap() );
	}

	/**
	 * 为操作员分配角色
	 * @param operatorId 操作员ID
	 * @param roleIds 数组，角色ID
	 */
	public void grantOperator( String operatorId, String[] roleIds ) {
		cmcSimpleDao.executeUpdate( "delete from " + DBConstant.CMC_OPERATOR_ROLE_REL + " where operator_id = ? ", operatorId );
		if( roleIds.length > 0 ) {
			String[][] rows = new String[roleIds.length][2];
			for( int i = 0; i < roleIds.length; i++ ) {
				rows[i][0] = operatorId;
				rows[i][1] = roleIds[i];
			}
			cmcSimpleDao.batchUpdate( "insert into " + DBConstant.CMC_OPERATOR_ROLE_REL + " (operator_id, role_id) values(?, ?)", rows );
		}
	}

	/**
	 * 获取用户是否存在
	 * @param userName 用户名
	 */
	public boolean hasUserName( String userName ) {
		String sql = "select * from " + DBConstant.CMC_OPERATOR + " where flag<>'9' ";
		if( userName != null && !"".equals( userName ) ) {
			sql += " and login_name ='" + userName + "'";
		}
		List<Map<String, Object>> list = cmcSimpleDao.queryForList( sql );
		boolean flag = false;
		if( list != null && list.size() > 0 ) {
			flag = true;
		}
		return flag;
	}

	public boolean resetPwd( String operatorId, String password ) {
		boolean flag = true;
		String sql = "update CMC_OPERATOR set password = ? where operator_id = ?";
		int i = cmcSimpleDao.executeUpdate( sql, password, operatorId );
		if( i == 0 ) {
			flag = false;
		}
		return flag;
	}

	public Map<String, Object> getRole( String roleid ) {
		return cmcSimpleDao.retrieve( DBConstant.CMC_ROLE, MapWrap.map( DBConstant.CMC_ROLE_ID, roleid ).getMap() );

	}

	/**
	 * 获得所有用户，用于查询下拉框
	 * @return
	 */
	public List<Map<String, Object>> getUsers() {
		String sql = " select OPERATOR_ID,LOGIN_NAME,REAL_NAME from " + DBConstant.CMC_OPERATOR + " where flag!='9' and operator_id !='1'";
		return cmcSimpleDao.queryForList( sql );
	}
	/**
	 * 解锁用户，用于登录失败后发生的锁定状态
	 * @return
	 */
	public boolean resetLoginFailedAttempts(String operatorId) {
		boolean flag = true;
		String sql = "update CMC_OPERATOR set flag = 1 where operator_id = ? and flag = 0";
		int i = cmcSimpleDao.executeUpdate( sql, operatorId );
		if( i == 0 ) {
			flag = false;
		}
		return flag;
	}
}
