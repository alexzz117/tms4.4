/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.service.FuncService;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 功能管理实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Transactional
@Service("cmcFuncService")
public class FuncService {

	@Autowired
	private SimpleDao cmcSimpleDao;

	/**
	 * 获取功能树列表
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 记录行的值列表 （列表中存放 Map<列名称, 列值>）
	 */
	public List<Map<String, Object>> listAllFuncs( Map<String, String> conds ) {
		//TODO 组合条件
		String sql = "select * from " + DBConstant.CMC_FUNC;
		sql += " where 1=1 ";
		if( conds != null && !conds.isEmpty() ) {
			if( !CmcStringUtil.isBlank( conds.get( "parentId" ) ) ) {
				sql += " and " + DBConstant.CMC_FUNC_FUNC_PARENTID + " in (" + conds.get( "parentId" ) + ")";
			}
		}
		sql += " order by " + DBConstant.CMC_FUNC_FUNC_ONUM;
		return cmcSimpleDao.queryForList( sql, conds );

	}

	/**
	 * 通过parentId获取map，然后放入list中
	 * @param 
	 * @return 
	 */
	private List<Map<String, Object>> listAllFuncVal( Map<String, String> conds, List<Map<String, Object>> list ) {

		List<Map<String, Object>> listVal = new ArrayList<Map<String, Object>>();
		int sum = 0;
		if( conds != null && !conds.isEmpty() ) {
			for( int i = 0; i < list.size(); i++ ) {
				Object obj = list.get( i ).get( "PARENT_ID" );
				if( obj != null && obj.equals( conds.get( "parentId" ) ) ) {
					listVal.add( sum, list.get( i ) );
					sum++;
				}
			}
		}
		return listVal;
	}

	/**
	 * 遍历list，获取功能名，进行处理，放入funcOptList中
	 * @param 
	 * @return 
	 */
	private void getOptList( String parentId, List<Map<String, Object>> funcOptList, List<Map<String, Object>> list ) {
		Map<String, String> conds = new HashMap<String, String>();
		conds.put( "parentId", parentId );
		List<Map<String, Object>> funcList = listAllFuncVal( conds, list );
		if( funcList != null && funcList.size() > 0 ) {
			for( int i = 0; i < funcList.size(); i++ ) {
				Map<String, Object> func = funcList.get( i );
				String functype = CmcStringUtil.objToString( func.get( DBConstant.CMC_FUNC_FUNC_TYPE ) );
				if( "1".equals( functype ) ) {
					func.put( DBConstant.CMC_FUNC_FUNC_NAME, "-" + func.get( DBConstant.CMC_FUNC_FUNC_NAME ) );
				}
				else if( "2".equals( functype ) ) {
					func.put( DBConstant.CMC_FUNC_FUNC_NAME, "--" + func.get( DBConstant.CMC_FUNC_FUNC_NAME ) );
				}
				else if( "3".equals( functype ) ) {
					func.put( DBConstant.CMC_FUNC_FUNC_NAME, "---" + func.get( DBConstant.CMC_FUNC_FUNC_NAME ) );
				}
				funcOptList.add( func );
				parentId = (String) func.get( DBConstant.CMC_FUNC_FUNC_ID );
				getOptList( parentId, funcOptList, list );
			}
		}
	}

	/**
	 * 输出所有功能表，并对其进行排序
	 * @param 
	 * @return 
	 */
	public void listAllFuncsSort( List<Map<String, Object>> funcOptList ) {

		String sql = "select func_id, func_name, func_type, parent_id, onum " + " from CMC_FUNC t order by func_type, onum  asc";
		List<Map<String, Object>> list = cmcSimpleDao.queryForList( sql );
		if( list != null && list.size() > 0 ) {
			getOptList( "-1", funcOptList, list );
		}
	}

