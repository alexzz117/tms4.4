/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.rule.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.ac.service.AcService;
import cn.com.higinet.tms.manager.modules.common.CommonCheckService;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.RuleJsonUtil;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.rule.service.RuleService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;
import cn.com.higinet.tms35.comm.web_tool;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * 功能/模块:
 * 
 * @author 张立群
 * @version 1.0 May 22, 2013 
 * 类描述: 
 * 修订历史: 日期 作者 参考 描述
 */
@Service("ruleService35")
@Transactional
public class RuleServiceImpl implements RuleService {

	@Autowired
	@Qualifier("onlineDataSource")
	private DataSource onlineDataSource;
	@Autowired
	@Qualifier("offlineDataSource")
	private DataSource offlineDataSource;
	@Autowired
	@Qualifier("dynamicDataSource")
	private DataSource dynamicDataSource;
	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;
	@Autowired
	@Qualifier("onlineSimpleDao")
	private SimpleDao onlineSimpleDao;
	@Autowired
	private SqlMap tmsSqlMap;
	@Autowired
	private TransDefService transDefService;
	@Autowired
	private CommonCheckService commonCheckService;
	@Autowired
	private SequenceService sequenceService;
	@Autowired
	private AcService scService;

	private static Log log = LogFactory.getLog(RuleService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#listRule(java.util.Map)
	 */
	public List<Map<String, Object>> listRule(Map<String, Object> input) {
		String txnId = MapUtil.getString(input, DBConstant.TMS_COM_RULE_RULE_TXN);
		String type = MapUtil.getString(input, "type");
		String rule_sql = "";
		List<Map<String, Object>> rule_list = null;

		// 策略规则
		if ("1".equals(type)) {
			String st_id = MapUtil.getString(input, "st_id");
			// String txnIds = TransCommon.arr2str(TransCommon.cutToIds(txnId));;
			// rule_sql = "SELECT R.*,(SELECT COUNT(1) FROM TMS_COM_RULE_ACTION RA WHERE R.RULE_ID=RA.RULE_ID)  ACTION_COUNT FROM TMS_COM_RULE R LEFT JOIN TMS_COM_DISPOSAL C ON R.DISPOSAL=C.DP_CODE WHERE RULE_TXN IN ("+txnIds+") AND NOT EXISTS(SELECT 1 FROM TMS_COM_STRATEGY_RULE_REL SRR WHERE SRR.RULE_ID = R.RULE_ID AND SRR.ST_ID='"+st_id+"') ORDER BY C.RULE_ORDER ,R.RULE_SCORE ,R.RULE_TIMESTAMP DESC ";
			// List<Map<String,Object>> oc_list = officialSimpleDao.queryForList(rule_sql);
			// List<Map<String,Object>> tmp_list = tmpSimpleDao.queryForList(rule_sql);

			String txnIdSql = TransCommon.cutToIdsForSqlInj(txnId);
			Map<String, Object> txnIdsMap = TransCommon.cutToMapIdsForSqlInj(txnId);
			rule_sql = "SELECT R.*,(SELECT COUNT(1) FROM TMS_COM_RULE_ACTION RA WHERE R.RULE_ID=RA.RULE_ID)  ACTION_COUNT FROM TMS_COM_RULE R LEFT JOIN TMS_COM_DISPOSAL C ON R.DISPOSAL=C.DP_CODE WHERE RULE_TXN IN (" + txnIdSql
					+ ") AND NOT EXISTS(SELECT 1 FROM TMS_COM_STRATEGY_RULE_REL SRR WHERE SRR.RULE_ID = R.RULE_ID AND SRR.ST_ID=:stId) ORDER BY C.RULE_ORDER ,R.RULE_SCORE ,R.RULE_TIMESTAMP DESC ";
			Map<String, Object> sqlConds = new HashMap<String, Object>();
			sqlConds.putAll(txnIdsMap);
			sqlConds.put("stId", st_id);
			List<Map<String, Object>> oc_list = onlineSimpleDao.queryForList(rule_sql, sqlConds);
			List<Map<String, Object>> tmp_list = offlineSimpleDao.queryForList(rule_sql, sqlConds);
			rule_list = new ArrayList<Map<String, Object>>();

			if (oc_list == null || oc_list.size() == 0 || oc_list == null || oc_list.size() == 0)
				return rule_list;

			// 策略规则选择列表显示在线库和离线库都有的规则
			for (Map<String, Object> map2 : tmp_list) {
				String t_rule_id = MapUtil.getString(map2, DBConstant.TMS_COM_RULE_RULE_ID);
				for (Map<String, Object> map3 : oc_list) {
					String o_rule_id = MapUtil.getString(map3, DBConstant.TMS_COM_RULE_RULE_ID);
					if (t_rule_id.equals(o_rule_id)) {
						rule_list.add(map2);
					}
				}
			}

		} else {
			// rule_sql = "SELECT R.*,(SELECT COUNT(1) FROM TMS_COM_RULE_ACTION RA WHERE R.RULE_ID=RA.RULE_ID)  ACTION_COUNT FROM TMS_COM_RULE R LEFT JOIN TMS_COM_DISPOSAL C ON R.DISPOSAL=C.DP_CODE WHERE RULE_TXN='"+txnId+"' ORDER BY C.RULE_ORDER ,R.RULE_SCORE ,R.RULE_TIMESTAMP DESC ";
			rule_sql = "SELECT R.*,(SELECT COUNT(1) FROM TMS_COM_RULE_ACTION RA WHERE R.RULE_ID=RA.RULE_ID)  ACTION_COUNT FROM TMS_COM_RULE R LEFT JOIN TMS_COM_DISPOSAL C ON R.DISPOSAL=C.DP_CODE WHERE RULE_TXN=:txnId ORDER BY C.RULE_ORDER ,R.RULE_SCORE ,R.RULE_TIMESTAMP DESC ";
			Map<String, Object> sqlConds = new HashMap<String, Object>();
			sqlConds.put("txnId", txnId);
			rule_list = tmsSimpleDao.queryForList(rule_sql, sqlConds);
		}

		for (Map<String, Object> map : rule_list) {
			map.put("RULE_ORDER_VIEW", MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_EVAL_TYPE) + "_" + MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_DISPOSAL) + "_" + MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_RULE_ORDER));
		}

		return rule_list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#saveRule(java.util.Map)
	 */
	@Transactional
	public Map<String, Object> saveRule(Map<String, List<Map<String, ?>>> formList) {
		List<Map<String, Object>> delList = MapUtil.getList(formList, "del");
		List<Map<String, Object>> modList = MapUtil.getList(formList, "mod");
		List<Map<String, Object>> addList = MapUtil.getList(formList, "add");
		List<Map<String, Object>> copyList = MapUtil.getList(formList, "copy");
		List<Map<String, Object>> validYList = MapUtil.getList(formList, "valid-y");
		List<Map<String, Object>> validNList = MapUtil.getList(formList, "valid-n");

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 删除
		if (delList != null && delList.size() > 0) {
			for (Map<String, Object> map : delList) {
				String sql = "select * from tms_com_strategy_rule_rel where rule_id=?";
				long t_strategy_rule = tmsSimpleDao.count(sql, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ID));
				long o_strategy_rule = onlineSimpleDao.count(sql, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ID));
				if (t_strategy_rule > 0) {
					throw new TmsMgrServiceException("规则[" + MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被策略引用");
				}
				if (o_strategy_rule > 0) {
					throw new TmsMgrServiceException("规则[" + MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被引用的策略未授权");
				}
				deleteRule(map);
				rmap = map;
			}
		}

		// 复制
		if (copyList != null && copyList.size() > 0) {
			for (Map<String, Object> map : copyList) {

				String old_rule_id = MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ID);

				// 校验规则名称不能重复
				checkDuplicateRuleName(map);

				// 复制规则
				rmap = createRule(map);

				// 复制动作
				Map<String, Object> cond_map = new HashMap<String, Object>();
				cond_map.put("rule_id", old_rule_id);
				List<Map<String, Object>> ac_list = scService.listAction(cond_map);
				String new_rule_id = MapUtil.getString(rmap, DBConstant.TMS_COM_RULE_RULE_ID);
				for (Map<String, Object> map2 : ac_list) {
					map2.put(DBConstant.TMS_COM_RULE_RULE_ID, new_rule_id);
				}
				// scService.saveAc(ac_formList);
				if (ac_list != null && ac_list.size() > 0) {
					for (Map<String, Object> map2 : ac_list) {
						map2.put(DBConstant.TMS_COM_RULE_ACTION_RULE_ID, MapUtil.getString(rmap, DBConstant.TMS_COM_RULE_RULE_ID));
						// scService.createAction(map2);

						Long sequenceId = Long.valueOf(sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_RULE_ACTION_ID));

						Map<String, Object> actionData = new HashMap<String, Object>();
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_ID, sequenceId);
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_RULE_ID, MapUtil.getLong(map2, DBConstant.TMS_COM_RULE_ACTION_RULE_ID));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_DESC, MapUtil.getString(map2, DBConstant.TMS_COM_RULE_ACTION_AC_DESC));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_COND, MapUtil.getString(map2, DBConstant.TMS_COM_RULE_ACTION_AC_COND));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN, MapUtil.getString(map2, DBConstant.TMS_COM_RULE_ACTION_AC_COND_IN));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_EXPR, MapUtil.getString(map2, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN, MapUtil.getString(map2, DBConstant.TMS_COM_RULE_ACTION_AC_EXPR_IN));
						actionData.put(DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE, MapUtil.getLong(map2, DBConstant.TMS_COM_RULE_ACTION_AC_ENABLE));
						tmsSimpleDao.create("TMS_COM_RULE_ACTION", actionData);

					}
				}
			}
		}

		// 新增
		if (addList != null && addList.size() > 0) {
			// 初始化缓存
			cache_init.init(new data_source(dynamicDataSource));

			for (Map<String, Object> map : addList) {
				checkCond(map);// 校验条件有效性

				// 校验规则名称不能重复
				checkDuplicateRuleName(map);

				rmap = createRule(map);
			}
		}

		// 修改
		if (modList != null && modList.size() > 0) {
			// 初始化缓存
			cache_init.init(new data_source(dynamicDataSource));

			for (Map<String, Object> map : modList) {
				checkCond(map);// 校验条件有效性

				// 校验动作名称不能重复
				checkDuplicateRuleName(map);

				rmap = modRule(map);
			}
		}

		// 有效性-启用
		if (validYList != null && validYList.size() > 0) {
			// 初始化缓存
			cache_init.init(new data_source(dynamicDataSource));

			for (Map<String, Object> map : validYList) {
				// 校验条件是否有效
				checkCond(map);

				rmap = modRule(map);
			}
		}

		// 有效性-停用
		if (validNList != null && validNList.size() > 0) {
			for (Map<String, Object> map : validNList) {
				rmap = modRule(map);
			}
		}

		return rmap;
	}

	/**
	 * 方法描述:修改规则
	 * 
	 * @param map
	 * @return
	 */
	private Map<String, Object> modRule(Map<String, Object> map) {
		// 规则修改条件
		Map<String, Object> condData = new HashMap<String, Object>();
		condData.put(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULE_RULE_ID));

		// 规则修改数据
		Map<String, Object> ruleData = new HashMap<String, Object>();
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ID));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_NAME, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_NAME));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_SHORTDESC, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SHORTDESC));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_DESC, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_DESC));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_COND, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_COND));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_COND_IN, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_COND_IN));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_TXN, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_TXN));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_SCORE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SCORE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ENABLE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ISTEST, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ISTEST));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_EVAL_TYPE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_EVAL_TYPE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_DISPOSAL, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_DISPOSAL));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_RULE_ORDER, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_RULE_ORDER));
		long sysdate = System.currentTimeMillis();
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_TIMESTAMP, sysdate);

		// 修改
		tmsSimpleDao.update("TMS_COM_RULE", ruleData, condData);
		
		ruleData.put("UPDATE_DATE", sysdate);
		offlineSimpleDao.update("TMS_COM_RULE_TEMP", ruleData,condData);

		return ruleData;
	}

	/**
	 * 校验规则名称不能重复 方法描述:
	 * 
	 * @param map
	 */
	private void checkDuplicateRuleName(Map<String, Object> cond) {
		String rule_shortdesc = MapUtil.getString(cond, "RULE_SHORTDESC");
		String rule_txn = MapUtil.getString(cond, "RULE_TXN");
		String rule_id = MapUtil.getString(cond, "RULE_ID");

		// String txnids = TransCommon.arr2str(TransCommon.cutToIds(rule_txn));
		String txnIdSql = TransCommon.cutToIdsForSqlInj(rule_txn);
		Map<String, Object> txnIdsMap = TransCommon.cutToMapIdsForSqlInj(rule_txn);
		txnIdsMap.put("ruleShortDesc", rule_shortdesc);

		String tabName = "'" + rule_txn + "%'";
		txnIdsMap.put("tabName", tabName);

		String sql = "SELECT a.* FROM TMS_COM_RULE a WHERE RULE_SHORTDESC=:ruleShortDesc " + "and (RULE_TXN in (" + txnIdSql + ") OR  exists(select 1 from TMS_COM_TAB where TAB_NAME like :tabName  and a.RULE_TXN=TAB_NAME))";

		if (rule_id != null && rule_id.length() > 0) {
			sql += " and RULE_ID !=:ruleId";
			txnIdsMap.put("ruleId", rule_id);
		}

		List<Map<String, Object>> ruleList = tmsSimpleDao.queryForList(sql, txnIdsMap);
		if (ruleList != null && ruleList.size() > 0) {
			String txnName = transDefService.getSelfAndParentTranDefAsStr(MapUtil.getString(ruleList.get(0), DBConstant.TMS_COM_RULE_RULE_TXN));
			throw new TmsMgrServiceException("规则[" + rule_shortdesc + "]与交易[" + txnName + "]规则名称重复");
		}
	}

	// 校验条件有效性
	private void checkCond(Map<String, Object> map) {
		String rule_cond = MapUtil.getString(map, "RULE_COND");
		String rule_shortdesc = MapUtil.getString(map, "RULE_SHORTDESC");
		String rule_txn = MapUtil.getString(map, "RULE_TXN");
		StringBuffer error = new StringBuffer();

		if (!StringUtil.isEmpty(rule_cond)) {
			// 检查条件正确性
			boolean isTrue = true;
			try {
				isTrue = web_tool.compile_expr(rule_txn, rule_cond, error);
			} catch (Exception e) {
				log.error(e);
				throw new TmsMgrServiceException("条件表达式不合法，由于" + "条件[" + rule_shortdesc + "]" + e.getLocalizedMessage());
			}
			// 不正确弹出错误信息
			if (!isTrue) {
				throw new TmsMgrServiceException("条件表达式不合法，由于" + "条件[" + rule_shortdesc + "]" + error);
			}
		}
	}

	/**
	 * 方法描述:删除规则
	 * 
	 * @param map
	 */
	private void deleteRule(Map<String, Object> map) {
		tmsSimpleDao.delete("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULE_RULE_ID)).getMap());
		tmsSimpleDao.delete("TMS_COM_RULE_ACTION", MapWrap.map(DBConstant.TMS_COM_RULE_ACTION_RULE_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULE_RULE_ID)).getMap());
	}

	/**
	 * 方法描述:规则存库
	 * 
	 * @param map
	 * @return
	 */
	private Map<String, Object> createRule(Map<String, Object> map) {
		String txnId = MapUtil.getString(map, "RULE_TXN");
		// 添加新增规则
		String sql = tmsSqlMap.getSql("tms.rule.queryMaxRuleName");
		List<Map<String, Object>> maxList = offlineSimpleDao.queryForList(sql, txnId);
		List<Map<String, Object>> maxOnList = onlineSimpleDao.queryForList(sql, txnId);
		String rule_name = MapUtil.getString(maxList.get(0), "maxRuleName");
		String rule_on_name = MapUtil.getString(maxOnList.get(0), "maxRuleName");
		
		int int_rule_name = Integer.parseInt(rule_name.substring(1));
		int int_rule_on_name = Integer.parseInt(rule_on_name.substring(1));
		if (int_rule_on_name > int_rule_name) {
			rule_name = rule_on_name;
		}
		// 规则名称+1
		rule_name = rule_name.substring(0, 1) + (Integer.parseInt(rule_name.substring(1)) + 1);

		Map<String, Object> ruleData = new HashMap<String, Object>();
		Long sequenceId = Long.valueOf(sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_RULE_ID));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ID, sequenceId);
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_NAME, rule_name);
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_SHORTDESC, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SHORTDESC));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_DESC, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_DESC));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_COND, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_COND));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_COND_IN, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_COND_IN));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_TXN, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_TXN));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_SCORE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_SCORE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ENABLE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_ISTEST, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_ISTEST));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_EVAL_TYPE, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_EVAL_TYPE));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_DISPOSAL, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_DISPOSAL));
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_RULE_ORDER, MapUtil.getString(map, DBConstant.TMS_COM_RULE_RULE_RULE_ORDER));
		long sysdate = System.currentTimeMillis();
		ruleData.put(DBConstant.TMS_COM_RULE_RULE_TIMESTAMP, sysdate);
		
		tmsSimpleDao.create("TMS_COM_RULE", ruleData);
		
		ruleData.put("UPDATE_DATE", sysdate);
		offlineSimpleDao.create("TMS_COM_RULE_TEMP", ruleData);
		
		map.put(DBConstant.TMS_COM_RULE_RULE_ID, sequenceId);

		return ruleData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#getRule(java.util.Map)
	 */
	public Map<String, Object> getRule(Map<String, Object> reqs) {
		Long ruleId = MapUtil.getLong(reqs, "ruleId");
		return tmsSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId).getMap());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#getRuleId()
	 */
	public String getRuleId() {
		return sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_RULE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#auditRule(java.lang.String, java.lang.String, boolean)
	 */
	// TODO 规则授权需要修改
	// 规则授权
	@Transactional
	public void auditRule(String ruleId, String operate, boolean result) {
		long ruleId_l = Long.parseLong(ruleId);
		if (operate.equals("add")) { // 新增规则审核
			if (result) { // 审核通过

				Map<String, Object> tempRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, Long.valueOf(ruleId)).getMap());
				onlineSimpleDao.create("TMS_COM_RULE", tempRuleMap); // 添加规则到正式表

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn " + "where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> sqlConds = new HashMap<String, Object>();
				sqlConds.put("tabName", tabName);

				List<Map<String, Object>> officialRelationList = onlineSimpleDao.queryForList(sql, sqlConds);
				List<Map<String, Object>> tempRelationList = offlineSimpleDao.queryForList(sql);

				for (Map<String, Object> tempRelationMap : tempRelationList) {
					String tempMetadataJson = MapUtil.getString(tempRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
					Map<String, Map<String, Object>> tempMetadataMaps = RuleJsonUtil.json2Maps(tempMetadataJson);
					String tempJson = RuleJsonUtil.map2Json(MapUtil.getMap(tempMetadataMaps, ruleId));
					Map<String, Object> tempMap = RuleJsonUtil.json2Map(tempJson);

					Map<String, Object> tempMetadata = new HashMap<String, Object>();
					tempMetadata.put("RULE_ID", MapUtil.getString(tempMap, "RULE_ID"));
					tempMetadata.put("POSITION", MapUtil.getString(tempMap, "POSITION"));
					tempMetadata.put("DIRECTIONAL", null);
					String officialJson = RuleJsonUtil.map2Json(tempMetadata);

					for (Map<String, Object> officialRelationMap : officialRelationList) {
						if (MapUtil.getString(tempRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_TXN).equals(MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_TAB.TAB_NAME))) {
							officialRelationMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);

							if (StringUtil.isEmpty(MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
								String parentJson = null;
								String newJson = null;

								String txnIdsSql = TransCommon.cutToIdsForSqlInj(MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN));
								Map<String, Object> txnIdsMap = TransCommon.cutToMapIdsForSqlInj(MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN));

								String sql1 = "select * from (select * from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn ) m where m.tab_name in (" + txnIdsSql + ") and rulerel_id is not null  order by tab_name desc";

								List<Map<String, Object>> parentJsonList = tmsSimpleDao.queryForList(sql1, txnIdsMap);
								if (parentJsonList != null && parentJsonList.size() > 0 && MapUtil.getString(parentJsonList.get(0), "RULEREL_METADATA").length() > 2) {
									parentJson = MapUtil.getString(parentJsonList.get(0), "RULEREL_METADATA");
									newJson = "[" + parentJson.substring(1, parentJson.length() - 1) + "," + officialJson + "]";
								} else {
									newJson = "[" + officialJson + "]";
								}

								officialRelationMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(tempRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID));
								officialRelationMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_TXN, MapUtil.getString(tempRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_TXN));
								officialRelationMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, newJson);

								onlineSimpleDao.create("TMS_COM_RULERELATION", officialRelationMap);
							} else {
								String officialMetadataJson = MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
								officialMetadataJson = officialMetadataJson.substring(0, officialMetadataJson.length() - 1) + "," + officialJson + "]";
								officialRelationMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, officialMetadataJson);

								onlineSimpleDao.update("TMS_COM_RULERELATION", officialRelationMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
							}
						}
					}
				}
			} else { // 审核未通过
				Map<String, Object> tempRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());

				offlineSimpleDao.delete("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn " + "where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);

				List<Map<String, Object>> tempRelationList = offlineSimpleDao.queryForList(sql, conds);
				for (Map<String, Object> map : tempRelationList) {

					String tempMetadataJson = MapUtil.getString(map, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
					Map<String, Map<String, Object>> oldMetadataMaps = RuleJsonUtil.json2Maps(tempMetadataJson);
					oldMetadataMaps.remove(ruleId);

					if (oldMetadataMaps.isEmpty()) {
						offlineSimpleDao.delete("TMS_COM_RULERELATION", MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
					} else {

						String newMetadataMaps = RuleJsonUtil.maps2Json(oldMetadataMaps);
						map.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, newMetadataMaps);
						map.remove(DBConstant.TMS_COM_TAB.TAB_NAME);
						offlineSimpleDao.update("TMS_COM_RULERELATION", map, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
					}
				}
			}
		}

		if (operate.equals("mod")) { // 修改规则审核
			if (result) { // 审核通过
				Map<String, Object> tempRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				onlineSimpleDao.update("TMS_COM_RULE", tempRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap()); // 更新规则到正式表
			} else { // 审核未通过
				Map<String, Object> officialRuleMap = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				offlineSimpleDao.update("TMS_COM_RULE", officialRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
			}
		}
		if (operate.equals("valid-y")) { // 启用规则审核
			if (result) { // 审核通过
				Map<String, Object> tempRuleMap = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				tempRuleMap.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, 1);
				onlineSimpleDao.update("TMS_COM_RULE", tempRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap()); // 更新规则到正式表
			} else { // 审核未通过
				Map<String, Object> officialRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				officialRuleMap.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, 0);
				offlineSimpleDao.update("TMS_COM_RULE", officialRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
			}
		}
		if (operate.equals("valid-n")) { // 停用规则审核
			if (result) { // 审核通过
				Map<String, Object> tempRuleMap = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				tempRuleMap.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, 0);
				onlineSimpleDao.update("TMS_COM_RULE", tempRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap()); // 更新规则到正式表
			} else { // 审核未通过
				Map<String, Object> officialRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				officialRuleMap.put(DBConstant.TMS_COM_RULE_RULE_ENABLE, 1);
				offlineSimpleDao.update("TMS_COM_RULE", officialRuleMap, MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
			}
		}

		if (operate.equals("del")) { // 删除规则审核
			if (result) { // 审核通过
				// 查询当前规则的父规则
				List<Map<String, Object>> parentRuleList = offlineSimpleDao.queryForList("select * from tms_com_rule_child where child_id=?", ruleId_l);

				Map<String, Object> tempRuleMap = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);
				List<Map<String, Object>> officialRelationList = onlineSimpleDao.queryForList(sql, conds);

				onlineSimpleDao.delete("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());

				for (Map<String, Object> relationMap : officialRelationList) {
					if (StringUtil.isNotEmpty(MapUtil.getString(relationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
						String officialMetadataJson = MapUtil.getString(relationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
						Map<String, Map<String, Object>> oldMetadataMaps = RuleJsonUtil.json2Maps(officialMetadataJson);
						oldMetadataMaps.remove(ruleId);
						if (oldMetadataMaps.isEmpty()) {
							onlineSimpleDao.delete("TMS_COM_RULERELATION", MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(relationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
						} else {
							String newMetadataMaps = RuleJsonUtil.maps2Json(oldMetadataMaps);

							relationMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, newMetadataMaps);
							relationMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);
							onlineSimpleDao.update("TMS_COM_RULERELATION", relationMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(relationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
						}
					}
				}
			} else { // 审核未通过
				Map<String, Object> officiaRuleMap = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, ruleId_l).getMap());
				offlineSimpleDao.create("TMS_COM_RULE", officiaRuleMap); // 添加规则到正式表

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(officiaRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);

				List<Map<String, Object>> tempRelationList = offlineSimpleDao.queryForList(sql, conds);
				List<Map<String, Object>> officialRelationList = onlineSimpleDao.queryForList(sql, conds);
				for (Map<String, Object> officialRelationMap : officialRelationList) {
					if (StringUtil.isNotEmpty(MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
						String officialMetadataJson = MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
						Map<String, Map<String, Object>> officialMetadataMaps = RuleJsonUtil.json2Maps(officialMetadataJson);
						String officialMetada = RuleJsonUtil.map2Json(MapUtil.getMap(officialMetadataMaps, ruleId));
						Map<String, Object> officialMap = RuleJsonUtil.json2Map(officialMetada);

						Map<String, Object> officialMetadata = new HashMap<String, Object>();
						officialMetadata.put("RULE_ID", MapUtil.getString(officialMap, "RULE_ID"));
						officialMetadata.put("POSITION", MapUtil.getString(officialMap, "POSITION"));
						officialMetadata.put("DIRECTIONAL", null);
						String officialJson = RuleJsonUtil.map2Json(officialMetadata);

						for (Map<String, Object> map : tempRelationList) {
							if (MapUtil.getString(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_TXN).equals(MapUtil.getString(map, DBConstant.TMS_COM_TAB.TAB_NAME))) {
								if (StringUtil.isEmpty(MapUtil.getString(map, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
									String tempMetadataJson = "[" + officialJson + "]";
									map.put(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(officialRelationMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID));
									map.put(DBConstant.TMS_COM_RULERELATION_RULEREL_TXN, MapUtil.getString(map, DBConstant.TMS_COM_TAB.TAB_NAME));
									map.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, tempMetadataJson);
									map.remove(DBConstant.TMS_COM_TAB.TAB_NAME);
									offlineSimpleDao.create("TMS_COM_RULERELATION", map);
								} else {
									String tempMetadataJson = MapUtil.getString(map, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);

									tempMetadataJson = tempMetadataJson.substring(0, tempMetadataJson.length() - 1) + "," + officialJson + "]";

									map.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, tempMetadataJson);
									map.remove(DBConstant.TMS_COM_TAB.TAB_NAME);
									offlineSimpleDao.update("TMS_COM_RULERELATION", map, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(map, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
								}
							}
						}
					}
				}
			}
		}
	}

	// 画线授权
	@Transactional
	public void auditLine(String lineId, String operate, boolean result) {
		long longId_l = Long.parseLong(lineId);
		if (operate.equals("add")) { // 新增连接线审核
			if (result) { // 审核通过
				Map<String, Object> templineMap = offlineSimpleDao.retrieve("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());
				onlineSimpleDao.create("TMS_COM_RULE_CHILD", templineMap);

				Map<String, Object> tempRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(templineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID)).getMap());
				// String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn " + "where tab_name like '" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn " + "where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);

				List<Map<String, Object>> officialRelationList = onlineSimpleDao.queryForList(sql, conds);

				for (Map<String, Object> officialMap : officialRelationList) {
					if (StringUtil.isNotEmpty(MapUtil.getString(officialMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
						String oldJson = MapUtil.getString(officialMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
						// 在原来的基础上修改RuleId的指向
						Map<String, Map<String, Object>> jsonMap = RuleJsonUtil.json2Maps(oldJson);
						Map<String, Object> relationMap = MapUtil.getMap(jsonMap, MapUtil.getString(templineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID));

						ArrayList<String> directionalList = (ArrayList<String>) MapUtil.getObject(relationMap, "DIRECTIONAL");
						directionalList.add(MapUtil.getString(templineMap, DBConstant.TMS_COM_RULE_CHILD_CHILD_ID));

						// 替换后再转回JSON串
						oldJson = RuleJsonUtil.maps2Json(jsonMap);

						officialMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, oldJson);
						officialMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);

						onlineSimpleDao.update("TMS_COM_RULERELATION", officialMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(officialMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
					}
				}
			} else {
				Map<String, Object> templineMap = offlineSimpleDao.retrieve("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());
				offlineSimpleDao.delete("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());

				Map<String, Object> tempRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(templineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID)).getMap());
				// String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like '" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn " + "where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(tempRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);

				// 更新relation
				List<Map<String, Object>> tempRelationList = offlineSimpleDao.queryForList(sql, conds);
				for (Map<String, Object> reMap : tempRelationList) {
					if (StringUtil.isNotEmpty(MapUtil.getString(reMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID))) {
						String oldJson = MapUtil.getString(reMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
						// 在原来的基础上修改RuleId的指向
						Map<String, Map<String, Object>> jsonMap = RuleJsonUtil.json2Maps(oldJson);
						Map<String, Object> ruleMap = MapUtil.getMap(jsonMap, MapUtil.getString(templineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID));
						List<String> directionalList = (ArrayList<String>) MapUtil.getObject(ruleMap, "DIRECTIONAL");
						if (directionalList != null)
							directionalList.remove(MapUtil.getString(templineMap, DBConstant.TMS_COM_RULE_CHILD_CHILD_ID));

						// 替换后再转回JSON串
						oldJson = RuleJsonUtil.maps2Json(jsonMap);

						reMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, oldJson);
						reMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);

						offlineSimpleDao.update("TMS_COM_RULERELATION", reMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(reMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
					}
				}
			}

		}

		if (operate.equals("del")) { // 删除连接线规则审核
			if (result) { // 通过
				Map<String, Object> officialLineMap = onlineSimpleDao.retrieve("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());
				onlineSimpleDao.delete("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());

				Map<String, Object> officialRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID)).getMap());
				//String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like '" + MapUtil.getString(officialRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";

				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(officialRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);
				List<Map<String, Object>> officialRelationList = onlineSimpleDao.queryForList(sql,conds);
				for (Map<String, Object> offMap : officialRelationList) {
					String oldJson = MapUtil.getString(offMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
					// 在原来的基础上修改RuleId的指向
					Map<String, Map<String, Object>> jsonMap = RuleJsonUtil.json2Maps(oldJson);
					Map<String, Object> ruleMap = MapUtil.getMap(jsonMap, MapUtil.getString(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID));
					List<String> directionalList = (ArrayList<String>) MapUtil.getObject(ruleMap, "DIRECTIONAL");
					if (directionalList != null)
						directionalList.remove(MapUtil.getString(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_CHILD_ID));

					// 替换后再转回JSON串
					oldJson = RuleJsonUtil.maps2Json(jsonMap);

					offMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, oldJson);
					offMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);

					onlineSimpleDao.update("TMS_COM_RULERELATION", offMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(offMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
				}

			} else {
				Map<String, Object> officialLineMap = onlineSimpleDao.retrieve("TMS_COM_RULE_CHILD", MapWrap.map(DBConstant.TMS_COM_RULE_CHILD_RC_ID, longId_l).getMap());
				offlineSimpleDao.create("TMS_COM_RULE_CHILD", officialLineMap);

				Map<String, Object> officialRuleMap = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, MapUtil.getLong(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID)).getMap());
				String sql = "select t.tab_name,r.* from tms_com_tab t left join tms_com_rulerelation r on t.tab_name=r.rulerel_txn where tab_name like :tabName";
				String tabName = "'" + MapUtil.getString(officialRuleMap, DBConstant.TMS_COM_RULE_RULE_TXN) + "%'";
				Map<String, Object> conds = new HashMap<String, Object>();
				conds.put("tabName", tabName);
				List<Map<String, Object>> tempRelationList = offlineSimpleDao.queryForList(sql,conds);
				for (Map<String, Object> reMap : tempRelationList) {
					String oldJson = MapUtil.getString(reMap, DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA);
					// 在原来的基础上修改RuleId的指向
					Map<String, Map<String, Object>> jsonMap = RuleJsonUtil.json2Maps(oldJson);
					Map<String, Object> ruleMap = MapUtil.getMap(jsonMap, MapUtil.getString(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_RULE_ID));
					List<String> directionalList = (ArrayList<String>) MapUtil.getObject(ruleMap, "DIRECTIONAL");
					if (directionalList != null)
						directionalList.add(MapUtil.getString(officialLineMap, DBConstant.TMS_COM_RULE_CHILD_CHILD_ID));

					// 替换后再转回JSON串
					oldJson = RuleJsonUtil.maps2Json(jsonMap);

					reMap.put(DBConstant.TMS_COM_RULERELATION_RULEREL_METADATA, oldJson);
					reMap.remove(DBConstant.TMS_COM_TAB.TAB_NAME);

					offlineSimpleDao.update("TMS_COM_RULERELATION", reMap, MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_ID, MapUtil.getLong(reMap, DBConstant.TMS_COM_RULERELATION_RULEREL_ID)).getMap());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#check(java.util.Map)
	 */
	public void check(Map<String, Object> reqs) {
		String cond = MapUtil.getString(reqs, "rule_cond");
		String txn_id = MapUtil.getString(reqs, "txn_id");
		String rule_name = MapUtil.getString(reqs, "rule_name");
		String ruleShortdesc = MapUtil.getString(reqs, "ruleShortdesc");
		String endble = MapUtil.getString(reqs, "enable");
		String oper = MapUtil.getString(reqs, "oper");

		// 校验条件有效性
		if (StringUtil.isNotEmpty(cond)) {
			// 初始化缓存
			commonCheckService.initCache(offlineDataSource);

			commonCheckService.checkCond(reqs, "rule_cond", "ruleShortdesc", "txn_id");
		}

		// 规则停用需要校验规则是否被引用，被引用的规则不能停用
		if (StringUtil.isNotEmpty(endble) && endble.equals("0")) {

			checkRef(txn_id, rule_name, ruleShortdesc, oper);

		}

	}

	public void checkRef(String txn_id, String rule_name, String ruleShortdesc, String oper) {

		if (rule_name == null || rule_name.length() == 0)
			return;

		// 校验能否被引用
		// 初始化缓存
		commonCheckService.initCache(offlineDataSource);
		if ("del".equals(oper)) {
			if (commonCheckService.find_ref_rule(txn_id, rule_name))
				throw new TmsMgrServiceException("[" + ruleShortdesc + "]被引用，不能删除");
		} else {
			if (commonCheckService.find_ref_valid_rule(txn_id, rule_name))
				throw new TmsMgrServiceException("[" + ruleShortdesc + "]被引用，不能停用");
		}

		// 初始化缓存
		commonCheckService.initCache(onlineDataSource);
		if ("del".equals(oper)) {
			if (commonCheckService.find_ref_rule(txn_id, rule_name))
				throw new TmsMgrServiceException("[" + ruleShortdesc + "]被未授权，不能删除");
		} else {
			if (commonCheckService.find_ref_valid_rule(txn_id, rule_name))
				throw new TmsMgrServiceException("[" + ruleShortdesc + "]被未授权，不能停用");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.rule.service.RuleService#batchCheckRef(java.lang.String[], java.lang.String)
	 */
	public void batchCheckRef(String[] rule_ids, String oper) {
		// 初始化临时库缓存
		commonCheckService.initCache(offlineDataSource);
		for (String rule_id : rule_ids) {
			Map<String, Object> ruleInfo = offlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, Long.parseLong(rule_id)).getMap());

			if ("del".equals(oper)) {
				if (commonCheckService.find_ref_rule(MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_TXN), MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_NAME)))
					throw new TmsMgrServiceException("[" + MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被引用，不能删除");
			} else {
				if (commonCheckService.find_ref_valid_rule(MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_TXN), MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_NAME)))
					throw new TmsMgrServiceException("[" + MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被引用，不能停用");
			}
		}

		// 初始化正式库缓存
		commonCheckService.initCache(onlineDataSource);
		for (String rule_id : rule_ids) {
			Map<String, Object> ruleInfo = onlineSimpleDao.retrieve("TMS_COM_RULE", MapWrap.map(DBConstant.TMS_COM_RULE_RULE_ID, Long.parseLong(rule_id)).getMap());

			if ("del".equals(oper)) {
				if (commonCheckService.find_ref_rule(MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_TXN), MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_NAME)))
					throw new TmsMgrServiceException("[" + MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被引用未授权，不能删除");
			} else {
				if (commonCheckService.find_ref_valid_rule(MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_TXN), MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_NAME)))
					throw new TmsMgrServiceException("[" + MapUtil.getString(ruleInfo, DBConstant.TMS_COM_RULE_RULE_SHORTDESC) + "]被引用未授权，不能停用");
			}
		}
	}
}
