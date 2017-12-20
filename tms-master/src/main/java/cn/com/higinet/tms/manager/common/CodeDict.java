/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import cn.com.higinet.tms.base.entity.common.DictCategory;
import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 代码字典实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 */
@Service("codeDict")
public class CodeDict {

	@Autowired
	@Qualifier("cmcSimpleDao")
	private SimpleDao cmcSimpleDao;

	/**
	 * 获取一个代码类别下代码键的值
	 * @param category 代码类别
	 * @param code 代码键
	 * @return 键值
	 */
	public String getCode( String category, String code ) {
		String sql = "select * from cmc_code where category_id=? and code_key=? order by ONUM";
		List<Map<String, Object>> codelist = cmcSimpleDao.queryForList( sql, category, code );
		if( codelist.size() > 0 ) {
			return (String) codelist.get( 0 ).get( "CODE_VALUE" );
		}
		return null;
	}

	/**
	 * 获取一个代码类别下所有代码
	 * @param category 代码类别
	 * @return 存放代码的键值对的Map对象
	 */
	public Map<String, String> getCodes( String category ) {
		String sql = "select * from cmc_code where category_id=? order by ONUM";
		List<Map<String, Object>> codelist = cmcSimpleDao.queryForList( sql, category );
		Map<String, String> codemap = new HashMap<String, String>();
		if( codelist.size() > 0 ) {
			for( Map<String, Object> code : codelist ) {
				codemap.put( (String) code.get( "CODE_KEY" ), (String) code.get( "CODE_VALUE" ) );
			}
		}
		return codemap;
	}

	/**
	 * 获取所有代码定义
	 * @return 存放代码的键值对的Map对象
	 */
	public Map<String, DictCategory> getAllCodes() {
		String sql = "SELECT * FROM CMC_CODE_CATEGORY";
		List<Map<String, Object>> categorylist = cmcSimpleDao.queryForList( sql );
		sql = "SELECT * FROM CMC_CODE ORDER BY CATEGORY_ID, ONUM";
		List<Map<String, Object>> codelist = cmcSimpleDao.queryForList( sql );

		Map<String, DictCategory> codes0 = new HashMap<String, DictCategory>();
		for( Map<String, Object> category : categorylist ) {
			String categoryId = (String) category.get( "CATEGORY_ID" );
			String categoryName = (String) category.get( "CATEGORY_NAME" );
			String categoryInfo = (String) category.get( "INFO" );

			DictCategory dict = new DictCategory();
			dict.setId( categoryId );
			dict.setName( categoryName );
			dict.setInfo( categoryInfo );
			dict.setItems( new ArrayList<Map<String, Object>>() );

			codes0.put( categoryId, dict );
		}
		for( Map<String, Object> code : codelist ) {
			String categoryId = (String) code.get( "CATEGORY_ID" );
			String codeKey = (String) code.get( "CODE_KEY" );
			String codeValue = (String) code.get( "CODE_VALUE" );

			Map<String, Object> item = Maps.newHashMap();
			item.put( "value", codeKey );
			item.put( "label", codeValue );

			if( codes0.containsKey( categoryId ) ) {
				codes0.get( categoryId ).getItems().add( item );
			}
		}
		return codes0;
	}

}
