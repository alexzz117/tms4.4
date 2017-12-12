package cn.com.higinet.tms35.manage.mgr.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.cmc.common.DBConstant;
import cn.com.higinet.cmc.util.MapUtil;
import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.mgr.service.CodeDictService;


@Transactional
@Service("tmsCodeDictService")
public class CodeDictServiceImpl implements CodeDictService {

	@Autowired
	private SimpleDao cmcSimpleDao;

	public List<Map<String, Object>> listCode(Map<String, String> conds) {
		
		String sql  = "select CODE_ID, CODE_KEY, CODE_VALUE  from "+DBConstant.CMC_CODE+" where 1=1 ";
		if(conds.get("categoryId")==null || conds.size()< 1) return null;
		
		sql+=" and CATEGORY_ID='"+conds.get("categoryId")+"' ";
		sql+=" order by ONUM";
		return cmcSimpleDao.queryForList(sql);
	}
	

	/**
	 * 根据类别(支持多个)查询代码集
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listCodes(Map<String, String> conds) {
		
		String sql  = "select CODE_ID, CODE_KEY, CODE_VALUE,CATEGORY_ID from "+DBConstant.CMC_CODE+" where 1=1 ";
		if(conds!=null){
			if( conds.get("categoryId")!=null &&  !conds.get("categoryId").equals("")){
				sql+=" and CATEGORY_ID in ("+conds.get("categoryId")+") ";
			}
		} 		
		Order order = new Order();
		//默认用主键排序
		order.asc("ONUM");
		
		return cmcSimpleDao.queryForList(sql);
	}

	/**
	 * 根据类别查询代码分类集
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listCodeCateGorp(Map<String, String> conds) {
		String sql = "select * from " + DBConstant.CMC_CODE_CATEGORY + " where 1=1 ";
		String categoryIds = MapUtil.getString(conds, "categoryId");
		if(!StringUtil.isBlank(categoryIds)){
			sql += " and CATEGORY_ID in (" + categoryIds + ")";
		}
		return cmcSimpleDao.queryForList(sql);
	}
}
