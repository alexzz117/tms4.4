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

import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.ConditionUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;

/**
 * 字典管理实现类
 * @author chenr
 * @version 2.0.0, 2011-6-30
 * 
 * @author zhang.lei
 */
@Transactional
@Service("cmcCodeDictService")
public class CodeDictService {

	@Autowired
	@Qualifier("cmcSimpleDao")
	private SimpleDao cmcSimpleDao;
	
	/**
	 * 分页查询代码类别
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> listCodeCategory(Map<String, String> conds) {
		String sql  = "select * from "+DBConstant.CMC_CODE_CATEGORY;
		conds.put("CATEGORY_NAME", ConditionUtil.like(conds.get("CATEGORY_NAME")));
		conds.put("CATEGORY_ID", ConditionUtil.like(conds.get("CATEGORY_ID")));
		String where = ConditionUtil.and(conds, new String[][]{
				{"like", "CATEGORY_NAME", "CATEGORY_NAME"},
				{"like", "CATEGORY_ID", "CATEGORY_ID"}
		});
		sql += ConditionUtil.where(where);
		Order order = new Order();
		//默认用主键排序
		order.asc("CATEGORY_ID");
		return cmcSimpleDao.pageQuery(sql, conds, order);
	}
	/**
	 * 更新一个代码类别记录
	 * @param cc 代码类别信息（Map<列名称, 列值>）
	 */
	public void updateCodeCategory(Map<String, Object> cc) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put("CATEGORY_ID", cc.get("CATEGORY_ID"));
		cmcSimpleDao.update(DBConstant.CMC_CODE_CATEGORY, cc, conds);
	}
	/**
	 * 删除一个代码类别记录；
	 * 并删除该类别的所有代码记录
	 * @param ccId 代码类别ID
	 */
	public void deleteCodeCategory(String ccId) {
		String delSql = "delete from "+DBConstant.CMC_CODE +"  where category_id='"+ccId+"'";
		cmcSimpleDao.executeUpdate(delSql);
		cmcSimpleDao.delete(DBConstant.CMC_CODE_CATEGORY, MapWrap.map("CATEGORY_ID", ccId).getMap());
	}
	/**
	 * 新建一个代码类别记录
	 * @param cc 代码类别信息（Map<列名称, 列值>）
	 * @return 代码类别信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createCodeCategory(Map<String, Object> cc) {
		cmcSimpleDao.create(DBConstant.CMC_CODE_CATEGORY, cc);
		return cc;
	}
	/**
	 * 获取一个代码记录
	 * @param ccId 代码类别ID
	 * @return 代码类别信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getCodeCategory(String ccId) {
		return cmcSimpleDao.retrieve(DBConstant.CMC_CODE_CATEGORY, MapWrap.map("CATEGORY_ID", ccId).getMap());
	}
	/**
	 * 分页查询代码
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> listCode(Map<String, String> conds) {
		String sql  = "select code.CODE_ID, code.CODE_KEY,code.CODE_VALUE,code.ONUM,ca.CATEGORY_NAME,ca.CATEGORY_ID from "+DBConstant.CMC_CODE+" code,"+DBConstant.CMC_CODE_CATEGORY+" ca where 1=1 ";
		if(conds.size()>0 && conds.get("categoryId")!=null){
			sql+=" and code.CATEGORY_ID='"+conds.get("categoryId").trim()+"' and code.CATEGORY_ID=ca.CATEGORY_ID";
		}
		//TODO 补全condition
		Order order = new Order();
		//默认用主键排序
		order.asc("ONUM");
		return cmcSimpleDao.pageQuery(sql, conds, order);
	}
	/**
	 * 更新一个代码记录
	 * @param code 代码信息（Map<列名称, 列值>）
	 */
	public void updateCode(Map<String, Object> code) {
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put("CODE_ID", code.get("CODE_ID"));
		
		String onum = CmcMapUtil.getString(code, "ONUM");
		if(CmcStringUtil.isEmpty(onum)) code.remove("ONUM");
		
		cmcSimpleDao.update(DBConstant.CMC_CODE, code, conds);
	}
	/**
	 * 删除一个代码记录
	 * @param codeId 代码ID
	 */
	public void deleteCode(String codeId) {
		cmcSimpleDao.delete(DBConstant.CMC_CODE, MapWrap.map("CODE_ID", codeId).getMap());
	}
	/**
	 * 新建一个代码记录
	 * @param code 代码信息（Map<列名称, 列值>）
	 * @return 代码信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createCode(Map<String, Object> code) {
		code.put("CODE_ID", Stringz.randomUUID().toUpperCase());
		
		String onum = CmcMapUtil.getString(code, "ONUM");
		if(CmcStringUtil.isEmpty(onum)) code.remove("ONUM");
		
		cmcSimpleDao.create(DBConstant.CMC_CODE, code);
		return code;
	}
	/**
	 * 获取一个代码记录
	 * @param codeId 代码ID
	 * @return 代码信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> getCode(String codeId) {
		return cmcSimpleDao.retrieve(DBConstant.CMC_CODE, MapWrap.map("CODE_ID", codeId).getMap());
	}

	public boolean hasCateGoryId(String categoryId) {
		String sql = "select * from "+ DBConstant.CMC_CODE_CATEGORY +" where 1=1 ";
		if(null != categoryId && !"".equals(categoryId)){
			sql += "and category_id ='" + categoryId + "'";
		}
		List<Map<String, Object>> list = cmcSimpleDao.queryForList(sql);
		boolean flag = false;
		if(null != list && list.size() > 0){
			flag = true;
		}
		return flag;
	}
}
