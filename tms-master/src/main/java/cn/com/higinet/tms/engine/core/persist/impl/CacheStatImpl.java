/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  CacheStatImpl.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 14:12:31   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.core.persist.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;

import cn.com.higinet.tms.engine.core.bean;
import cn.com.higinet.tms.engine.core.persist.Stat;
import cn.com.higinet.tms.engine.stat.stat_row;

/**
 * 统计值的持久化接口实现类，本实现类是直接缓存实现
 *
 * @ClassName:  CacheStatImpl
 * @author: 王兴
 * @date:   2017-8-21 14:12:31
 * @since:  v4.3
 */
public class CacheStatImpl implements Stat {
	private static final Logger log = LoggerFactory.getLogger( CacheStatImpl.class );

	public static final String STAT_CACHE_GROUP = "stat";

	private CacheManager cacheManager = bean.get( CacheManager.class );

	@Override
	public List<stat_row> queryList( List<stat_row> rows ) throws Exception {
		return null;
	}

	@Override
	public boolean delete( stat_row row ) throws Exception {
		return false;
	}

	@Override
	public void saveList( List<stat_row> rows ) throws Exception {}

	@Override
	public void backup( List<stat_row> rows ) throws Exception {}

}
