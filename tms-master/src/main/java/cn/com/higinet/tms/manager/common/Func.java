/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 功能表的配置对象；
 * 在功能修改时应该通知该类进行更新；
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Service("cmcFunc")
public class Func {

	private long lastmodify = -1L;
	private Object lock = new Object();

	// 缓存住功能URI数据
	private Map<String, Set<String>> uriMap = new HashMap<String, Set<String>>();
	private Set<String> enableFuncIds = new HashSet<String>();

	@Autowired
	@Qualifier("cmcSimpleDao")
	private SimpleDao cmcSimpleDao;

	/**
	 * 检查URI是否被权限保护
	 * @param uri URI地址
	 * @return 是否保护，（true被保护，false没被保护）
	 */
	public boolean isProtectedUri( String uri ) {
		if( this.lastmodify < 0 ) reload();
		synchronized (lock) {
			return uriMap.containsKey( uri );
		}
	}

	/**
	 * 获取指定URI对应的功能编号（正常状态的功能）
	 * @param uri URI地址
	 * @return 数组，URI对应的功能编号
	 */
	public String[] getEnableFuncIdsByUri( String uri ) {
		if( this.lastmodify < 0 ) reload();
		synchronized (lock) {
			if( this.uriMap.containsKey( uri ) ) {
				Set<String> funcIds = this.uriMap.get( uri );
				List<String> list = new ArrayList<String>();
				for( String funcId : funcIds ) {
					if( this.enableFuncIds.contains( funcId ) ) {
						list.add( funcId );
					}
				}
				return list.toArray( new String[0] );
			}
		}
		return new String[0];
	}

	/**
	 * 最后重载时间
	 * @return long型毫秒时间
	 */
	public long lastModify() {
		return this.lastmodify;
	}

	/**
	 * 重载方法，刷新功能URI数据
	 * @return void
	 */
	public void reload() {
		String sql = "SELECT * FROM " + DBConstant.CMC_FUNC + " WHERE GRANT_FLAG = '1' AND (FUNC_TYPE='2' OR FUNC_TYPE='3') ";
		List<Map<String, Object>> list = this.cmcSimpleDao.queryForList( sql );
		Map<String, Set<String>> urimap0 = new HashMap<String, Set<String>>();
		Set<String> enableFuncIds0 = new HashSet<String>();

		for( Map<String, Object> func : list ) {
			String type = (String) func.get( "FUNC_TYPE" );
			if( !"2".equals( type ) && !"3".equals( type ) ) {
				continue;
			}
			String conf = (String) func.get( "CONF" );
			conf = conf == null ? "" : conf;
			String id = (String) func.get( "FUNC_ID" );
			String pid = (String) func.get( "PARENT_ID" );

			String[] uris = conf.split( "," );
			for( String uri : uris ) {
				Set<String> funcIds;
				if( urimap0.containsKey( uri ) ) {
					funcIds = urimap0.get( uri );
				}
				else {
					funcIds = new HashSet<String>();
					urimap0.put( uri, funcIds );
				}
				if( "2".equals( type ) ) {
					funcIds.add( id );
					if( "1".equals( (String) func.get( "FLAG" ) ) ) {
						enableFuncIds0.add( id );
					}
				}
				else {//子功能并入功能
					funcIds.add( pid );
				}
			}
		}

		synchronized (lock) {
			this.uriMap = urimap0;
			this.lastmodify = System.currentTimeMillis();
			this.enableFuncIds = enableFuncIds0;
		}
	}

	/**
	 * 根据uri获取对应的功能模块对象
	 * @param uri URI地址
	 * @param method 调用方式
	 * @return 功能模块信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getFuncByUri( String uri, String method ) {
		String methodType = "0"; //GET
		if( "POST".equalsIgnoreCase( method ) ) {
			methodType = "1";
		}
		String sql = "SELECT * FROM " + DBConstant.CMC_FUNC + " WHERE LOG_FLAG='1' and  LOG_URI='" + uri + "' and LOG_METHOD='" + methodType + "'";
		List<Map<String, Object>> templist = cmcSimpleDao.queryForList( sql );
		if( templist == null || templist.size() < 1 ) {
			Map<String, Object> map = new HashMap<String, Object>();
			return map;
		}
		else {
			return templist.get( 0 );
		}
	}
}
