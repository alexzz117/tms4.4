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

import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 系统服务实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Transactional
@Service("cmcSysService")
public class SysService {

	/**
	 * 默认设置操作员ID为1
	 */
	private final static String ADMIN_ID = "1";

	@Autowired
	private SimpleDao cmcSimpleDao;

	public void setCmcSimpleDao( SimpleDao cmcSimpleDao ) {
		this.cmcSimpleDao = cmcSimpleDao;
	}

	/**
	 * 取得操作员的信息
	 * @param username 用户名
	 * @param password 密码
	 * @return 用户记录信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getOperator( String username, String password ) {
		//		Map<String, Object> operator = cmcSimpleDao.retrieve(DBConstant.CMC_OPERATOR, 
		//				MapWrap.map("login_name", username)
		//				.put("password", password).getMap());
		Map<String, Object> operator;
		String sql = "select * from cmc_operator where login_name=? and flag<>'9'";
		List<Map<String, Object>> list = cmcSimpleDao.queryForList( sql, username );
		if( list.size() == 0 ) {
			return operator = null;
		}
		operator = list.get( 0 );
		//operator.remove("PASSWORD");
		return operator;
	}

	/**
	 * 获取操作员的菜单信息
	 * @param operatorId 操作员ID
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public List<Map<String, Object>> getOperatorMenu( String operatorId ) {
		//TODO SQL 要移动到外部配置文件
		//TODO 替换数据字典代码
		String sql = "";
		List<Map<String, Object>> funclist;
		if( ADMIN_ID.equals( operatorId ) ) {
			sql = "select f.func_id, func_name, func_type, parent_id, onum, conf from cmc_func f " + " where f.flag = '1' and f.menu = '1' and f.func_type = '2' "
					+ " order by f.onum asc";
			funclist = cmcSimpleDao.queryForList( sql );
		}
		else {
			sql = "select distinct f.func_id, func_name, func_type, parent_id, onum, conf from cmc_func f " + " left join cmc_role_func_rel r " + " on f.func_id = r.func_id "
					+ " where f.flag = '1' and f.menu = '1' and f.func_type = '2' and role_id in ( " + " select role_id from cmc_operator_role_rel where operator_id = ? ) "
					+ " order by f.onum asc";
			funclist = cmcSimpleDao.queryForList( sql, operatorId );
		}
		sql = "select func_id, func_name, func_type, parent_id, onum, conf " + " from cmc_func f where f.func_type in ('0','1') and f.menu = '1' "
				+ " order by f.func_type desc, f.onum asc";
		List<Map<String, Object>> nodelist = cmcSimpleDao.queryForList( sql );
		Set<String> pids = new HashSet<String>();
		//		List<Map<String, Object>> menulist = new ArrayList<Map<String,Object>>();
		for( Map<String, Object> func : funclist ) {
			String pid = (String) func.get( "PARENT_ID" );
			pids.add( pid );
		}
		//顺序很重要
		for( Map<String, Object> node : nodelist ) {
			String id = (String) node.get( "FUNC_ID" );
			String pid = (String) node.get( "PARENT_ID" );
			if( pids.contains( id ) ) {
				funclist.add( node );
				pids.add( pid );
			}
		}
		return funclist;
	}

	/**
	 * 获取操作员功能ID信息
	 * @param operatorId 操作员ID
	 * @return 数组，包含操作员功能ID信息
	 */
	public String[] getOperatorFuncIds( String operatorId ) {
		String sql = "";
		List<Map<String, Object>> funclist;
		Set<String> funcIds = new HashSet<String>();
		if( ADMIN_ID.equals( operatorId ) ) {
			sql = "select f.func_id from cmc_func f " + " where f.flag = '1' and f.func_type = '2' ";
			funclist = cmcSimpleDao.queryForList( sql );
		}
		else {
			//加载不需要授权的功能ID
			sql = "select func_id from cmc_func where grant_flag = '0'";
			funclist = cmcSimpleDao.queryForList( sql );
			for( int i = 0; i < funclist.size(); i++ ) {
				Map<String, Object> func = funclist.get( i );
				funcIds.add( (String) func.get( "FUNC_ID" ) );
			}
			sql = "select distinct func_id from cmc_role_func_rel rfr " + " left join cmc_operator_role_rel orr " + " on rfr.role_id = orr.role_id "
					+ " where orr.operator_id = ? ";
			funclist = cmcSimpleDao.queryForList( sql, operatorId );
		}
		for( int i = 0; i < funclist.size(); i++ ) {
			Map<String, Object> func = funclist.get( i );
			funcIds.add( (String) func.get( "FUNC_ID" ) );
		}
		return funcIds.toArray( new String[0] );
	}

	public boolean checkRole( String operatorId ) {
		String sql = "select r.flag from cmc_role r where r.role_id in(select t.role_id from cmc_operator_role_rel t where t.operator_id ='" + operatorId + "')";
		Map<String, Object> list = cmcSimpleDao.queryForList( sql ).get( 0 );
		String flag = (String) list.get( "flag" );
		if( flag == "0" || "0".equals( flag ) ) {
			return false;
		}
		return true;
	}
}