	/**
	 * 修改一个功能
	 * @param func 功能信息（Map<列名称, 列值>）
	 */
	public void updateFunc( Map<String, Object> func ) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put( "FUNC_ID", func.get( "FUNC_ID" ) );
		//是否需要从func 中去除主键信息
		cmcSimpleDao.update( DBConstant.CMC_FUNC, func, conds );
	}

	/**
	 * 删除一个功能
	 * @param funcId 功能ID
	 * @return 是否成功（true成功 ，false失败)
	 */
	public boolean deleteFunc( String funcId ) {
		boolean flag = true;
		//获取所有已授权的功能ID
		StringBuffer funIdStr = new StringBuffer();
		String funrolesql = "select distinct FUNC_ID from " + DBConstant.CMC_ROLE_FUNC_REL;
		List<Map<String, Object>> funcrole = cmcSimpleDao.queryForList( funrolesql );
		for( Map<String, Object> fr : funcrole ) {
			funIdStr.append( fr.get( "FUNC_ID" ) + "," );
		}
		if( !funIdStr.toString().contains( funcId ) ) {//判断传入的功能是否已授权
			//oracle数据库递归查询
			//			String funsql = "select func.func_id from  "+DBConstant.CMC_FUNC+" func start with func_id='"+funcId+"' connect by prior func_id = parent_id";
			//			List<Map<String, Object>>  flist = cmcSimpleDao.queryForList(funsql);
			//查询传入功能及其子孙记录
			List<Map<String, Object>> flist = new ArrayList<Map<String, Object>>();
			String sql = "select func.FUNC_ID from " + DBConstant.CMC_FUNC + " func where func.parent_id in(?)";
			List<Map<String, Object>> tlist = cmcSimpleDao.queryForList( sql, funcId );//查询传入功能的直接子记录
			//循环查询子孙记录，知道记录为空
			boolean tf = true;
			while (tf) {
				if( tlist == null || tlist.size() < 1 ) {
					tf = false;
				}
				else {
					String tId = "";
					for( Map<String, Object> tl : tlist ) {
						tId += "'" + tl.get( "FUNC_ID" ) + "',";
						flist.add( tl );
					}
					tId = tId.substring( 0, tId.length() - 1 );
					String tsql = "select func.FUNC_ID from " + DBConstant.CMC_FUNC + " func where func.parent_id in(" + tId + ")";
					tlist = cmcSimpleDao.queryForList( tsql );
				}
			}
			//遍历功能是否被授权，同时将功能Id拼装
			String delfunc = "'" + funcId + "',";
			for( Map<String, Object> fun : flist ) {
				if( funIdStr.toString().contains( fun.get( "FUNC_ID" ).toString() ) ) {
					flag = false;
					break;
				}
				delfunc += "'" + fun.get( "FUNC_ID" ).toString() + "',";
			}
			//传入的功能及其子孙记录若都没有被授权，删除
			if( flag ) {
				delfunc = delfunc.substring( 0, delfunc.length() - 1 );
				int t = 0;
				String delsql = "delete from " + DBConstant.CMC_FUNC + " where func_id in (" + delfunc + ")";
				t = cmcSimpleDao.executeUpdate( delsql );
				if( t == 0 ) {
					flag = false;
				}
			}
		}
		else {
			flag = false;
		}
		return flag;
	}

	/**
	 * 增加一个功能
	 * @param func 功能信息（Map<列名称, 列值>）
	 * @return 功能信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createFunc( Map<String, Object> func ) {
		func.put( "FUNC_ID", Stringz.randomUUID().toUpperCase() );
		cmcSimpleDao.create( DBConstant.CMC_FUNC, func );
		return func;
	}

	/**
	 * 获取一个功能的信息
	 * @param funcId 功能ID
	 * @return 功能信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getFunc( String funcId ) {
		String sql = "select * from CMC_FUNC t where t.func_id = ?";
		return cmcSimpleDao.queryForList( sql, funcId ).get( 0 );
	}

	/**
	 * 修改功能的状态为禁用，包括子功能
	 * @param funcId 功能ID
	 * @return 是否成功（true成功 ，false失败)
	 */
	public boolean modSubFuncs( String funcId ) {
		String fids = "'" + funcId + "'";
		//查询传入功能及其子孙记录
		String sql = "select func_id from " + DBConstant.CMC_FUNC + " where parent_id in(" + fids + ")";
		List<String> flist = new ArrayList<String>();
		List<Map<String, Object>> tlist = cmcSimpleDao.queryForList( sql );//查询传入功能的直接子记录
		//循环查询子孙记录，知道记录为空
		boolean tf = true;
		int max = 0;
		while (tf && max < 10) {
			max++;
			if( tlist == null || tlist.size() < 1 ) {
				tf = false;
			}
			else {
				fids = "";
				for( Map<String, Object> tl : tlist ) {
					fids += "'" + tl.get( "FUNC_ID" ) + "'" + ",";
					flist.add( tl.get( "FUNC_ID" ).toString() );
				}
				fids = fids.substring( 0, fids.length() - 1 );
				sql = "select func_id from " + DBConstant.CMC_FUNC + " where parent_id in(" + fids + ")";
				tlist = cmcSimpleDao.queryForList( sql );
			}
		}
		String modFuncs = "";
		for( String s : flist ) {
			modFuncs += "'" + s + "'" + ",";
		}
		int t = 1;
		if( !isEmpty( modFuncs ) ) {
			modFuncs = modFuncs.substring( 0, modFuncs.length() - 1 );
			String modsql = "update " + DBConstant.CMC_FUNC + " set flag='0' where func_id in (" + modFuncs + ")";
			t = cmcSimpleDao.executeUpdate( modsql );
		}
		return t == 0 ? false : true;
	}

	/**
	 * 修改功能的状态为正常，包括父功能
	 * @param funcId 功能ID
	 * @return 是否成功（true成功 ，false失败)
	 */
	public boolean modParentFuncs( String funcId ) {
		String sql = "select parent_id from " + DBConstant.CMC_FUNC + " where func_id ='" + funcId + "'";
		List<String> flist = new ArrayList<String>();
		List<Map<String, Object>> tlist = cmcSimpleDao.queryForList( sql );//查询传入功能的直接子记录
		boolean tf = true;
		int max = 0;
		while (tf && max < 10) {
			max++;
			if( tlist == null || tlist.size() < 1 ) {
				tf = false;
			}
			else {
				funcId = tlist.get( 0 ).get( "PARENT_ID" ).toString();
				flist.add( funcId );
				sql = "select parent_id from " + DBConstant.CMC_FUNC + " where func_id ='" + funcId + "'";
				tlist = cmcSimpleDao.queryForList( sql );
			}
		}
		String modFuncs = "";
		for( String s : flist ) {
			modFuncs += "'" + s + "'" + ",";
		}
		int t = 1;
		if( !isEmpty( modFuncs ) ) {
			modFuncs = modFuncs.substring( 0, modFuncs.length() - 1 );
			String modsql = "update " + DBConstant.CMC_FUNC + " set flag='1' where func_id in (" + modFuncs + ")";
			t = cmcSimpleDao.executeUpdate( modsql );
		}
		return t == 0 ? false : true;
	}

	/**
	 * 字符串判断是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty( String str ) {
		return str == null || "".equals( str.trim() ) ? true : false;
	}

	public String delFuncName( String funcId ) {
		String sql = "SELECT FUNC_NAME FROM CMC_FUNC WHERE FUNC_ID = '" + funcId + "'";
		List<Map<String, Object>> list = cmcSimpleDao.queryForList( sql );
		String funcName = (String) list.get( 0 ).get( "FUNC_NAME" );
		if( !CmcStringUtil.isEmpty( funcName ) ) return funcName;
		return null;
	}
}
