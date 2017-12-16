/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.stat.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.stat.service.StatService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;
import cn.com.higinet.tms35.comm.web_tool;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_stat;

/**
 * 功能/模块:
 * @author 张立群
 * @version 1.0  Apr 26, 2013
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Service("statService")
//@Transactional("tms")
public class StatServiceImpl implements StatService {
	
	@Autowired
	@Qualifier("officialTransactionManager")
	private DataSourceTransactionManager officialTransactionManager;
	
	@Autowired
	@Qualifier("tmpTransactionManager")
	private DataSourceTransactionManager tmpTransactionManager;
	
	@Autowired
	private CommonCheckService commonCheckService;
	
	@Autowired
	private TransDefService transDefService;
	
	@Autowired
	private SequenceService sequenceService;

	/**
	 * 区间分布，带参数
	 */
	private static final String RANG_BIN_DIST = "rang_bin_dist";

	private static Log log = LogFactory.getLog(StatServiceImpl.class);
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;

	@Autowired
	private SqlMap tmsSqlMap;
	
	
	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#statList(java.util.Map)
	 */
	public List<Map<String, Object>> statList(Map<String, Object> conds) {
		return tmsSimpleDao.queryForList("SELECT STAT_ID,STAT_NAME,STAT_DESC,STAT_TXN,STAT_PARAM,STAT_COND, STAT_COND_IN, RESULT_COND,STAT_DATAFD,STAT_FN,COUNUNIT,COUNTROUND,STAT_UNRESULT,DATATYPE,CONTINUES,STAT_VALID,FN_PARAM,'' MARK ,STORECOLUMN FROM TMS_COM_STAT WHERE STAT_TXN=:txnId ORDER BY STAT_NAME",conds);
	}


	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#txnFeatureList(java.lang.String)
	 */
	public List<Map<String, Object>> txnFeatureList(String txnId) {
		String sql = "select * from tms_com_fd where tab_name like '"+txnId+"%'";
		return tmsSimpleDao.queryForList(sql, new HashMap<String, Object>());
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#getOneStat(java.lang.String)
	 */
	public Map<String, Object> getOneStat(String statId) {
		return tmsSimpleDao.retrieve("TMS_COM_STAT", MapWrap.map(DBConstant.TMS_COM_STAT_STAT_ID, Long.parseLong(statId)).getMap());
	}


	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#codeList(java.lang.String[])
	 */
	public List<Map<String, Object>> codeList(String category_id,String[] conds) {
		String sql = "SELECT * FROM CMC_CODE_CATEGORY WHERE CATEGORY_ID = ?";
		List<Map<String,Object>> categoryList = tmsSimpleDao.queryForList(sql, category_id);
		if(categoryList != null && categoryList.size() > 0) {
			String category_sql = MapUtil.getString(categoryList.get(0),"CATEGORY_SQL");
			for (int i = 0; conds != null && i < conds.length; i++) {
				category_sql = category_sql.replace("${"+i+"}", conds[i]);
			}
			
			return tmsSimpleDao.queryForList(category_sql);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#updateValidStat(java.lang.String[])
	 */
	public void updateValidStat(String[] stat_ids,String stat_status) {

		String[] sqls = new String[stat_ids.length];;
		
		for (int i = 0; i < stat_ids.length; i++) {
			sqls[i] = "UPDATE TMS_COM_STAT SET STAT_VALID="+stat_status+" WHERE STAT_ID="+stat_ids[i];
		}
		
		tmsSimpleDao.batchUpdate(sqls);
		
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#delStat(java.lang.String[])
	 */
	public void delStat(String[] stat_ids) {
		String[] sqls = new String[stat_ids.length];;
		
		for (int i = 0; i < stat_ids.length; i++) {
			sqls[i] = "DELETE FROM TMS_COM_STAT WHERE STAT_ID="+stat_ids[i];
		}
		
		tmsSimpleDao.batchUpdate(sqls);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#getOneStatForCond(java.lang.String)
	 */
	public Map<String, Object> getOneStatForCond(String statId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT STAT.*,TAB.TAB_DESC,FUNC.FUNC_NAME,FD.NAME STAT_PARAM_CODENAME FROM TMS_COM_STAT STAT");
		sql.append(" LEFT JOIN TMS_COM_TAB TAB ON STAT.STAT_TXN=TAB.TAB_NAME");
		sql.append(" LEFT JOIN TMS_COM_FD FD ON STAT.STAT_TXN=FD.TAB_NAME AND STAT.STAT_PARAM=FD.REF_NAME");
		sql.append(" LEFT JOIN TMS_COM_FUNC FUNC ON STAT.STAT_FN=FUNC.FUNC_CODE");
		sql.append(" WHERE STAT.STAT_ID = ?");
		List<Map<String,Object>> statList = tmsSimpleDao.queryForList(sql.toString(), Long.parseLong(statId));
		for (Map<String, Object> map : statList) {
			return map;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#updateStatCond(java.util.Map)
	 */
	public void updateStatCond(Map<String, Object> input) {
		String sql = "UPDATE TMS_COM_STAT SET STAT_COND=:STAT_COND_VALUE WHERE STAT_ID=:STAT_ID";
		input.put("STAT_ID", MapUtil.getLong(input, "STAT_ID"));
		tmsSimpleDao.executeUpdate(sql, input);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#getTxnTree(java.lang.String)
	 */
	public List<Map<String, Object>> getTxnTree(Map<String,Object> tab) {
		//String sql = tmsSqlMap.getSql("tms.stat.queryTxnTree");
		String sql = "select distinct(tab_name),parent_tab,tab_desc from tms_com_tab tab where tab.tab_name in (:tab_name) order by tab_name";
		sql = sql.replace(":tab_name", MapUtil.getString(tab, "tab_name"));
		return tmsSimpleDao.queryForList(sql, tab);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#queryTxnFeature(java.util.Map)
	 */
	public Map<String, Object> queryTxnFeature(Map<String, Object> reqs) {
		
//		String txnFeatureSql = tmsSqlMap.getSql("tms.pub.queryTxnFeature");;
		String txnFeatureSql = "SELECT * FROM TMS_COM_FD F WHERE EXISTS(SELECT 1 FROM (SELECT M.TAB_NAME FROM TMS_COM_TAB M WHERE M.TAB_NAME IN (:TAB_NAME) )M1 WHERE F.TAB_NAME=M1.TAB_NAME) AND  REF_NAME=:REF_NAME ORDER BY TAB_NAME DESC";;
		
		Map<String, Object> condData = new HashMap<String, Object>(); 
		condData.put("TAB_NAME", MapUtil.getString(reqs, "txnId"));
		condData.put("REF_NAME", MapUtil.getString(reqs, "stat_datafd"));
		
		List<Map<String,Object>> featureList = tmsSimpleDao.queryForList(txnFeatureSql, condData);
		if(featureList!=null && featureList.size() > 0) {
			return featureList.get(0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms35.manage.stat.service.StatService#saveStat(java.util.List)
	 */
	@Transactional
	public Map<String,Object> saveStat(Map<String, List<Map<String, ?>>> formList) {
		
		
		List<Map<String, ?>> delList = formList.get("del");
		List<Map<String, Object>> modList = MapUtil.getList(formList, "mod");
		List<Map<String, Object>> addList = MapUtil.getList(formList, "add");
		List<Map<String, Object>> validYList = MapUtil.getList(formList, "valid-y");
		List<Map<String, Object>> validNList = MapUtil.getList(formList, "valid-n");
		
		Map<String, Object> rmap = new HashMap<String, Object>();
		
		DataSource officialTmsDataSource = officialTransactionManager.getDataSource();
		
		DataSource tmpTmsDataSource = tmpTransactionManager.getDataSource();
		
		// 删除
		if(delList != null && delList.size() > 0) {
			cachInit(tmpTmsDataSource);
			for (Map<String, ?> map : delList) {
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);

				// 校验能否删除
				if(commonCheckService.find_ref_stat(txnId, stat_name))
					throw new TmsMgrServiceException("["+MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC)+"]被引用，不能删除");
			}
			
			cachInit(officialTmsDataSource);
			for (Map<String, ?> map : delList) {
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
				
				// 校验能否删除
				if(commonCheckService.find_ref_stat(txnId, stat_name))
					throw new TmsMgrServiceException("["+MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC)+"]被引用未授权，不能删除");
			}
			for (Map<String, ?> map : delList) {
				deleteStat(map);
			}
		}
		
		// 新增
		if(addList != null && addList.size() > 0) {
			cachInit(tmpTmsDataSource);
			for (Map<String, Object> map : addList) {
				// 校验能否修改
				checkCond(map);
				
				// 校验统计描述是否重复
				checkDuplicateStatDesc(map);
				
				rmap =  createStat(map);
			}
		}
		
		// 修改
		if(modList != null && modList.size() > 0) {
			cachInit(tmpTmsDataSource);
			db_stat.cache d = db_cache.get().stat();
			
			for (Map<String, Object> map : modList) {
				
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				
				db_stat db_stat = d.get(txnId, stat_name);
				
				// 校验是否被引用
				isRef(db_stat,map);
				
				// 校验条件是否有效
				checkCond(map);
				
				// 校验统计描述是否重复
				checkDuplicateStatDesc(map);
				
			}
			
			cachInit(officialTmsDataSource);
			for (Map<String, Object> map : modList) {
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				
				db_stat db_stat = d.get(txnId, stat_name);
				
				// 校验是否被引用
				isRef(db_stat,map);
			}
			
			for (Map<String, Object> map : modList) {
				rmap = modStat(map);
			}
		}
		
		// 有效性-启用
		if(validYList != null && validYList.size() > 0) {
			for (Map<String, Object> map : validYList) {
				// 校验条件是否有效
				checkCond(map);
				
				rmap = modStat(map);
			}
		}
		
		// 有效性-停用
		if(validNList != null && validNList.size() > 0) {
			cachInit(tmpTmsDataSource);
			for (Map<String, Object> map : validNList) {
				// 校验能否被引用
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				String stat_desc = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC);

				// 缓存中查找引用
				if(commonCheckService.find_ref_valid_stat(txnId , stat_name))
					throw new TmsMgrServiceException("["+stat_desc+"]被引用，不能停用");
			}
			
			cachInit(officialTmsDataSource);
			for (Map<String, Object> map : validNList) {
				// 校验能否被引用
				String txnId = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
				String stat_name = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_NAME);
				String stat_desc = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC);

				// 缓存中查找引用
				if(commonCheckService.find_ref_valid_stat(txnId , stat_name))
					throw new TmsMgrServiceException("["+stat_desc+"]被引用未授权，不能停用");
				
			}
			
			for (Map<String, Object> map : validNList) {
				rmap = modStat(map);
			}
		}

		return rmap;
		
	}


	/**
	* 方法描述:统计被引用后，统计目标、引用对象、统计函数、有效性不能修改
	* @param map
	*/
	private void isRef(db_stat db_stat, Map<String, Object> map) {
		
		String db_stat_fd = db_stat.stat_fd_name;// 统计目标
		String db_stat_param = db_stat.stat_param_fd;// 引用对象
		String db_stat_func = db_stat.stat_func_name;// 统计函数
		int db_is_valid = db_stat.is_valid;// 是否有效
		
		int in_is_valid = MapUtil.getInteger(map, DBConstant.TMS_COM_STAT_STAT_VALID);
/*		if((db_stat_fd != null && !db_stat_fd.equalsIgnoreCase(MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DATAFD))) 
			||!db_stat_param.equalsIgnoreCase(MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_PARAM))
			||!db_stat_func.equalsIgnoreCase(MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_FN))) {
			// 缓存中查找引用
			if(commonCheckService.find_ref_stat(db_stat.stat_txn , db_stat.stat_name))
				throw new TmsMgrServiceException("["+MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC)+"]被引用，不能编辑");
		}*/
		
		if(db_is_valid != in_is_valid && in_is_valid == 0) {
			// 缓存中查找引用
			if(commonCheckService.find_ref_valid_stat(db_stat.stat_txn , db_stat.stat_name))
				throw new TmsMgrServiceException("["+MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC)+"]被引用，不能编辑");
		}
		
	}


	private void cachInit(DataSource ds) {
		try {
			commonCheckService.initCache(ds);
		} catch (Exception e) {
			log.error(e);
			throw new TmsMgrServiceException("条件初始化错误，由于"+e.getLocalizedMessage());
		}
	}


	private void checkCond(Map<String, Object> map) {
		String stat_cond_value = MapUtil.getString(map, "STAT_COND");
		String stat_name = MapUtil.getString(map, "STAT_DESC");
		String stat_fn = MapUtil.getString(map, "STAT_FN");
		String data_type = MapUtil.getString(map, "DATATYPE");
		String txnid = MapUtil.getString(map, "STAT_TXN");
		StringBuffer error = new StringBuffer();
		
		if(!StringUtil.isEmpty(stat_cond_value)) {
			try {
				// 校验条件的有效性
				boolean isTrue = checkCond(stat_cond_value, txnid, error);
				
				// 不正确弹出错误信息
				if(!isTrue) {
					throw new TmsMgrServiceException("条件["+stat_name+"]"+error);
				}
				
			} catch (Exception e) {
				log.error(e);
				throw new TmsMgrServiceException("条件表达式不合法，由于"+e.getLocalizedMessage());
			}
			
			// 计数表达式函数需要校验表达式计数的数据类型是否和页面选择的数据类型一致
			if("calculat_expressions".equalsIgnoreCase(stat_fn)) {
				String type = web_tool.tms_type(txnid, stat_cond_value, error);
				if(!data_type.equalsIgnoreCase(type)) {
					throw new TmsMgrServiceException("表达式类型和数据类型不一致，请选择正确的数据类型：\""+code2name(type)+"\"");
				}
			}
		}
	}


	private String code2name(String type) {
		String sql = "SELECT * FROM CMC_CODE WHERE CATEGORY_ID='tms.stat.datatype'";
		List<Map<String,Object>> codeList = tmsSimpleDao.queryForList(sql);
		String mess = type;
		for (Map<String, Object> map2 : codeList) {
			if(type.equalsIgnoreCase(MapUtil.getString(map2, "CODE_KEY"))) {
				mess = MapUtil.getString(map2, "CODE_VALUE");
			}
		}
		return mess;
	}
	
	private boolean checkCond(String stat_cond_value, String txnid,
			StringBuffer error) {
		if(StringUtil.isEmpty(stat_cond_value)) return true;
		// 检查条件正确性
		boolean isTrue = web_tool.compile_expr(txnid, stat_cond_value, error);
		return isTrue;
	}
	private void deleteStat(Map<String,?> input) {
		tmsSimpleDao.delete("TMS_COM_STAT", MapWrap.map("STAT_ID", MapUtil.getLong(input, "STAT_ID")).getMap());
	}
	
	private Map<String, Object> createStat(Map<String, Object> input) {
		String sequenceId = sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_STAT_ID);
		
		// 查询当前交易下最大的统计名称
		String sql = tmsSqlMap.getSql("tms.stat.queryMaxStatName");
		List<Map<String, Object>> maxList = tmsSimpleDao.queryForList(sql);
		List<Map<String, Object>> maxOnList = onlineSimpleDao.queryForList(sql);
		Map<String,Object> stat = new HashMap<String, Object>();
		stat.put(DBConstant.TMS_COM_STAT_STAT_ID, Long.parseLong(sequenceId));
		String stat_name = MapUtil.getString(maxList.get(0), "maxStatName");
		String stat_on_name = MapUtil.getString(maxOnList.get(0), "maxStatName");
		int int_stat_name = Integer.parseInt(stat_name.substring(1));
		int int_stat_on_name = Integer.parseInt(stat_on_name.substring(1));
		if (int_stat_on_name > int_stat_name) {
			stat_name = stat_on_name;
		}
		while (stat_name.length() < 4) {
			stat_name = stat_name.substring(0, 1) + '0' + stat_name.substring(1);
		}

		stat.put(DBConstant.TMS_COM_STAT_STAT_NAME, stat_name);
		stat.put(DBConstant.TMS_COM_STAT_STAT_DESC, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_DESC));
		stat.put(DBConstant.TMS_COM_STAT_STAT_TXN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_TXN));
		String stat_param = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_PARAM);
		String stat_fn = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_FN);
		if(!"calculat_expressions".equalsIgnoreCase(stat_fn)) {
			stat.put(DBConstant.TMS_COM_STAT_STAT_PARAM, sortString(stat_param));
		}
		stat.put(DBConstant.TMS_COM_STAT_STAT_COND, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_COND));
		stat.put(DBConstant.TMS_COM_STAT_STAT_COND_IN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_COND_IN));
		stat.put(DBConstant.TMS_COM_STAT_STAT_DATAFD, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_DATAFD));
		
		stat.put(DBConstant.TMS_COM_STAT_STAT_FN, stat_fn);
		stat.put(DBConstant.TMS_COM_STAT_STORECOLUMN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STORECOLUMN));
		stat.put(DBConstant.TMS_COM_STAT_DATATYPE, MapUtil.getString(input, DBConstant.TMS_COM_STAT_DATATYPE));

		String result_cond = MapUtil.getString(input, DBConstant.TMS_COM_STAT_RESULT_COND);
		if(result_cond.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_RESULT_COND, StringUtil.parseToLong(result_cond));
		
		String counuint = MapUtil.getString(input, DBConstant.TMS_COM_STAT_COUNUNIT);
		if(counuint.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_COUNUNIT, StringUtil.parseToLong(counuint));
		
		String rond = MapUtil.getString(input, DBConstant.TMS_COM_STAT_COUNTROUND);
		if(rond.length()>0) {
			stat.put(DBConstant.TMS_COM_STAT_COUNTROUND, StringUtil.parseToLong(rond));
		}
		
		String unresult = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_UNRESULT);
		if(unresult.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_STAT_UNRESULT, StringUtil.parseToLong(unresult));
		
		String continues = MapUtil.getString(input, DBConstant.TMS_COM_STAT_CONTINUES);
		if(continues.length()>0)
			stat.put(DBConstant.TMS_COM_STAT_CONTINUES, StringUtil.parseToLong(continues));
		
		stat.put(DBConstant.TMS_COM_STAT_STAT_VALID, MapUtil.getLong(input, DBConstant.TMS_COM_STAT_STAT_VALID));
		stat.put(DBConstant.TMS_COM_STAT_STAT_FN_PARAM, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_FN_PARAM));
		
		input.put(DBConstant.TMS_COM_STAT_STAT_ID, sequenceId);
		
		tmsSimpleDao.create("TMS_COM_STAT", stat);
		return stat;
	}
	
	private void checkDuplicateStatDesc(Map<String, Object> map){
		String stat_id = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_ID);
		String stat_desc = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DESC);
		String  txn_id = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);

		String txnids = TransCommon.arr2str(TransCommon.cutToIds(txn_id));
		
		String sql = "select * from tms_com_stat a where stat_desc = ? " +
		"and (stat_txn in ("+txnids+") OR  exists(select 1 from TMS_COM_TAB where TAB_NAME like '"+txn_id+"%' and a.stat_txn=TAB_NAME))";
		
		if(stat_id != null && stat_id.length() > 0) {
			sql += " and stat_id != "+stat_id;
		}
		
		
		
		List<Map<String,Object>> statList = tmsSimpleDao.queryForList(sql, stat_desc);
		
		if(statList != null && statList.size() > 0) {
			String txnName = transDefService.getSelfAndParentTranDefAsStr(MapUtil.getString(statList.get(0), DBConstant.TMS_COM_STAT_STAT_TXN));
			throw new TmsMgrServiceException("当前统计描述与交易["+txnName+"]统计描述重复");
		}
		
	}
	
	private Map<String,Object> modStat(Map<String, ?> input) {
		
		Map<String,Object> stat = new HashMap<String, Object>();
		stat.put(DBConstant.TMS_COM_STAT_STAT_ID, MapUtil.getLong(input, DBConstant.TMS_COM_STAT_STAT_ID));
		stat.put(DBConstant.TMS_COM_STAT_STAT_NAME, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_NAME));
		
		stat.put(DBConstant.TMS_COM_STAT_STAT_DESC, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_DESC));
		stat.put(DBConstant.TMS_COM_STAT_STAT_TXN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_TXN));
		String stat_param = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_PARAM);
		stat.put(DBConstant.TMS_COM_STAT_STAT_PARAM, sortString(stat_param));
		stat.put(DBConstant.TMS_COM_STAT_STAT_COND, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_COND));
		stat.put(DBConstant.TMS_COM_STAT_STAT_COND_IN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_COND_IN));
		stat.put(DBConstant.TMS_COM_STAT_STAT_DATAFD, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_DATAFD));
		stat.put(DBConstant.TMS_COM_STAT_STORECOLUMN, MapUtil.getString(input, DBConstant.TMS_COM_STAT_STORECOLUMN));
		stat.put(DBConstant.TMS_COM_STAT_DATATYPE, MapUtil.getString(input, DBConstant.TMS_COM_STAT_DATATYPE));
		String stat_fn = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_FN);
		stat.put(DBConstant.TMS_COM_STAT_STAT_FN, stat_fn);
		
		String result_cond = MapUtil.getString(input, DBConstant.TMS_COM_STAT_RESULT_COND);
		if(result_cond.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_RESULT_COND, StringUtil.parseToLong(result_cond));
		
		String counuint = MapUtil.getString(input, DBConstant.TMS_COM_STAT_COUNUNIT);
		if(counuint.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_COUNUNIT, StringUtil.parseToLong(counuint));
		
		String rond = MapUtil.getString(input, DBConstant.TMS_COM_STAT_COUNTROUND);
		if(rond.length()>0) {
			stat.put(DBConstant.TMS_COM_STAT_COUNTROUND, StringUtil.parseToLong(rond));
		}
		
		String unresult = MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_UNRESULT);
		if(unresult.length() > 0)
			stat.put(DBConstant.TMS_COM_STAT_STAT_UNRESULT, StringUtil.parseToLong(unresult));
		
		String continues = MapUtil.getString(input, DBConstant.TMS_COM_STAT_CONTINUES);
		if(continues.length()>0)
			stat.put(DBConstant.TMS_COM_STAT_CONTINUES, StringUtil.parseToLong(continues));
		
		stat.put(DBConstant.TMS_COM_STAT_STAT_VALID, MapUtil.getLong(input, DBConstant.TMS_COM_STAT_STAT_VALID));
		
		stat.put(DBConstant.TMS_COM_STAT_STAT_FN_PARAM, (!stat_fn.equals(RANG_BIN_DIST))?"":MapUtil.getString(input, DBConstant.TMS_COM_STAT_STAT_FN_PARAM));
		
		updateOneStat(stat);
		return stat;
	}
	private String sortString(String args) {
		String[] ds = args.split(",");
		Arrays.sort(ds);
		StringBuilder sb = new StringBuilder();
		for (String d : ds) {
			if(sb.length() > 0) sb.append(",");
			sb.append(d);
		}
		return sb.toString();
	}

	private void updateOneStat(Map<String, Object> input) {
		tmsSimpleDao.update("TMS_COM_STAT", input, MapWrap.map(DBConstant.TMS_COM_STAT_STAT_ID, MapUtil.getLong(input, DBConstant.TMS_COM_STAT_STAT_ID)).getMap());
	}
}
