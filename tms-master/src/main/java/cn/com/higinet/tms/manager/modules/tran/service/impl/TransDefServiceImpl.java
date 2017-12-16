package cn.com.higinet.tms.manager.modules.tran.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.auth.exception.TmsMgrAuthDepException;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.DBConstant.ROOT_NODE;
import cn.com.higinet.tms.manager.modules.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.CalendarUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;

/**
 * 
 * @author yangk
 */
@Service("transDefService")
public class TransDefServiceImpl implements TransDefService {

	@Autowired
	private SqlMap tmsSqlMap;

	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;

	@Autowired
	@Qualifier("offlineSimpleDao")
	private SimpleDao offlineSimpleDao;

	/**
	 * 返回全部可用交易
	 */
	public List<Map<String, Object>> getTranDefs() {

		// default ds is tmsDataSource
		// DSContextHolder.setDSType(DSType.TEMP);
		// log.error(tmsSimpleDao.queryForList("select * from tms_com_fd t where t.tab_name='T'").size());
		// DSContextHolder.setDSType(DSType.OFFICIAL);
		// log.error(tmsSimpleDao.queryForList("select * from tms_com_fd t where t.tab_name='T'").size());
		// DSContextHolder.clearDSType();

		StringBuffer sb = new StringBuffer();
		String symbol = ", ";
		Map<String, Object> cond = new HashMap<String, Object>();

		sb.append("SELECT ");
		sb.append(TMS_COM_TAB.TAB_NAME).append(symbol);
		sb.append(TMS_COM_TAB.TAB_DESC).append(symbol);
		sb.append(TMS_COM_TAB.IS_SYS).append(symbol);
		sb.append(TMS_COM_TAB.PARENT_TAB).append(symbol);
		sb.append(TMS_COM_TAB.CHANN).append(symbol);
		sb.append(TMS_COM_TAB.IS_ENABLE).append(symbol);
		sb.append(TMS_COM_TAB.SHOW_ORDER).append(symbol);
		sb.append(TMS_COM_TAB.TXNID);
		sb.append(" FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" WHERE ");
		// sb.append(TMS_COM_TAB.IS_ENABLE + " = :IS_ENABLE AND ");
		sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE");
		sb.append(" ORDER BY ").append(TMS_COM_TAB.SHOW_ORDER);

		// 条件为:启用并且表类型为交易模型
		cond.put(TMS_COM_TAB.IS_ENABLE, Integer.parseInt(StaticParameters.STATUS_1));
		cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
		List<Map<String, Object>> sss = tmsSimpleDao.queryForList(sb.toString(), cond);
		return sss;
	}

	public List<Map<String, Object>> getTranRules() {

		StringBuffer sb = new StringBuffer();
		String symbol = ", ";
		Map<String, Object> cond = new HashMap<String, Object>();

		sb.append("SELECT ");
		sb.append(DBConstant.TMS_COM_RULE_RULE_ID).append(" ").append(TMS_COM_TAB.TAB_NAME).append(symbol);
		sb.append(DBConstant.TMS_COM_RULE_RULE_SHORTDESC).append(" ").append(TMS_COM_TAB.TAB_DESC).append(symbol);
		sb.append("''").append(" ").append(TMS_COM_TAB.IS_SYS).append(symbol);
		sb.append(DBConstant.TMS_COM_RULE_RULE_TXN).append(" ").append(TMS_COM_TAB.PARENT_TAB).append(symbol);
		sb.append("''").append(" ").append(TMS_COM_TAB.CHANN).append(symbol);
		sb.append(DBConstant.TMS_COM_RULE_RULE_ENABLE).append(" ").append(TMS_COM_TAB.IS_ENABLE).append(symbol);
		sb.append(DBConstant.TMS_COM_RULE_RULE_RULE_ORDER).append(" ").append(TMS_COM_TAB.SHOW_ORDER).append(symbol);
		sb.append(DBConstant.TMS_COM_RULE_RULE_TXN).append(" ").append(TMS_COM_TAB.TXNID);
		sb.append(" FROM ");
		sb.append(DBConstant.TMS_COM_RULE);
		sb.append(" ORDER BY ").append(DBConstant.TMS_COM_RULE_RULE_RULE_ORDER);

		List<Map<String, Object>> sss = tmsSimpleDao.queryForList(sb.toString(), cond);
		return sss;
	}

	/**
	 * 新建交易
	 */
	@Transactional
	private Map createTran(List addList) {

		Map<String, String> reqs = (Map<String, String>) addList.get(0);

		String par_name = MapUtil.getString(reqs, "parent_tab");
		String tab_desc = MapUtil.getString(reqs, "tab_desc");
		String txnid = MapUtil.getString(reqs, "txnid");

		// check
		// S1:名字/顺序/渠道,不与同级其他相同
		// S2:如果上级有标识,不让选交易标识

		@SuppressWarnings("rawtypes")
		Map bro_par_map = getBroAndFarther("", par_name);
		List<Map<String, Object>> brolist = MapUtil.getList(bro_par_map, "bro");
		Map<String, Object> parMap = MapUtil.getMap(bro_par_map, "farther");
		checkSame(reqs, brolist, parMap); // check brother
		checkSingleParentAndAllSubSame(null, tab_desc, par_name, true, "def", true);
		checkTxnId(txnid, false, "");

		// 算主键id
		String tab_name = checkAndGenericTabName(brolist, par_name);

		Map<String, Object> clms = new HashMap<String, Object>();

		clms.put(TMS_COM_TAB.TAB_NAME, tab_name);
		clms.put(TMS_COM_TAB.TAB_TYPE, "4");
		clms.put(TMS_COM_TAB.IS_QUERY, 1);
		clms.put(TMS_COM_TAB.IS_SYS, 1);
		clms.put(TMS_COM_TAB.PARENT_TAB, par_name);

		clms.put(TMS_COM_TAB.CHANN, MapUtil.getString(reqs, "chann"));
		clms.put(TMS_COM_TAB.TAB_DESC, MapUtil.getString(reqs, "tab_desc"));
		clms.put(TMS_COM_TAB.SHOW_ORDER, MapUtil.getLong(reqs, "show_order"));
		clms.put(TMS_COM_TAB.IS_ENABLE, MapUtil.getLong(reqs, "is_enable"));
		clms.put(TMS_COM_TAB.TXNID, MapUtil.getString(reqs, "txnid"));
		clms.put(TMS_COM_TAB.MODELUSED, MapUtil.getString(reqs, "MODELUSED"));
		clms.put(TMS_COM_TAB.BASE_TAB, "TMS_RUN_TRAFFICDATA");
		clms.put(TMS_COM_TAB.CAN_REF, 0);
		clms.put(TMS_COM_TAB.TAB_DISPOSAL, MapUtil.getString(reqs, "TAB_DISPOSAL"));

		// 将交易主键id放到service方法createTran的参数中，双审需要。add by wangsch 2013-08-09
		reqs.put("tab_name", tab_name);

		// 解决bug：在同一个父交易下，删除交易后再添加交易，双审授权记录异常>add by wangsch 2013-10-24
		/*
		 * String sql = "select auth.*,record.real_oper from tms_mgr_authinfo auth,tms_mgr_authrecord record" + " where record.auth_id = convert(varchar,auth.auth_id) and query_pkvalue = '"+tab_name+"' and module_id='tranConf' and auth_status='0'" + " and record.real_oper = 'd' and record.is_main = '1'";
		 */
		String sql = tmsSqlMap.getSql("tms.auth.query");
		List<Map<String, Object>> list = offlineSimpleDao.queryForList(sql, tab_name);// 授权的表都在离线库

		StringBuffer sb = new StringBuffer();
		for (Map<String, Object> depInfoMap : list) {
			sb.append("模块名->" + MapUtil.getString(depInfoMap, "MODULE_NAME") + "，");
			sb.append("授权编号->" + MapUtil.getString(depInfoMap, "AUTH_ID") + "；");
		}
		String depInfo = sb.toString();
		if (!StringUtil.isEmpty(depInfo)) {
			throw new TmsMgrAuthDepException("新建交易失败，请先完成必要的授权：" + sb.toString());
		}

		tmsSimpleDao.create(TMS_COM_TAB.TABLE_NAME, clms);

		return reqs;
	}

	/**
	 * 修改交易
	 */
	@Transactional
	private Map updateTran(List modList) {

		Map<String, String> reqs = (Map<String, String>) modList.get(0);
		String tab_name = MapUtil.getString(reqs, "tab_name");
		String par_name = MapUtil.getString(reqs, "parent_tab");
		String chann = MapUtil.getString(reqs, "chann");
		String txnid = MapUtil.getString(reqs, "txnid");
		String show_order = MapUtil.getString(reqs, "show_order");
		String tab_desc = MapUtil.getString(reqs, "tab_desc");
		String tab_disposal = MapUtil.getString(reqs, "TAB_DISPOSAL");

		// check
		// S1:名字/顺序/渠道,不与同级其他相同,可以与自己相同
		// S2:如果有下级,渠道/交易标识不让修改

		@SuppressWarnings("rawtypes")
		Map bro_par_map = getBroAndFarther(tab_name, par_name);
		List<Map<String, Object>> brolist = MapUtil.getList(bro_par_map, "bro");
		Map<String, Object> parMap = MapUtil.getMap(bro_par_map, "farther");
		// List<Map<String, Object>> allBrolist = getAllBroList("", par_name);
		// List<Map<String, Object>> brolist = getEffectiveBroList(allBrolist);
		checkSame(reqs, brolist, parMap);
		checkSingleParentAndAllSubSame(tab_name, tab_desc, tab_name, false, "def", false);

		// 校验处置方式
		checkTab_disposal(tab_name, tab_disposal);

		Map<String, Object> old = getNodeFullInfos(tab_name);
		String o_chann = MapUtil.getString(old, "CHANN");
		String o_trans_chann = StringUtil.isEmpty(o_chann) ? "" : o_chann;

		String o_txnid = MapUtil.getString(old, "TXNID");
		String o_trans_txnid = StringUtil.isEmpty(o_chann) ? "" : o_txnid;

		if (StringUtil.isNotEmpty(txnid)) {
			checkTxnId(txnid, true, tab_name);
		}

		if (!chann.equals(o_trans_chann) || !txnid.equals(o_trans_txnid)) { // 字段有变动
			hasSub(tab_name);
		}

		// /*
		// * 选择了交易识别码时,检查当前交易是否是最下层交易
		// * 如果还有下级交易则不许修改为带交易识别码
		// */
		// if(StringUtil.isNotEmpty(txnid)) { //选择了交易识别码
		// canWeDoThat("update", tab_name);
		// }
		//
		// checkAndGenericTabName(reqs, "update");

		Map<String, Object> row = new HashMap<String, Object>();
		Map<String, String> conds = new HashMap<String, String>();

		row.put(TMS_COM_TAB.CHANN, chann);
		row.put(TMS_COM_TAB.TAB_DESC, MapUtil.getString(reqs, "tab_desc"));

		String traindate = MapUtil.getString(reqs, "TRAINDATE");
		String modelused = MapUtil.getString(reqs, "MODELUSED");
		String modelused_o = MapUtil.getString(reqs, "MODELUSED_O");

		row.put(TMS_COM_TAB.MODELUSED, modelused);

		if (modelused != null && !modelused.equals(modelused_o)) {// 模型状态被修改
			String curdate = CalendarUtil.getCalendarByFormat(CalendarUtil.FORMAT10);
			if ("1".equals(modelused)) { // 改为模型学习期
				if (!"2".equals(modelused_o)) {// 由运行期改为学习期，不修改学习期开始时间
					row.put(TMS_COM_TAB.TRAINDATE, curdate);
				}
			} else if ("2".equals(modelused))// 改为模型运行期
			{
				// 查询模型表，看交易是否存在模型。只有存在模型才能由模型学习期修改为运行期
				String mTrain_sql = "SELECT * FROM TMS_COM_MTRAIN WHERE TXNID = ? AND TRAINDATE = ?";
				List mtrain_list = tmsSimpleDao.queryForList(mTrain_sql, tab_name, traindate);
				if (mtrain_list == null || mtrain_list.size() == 0)
					throw new TmsMgrServiceException("只有存在风险模型，才能由模型学习期修改为模型运行期！");

				row.put(TMS_COM_TAB.RUNDATE, curdate);
			}
		}

		row.put(TMS_COM_TAB.SHOW_ORDER, StringUtil.isEmpty(show_order) ? 0 : Integer.parseInt(show_order));
		row.put(TMS_COM_TAB.IS_ENABLE, MapUtil.getLong(reqs, "is_enable"));
		row.put(TMS_COM_TAB.TXNID, txnid);
		row.put(TMS_COM_TAB.TAB_DISPOSAL, tab_disposal);

		conds.put(TMS_COM_TAB.TAB_NAME, MapUtil.getString(reqs, "tab_name"));

		tmsSimpleDao.update(TMS_COM_TAB.TABLE_NAME, row, conds);

		return reqs;
	}

	/**
	 * 方法描述:校验处置方式是否已经被子交易或规则使用
	 * 
	 * @param txnid
	 * @param tab_disposal
	 */
	private void checkTab_disposal(String txnid, String tab_disposal) {
		String[] ts = tab_disposal.split(",");
		String child_sql = "SELECT * FROM TMS_COM_TAB WHERE TAB_NAME LIKE :tabName AND TAB_NAME!=:tabName1";
		Map<String, Object> cond = new HashMap<String, Object>();
		cond.put("tabName", "'" + txnid + "%'");
		cond.put("tabName1", txnid);

		List<Map<String, Object>> child_txn = tmsSimpleDao.queryForList(child_sql, cond);
		for (int i = 0; child_txn != null && i < child_txn.size(); i++) {
			Map<String, Object> child = child_txn.get(i);
			String child_disposal = MapUtil.getString(child, "TAB_DISPOSAL");
			if (child_disposal.length() > 0) {
				String[] ds = child_disposal.split(",");
				for (int j = 0; j < ds.length; j++) {
					boolean isFind = false;
					for (int j2 = 0; j2 < ts.length; j2++) {
						if (ds[j].equals(ts[j2])) {
							isFind = true;
							break;
						}
					}
					if (!isFind) {
						throw new TmsMgrWebException("处置策略已被子交易[" + MapUtil.getString(child, "TAB_DESC") + "]使用！");
					}
				}
			}
		}

		String rule_sql = "SELECT * FROM TMS_COM_RULE WHERE RULE_TXN LIKE :txnId1 AND RULE_TXN!=:txnId2 AND DISPOSAL NOT IN (" + "'" + tab_disposal.replace(",", "','") + "'" + ")";
		Map<String, Object> cond2 = new HashMap<String, Object>();
		cond2.put("txnId1", txnid + "%");
		cond2.put("txnId2", txnid);
		if (tmsSimpleDao.count(rule_sql, cond2) > 0)
			throw new TmsMgrWebException("处置策略已被规则使用！");

	}

	/**
	 * 物理删除交易
	 */
	@Transactional
	private Map deleteTran(List list, String type) {

		Map<String, Object> reqs = (Map<String, Object>) list.get(0);
		Map<String, String> conds = new HashMap<String, String>();
		String tab_name = MapUtil.getString(reqs, "tab_name");
		// 当前交易有子交易,不让删
		checkHasSub("delete", tab_name);
		// 当交易有开关等属性不让删
		checkTxnAttrs(tab_name);

		conds.put(TMS_COM_TAB.TAB_NAME, tab_name);

		// tmsSimpleDao.delete("TMS_COM_RULERELATION",MapWrap.map(DBConstant.TMS_COM_RULERELATION_RULEREL_TXN, tab_name).getMap());// 当前交易下规则全删除后，还会有父交易的规则图需要删除。

		tmsSimpleDao.delete(TMS_COM_TAB.TABLE_NAME, conds);

		return conds;
	}

	/*
	 * 检查是否可update或delete
	 * 
	 * @param method update或delete
	 * 
	 * @param parent_tab 父id
	 */
	private void checkHasSub(String method, String parent_tab) {

		StringBuffer sb = new StringBuffer();
		Map<String, String> cond = new HashMap<String, String>();
		sb.append("SELECT 1 FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" WHERE ");
		// sb.append(TMS_COM_TAB.IS_ENABLE + " = :IS_ENABLE AND ");
		sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE AND ");
		sb.append(TMS_COM_TAB.PARENT_TAB + " = :PARENT_TAB");

		// 条件为:启用并且表类型为交易模型
		// cond.put(TMS_COM_TAB.IS_ENABLE, StaticParameters.STATUS_1);
		cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
		cond.put(TMS_COM_TAB.PARENT_TAB, parent_tab);

		long num = tmsSimpleDao.count(sb.toString(), cond);

		if (num > 0) {
			throw new TmsMgrWebException(switchErrorMsg(method));
		}
	}

	/*
	 * 检查是否可update或delete
	 * 
	 * @param method update或delete
	 * 
	 * @param parent_tab 父id
	 */
	private void checkTxnAttrs(String tab_name) {

		StringBuffer sb = new StringBuffer();
		sb.append("select rfd.*, ftab.*, ru.*, st.*, fd.* , sw.*");
		sb.append(" from tms_com_tab tab");
		sb.append(" left join tms_com_reffd rfd on rfd.tab_name = tab.tab_name");
		sb.append(" left join tms_com_reftab ftab on ftab.tab_name = tab.tab_name");
		sb.append(" left join tms_com_rule ru on ru.rule_txn = tab.tab_name");
		sb.append(" left join tms_com_strategy sw on sw.st_txn = tab.tab_name");
		sb.append(" left join tms_com_stat st on st.stat_txn = tab.tab_name");
		sb.append(" left join tms_com_fd fd on fd.tab_name = tab.tab_name");
		sb.append(" where tab.tab_name = ?");

		List<Map<String, Object>> num = tmsSimpleDao.queryForList(sb.toString(), tab_name);

		if (num.size() > 1) {
			throw new TmsMgrServiceException("该交易下有属性,请先删除属性");
		} else if (num.size() == 1) {
			Set<Entry<String, Object>> set = num.get(0).entrySet();
			for (Map.Entry<String, Object> col_entry : set) {
				// Object c = col_entry.getValue();
				// if (StringUtil.isNotEmpty((String)col_entry.getValue())){
				if (col_entry.getValue() != null) {
					throw new TmsMgrServiceException("该交易下有属性,请先删除属性");
				}
			}
		}
	}

	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null.
	 * 
	 * @param reference
	 *            an object reference
	 * @return the non-null reference that was validated
	 * @throws NullPointerException
	 *             if {@code reference} is null
	 */
	public static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	/*
	 * 以update or delete为标识,返回对应的错误信息
	 * 
	 * @param method update or delete
	 * 
	 * @return
	 */
	private String switchErrorMsg(String method) {

		if ("update".equals(method)) {
			return "当前交易有下级交易,无法配置交易识别标识";
		} else if ("create".equals(method)) {
			return "父交易已有交易识别标识,无法再次配置";
		} else {
			return "当前交易有下级交易,无法删除";
		}
	}

	// /*
	// * 去同级节点
	// * 如果self不为空则过滤自己
	// */
	// private List<Map<String,Object>> getBroList(String self, String par_name){
	//
	// StringBuffer sb = new StringBuffer();
	// Map<String, String> cond = new HashMap<String, String>();
	// sb.append("SELECT * FROM ");
	// sb.append(TMS_COM_TAB.TABLE_NAME);
	// sb.append(" WHERE ");
	// // 失效的交易也计数
	// sb.append(TMS_COM_TAB.PARENT_TAB + " = :PARENT_TAB AND  ");
	// sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE ");
	//
	// cond.put(TMS_COM_TAB.PARENT_TAB, par_name);
	// cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
	//
	// // if(StringUtil.isNotEmpty(self)){
	// //
	// // sb.append(" AND ").append(TMS_COM_TAB.IS_ENABLE + " = :IS_ENABLE");
	// // cond.put(TMS_COM_TAB.IS_ENABLE, StaticParameters.STATUS_1);
	// // }
	//
	// List<Map<String,Object>> list = tmsSimpleDao.queryForList(sb.toString(), cond);
	//
	// if(StringUtil.isEmpty(self)){
	// return list;
	// } else {
	// int i=0;
	// for(; i < list.size(); i++){
	//
	// Map<String,Object> row = list.get(i);
	// String db_tab_name = MapUtil.getString(row, TMS_COM_TAB.TAB_NAME);
	// if(db_tab_name.equals(self)){
	// break;
	// }
	// }
	// list.remove(i);
	// }
	//
	// return list;
	// }
	//
	// private List<Map<String,Object>> getEffectiveBroList(List<Map<String,Object>> list){
	//
	// List<Map<String,Object>> effList= new ArrayList<Map<String,Object>>();
	// for(int i=0; i < list.size(); i++){
	//
	// Map<String,Object> row = list.get(i);
	// String is_enable = MapUtil.getString(row, TMS_COM_TAB.IS_ENABLE);
	// if(!"0".equals(is_enable)){
	// effList.add(row);
	// }
	// }
	//
	// return effList;
	// }

	/*
	 * 取父节点,兄弟节点和自己
	 */
	@SuppressWarnings("all")
	private Map getBroAndFarther(String self_tab_name, String par_name) {

		StringBuffer sb = new StringBuffer();
		Map<String, String> cond = new HashMap<String, String>();
		sb.append("SELECT * FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" WHERE ");
		sb.append("(");
		sb.append(TMS_COM_TAB.PARENT_TAB + " = :PARENT_TAB OR ");
		sb.append(TMS_COM_TAB.TAB_NAME + " = :TAB_NAME) AND ");
		sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE ");

		cond.put(TMS_COM_TAB.PARENT_TAB, par_name);
		cond.put(TMS_COM_TAB.TAB_NAME, par_name);
		cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);

		List<Map<String, Object>> list = tmsSimpleDao.queryForList(sb.toString(), cond);

		List<Map<String, Object>> bro = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> self = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> farther = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> col : list) {

			String col_tab_name = MapUtil.getString(col, TMS_COM_TAB.TAB_NAME);

			if (self_tab_name.equals(col_tab_name)) { // self
				self.add(col);
			} else if (par_name.equals(col_tab_name)) { // parent
				farther.add(col);
			} else {
				bro.add(col);
			}
		}

		Map fam = new HashMap();

		if (bro.size() > 0) {
			fam.put("bro", bro);
		}

		if (self.size() > 0) {
			fam.put("self", self.get(0));
		}

		if (farther.size() > 0) {
			fam.put("farther", farther.get(0));
		}

		return fam;
	}

	// /*
	// * 去同级节点,包括失效的
	// */
	// private List<Map<String,Object>> getAllBroList(String self, String par_name){
	//
	// StringBuffer sb = new StringBuffer();
	// Map<String, String> cond = new HashMap<String, String>();
	// sb.append("SELECT * FROM ");
	// sb.append(TMS_COM_TAB.TABLE_NAME);
	// sb.append(" WHERE ");
	// sb.append(TMS_COM_TAB.PARENT_TAB + " = :PARENT_TAB AND  ");
	// sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE ");
	//
	// cond.put(TMS_COM_TAB.PARENT_TAB, par_name);
	// cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
	//
	// List<Map<String,Object>> list = tmsSimpleDao.queryForList(sb.toString(), cond);
	//
	// if(StringUtil.isEmpty(self)){
	// return list;
	// } else {
	// int i=0;
	// boolean isBreak = false;
	// for(; i < list.size(); i++){
	//
	// Map<String,Object> row = list.get(i);
	// String db_tab_name = MapUtil.getString(row, TMS_COM_TAB.TAB_NAME);
	// if(db_tab_name.equals(self)){
	// isBreak = true;
	// break;
	// }
	// }
	// if(isBreak)
	// list.remove(i);
	// else
	// list.remove(--i);
	// }
	//
	// return list;
	// }

	/*
	 * 检查同级有无重复,顺序/渠道/名称
	 */
	private void checkSame(Map<String, String> reqs, List<Map<String, Object>> brolist, Map<String, Object> par) {

		String tab_desc = MapUtil.getString(reqs, "tab_desc");
		String show_order = MapUtil.getString(reqs, "show_order");
		String chann = MapUtil.getString(reqs, "chann");

		if (brolist != null && brolist.size() > 0) {
			for (Map<String, Object> row : brolist) {
				String db_show_order = MapUtil.getString(row, TMS_COM_TAB.SHOW_ORDER);
				String db_tab_desc = MapUtil.getString(row, TMS_COM_TAB.TAB_DESC);
				String db_chann = MapUtil.getString(row, TMS_COM_TAB.CHANN);

				if (show_order.equals(db_show_order)) {
					throw new TmsMgrWebException("重复的顺序");
				}
				if (StringUtil.isEmpty(MapUtil.getString(par, TMS_COM_TAB.CHANN)) && StringUtil.isNotEmpty(db_chann) && chann.equals(db_chann)) {
					throw new TmsMgrWebException("重复的渠道");
				}
				if (tab_desc.equals(db_tab_desc)) {
					throw new TmsMgrWebException("重复的名称");
				}
			}
		}
	}

	/*
	 * 
	 */
	public List<Map<String, Object>> getSameInfoDetail(String tab_desc, String tab_name, boolean isParentName, String type) {

		List<Map<String, Object>> famliy = getImmediateFamilyAttrInfos(tab_name, type, isParentName);
		List<Map<String, Object>> sameList = new ArrayList<Map<String, Object>>();

		if (famliy != null && famliy.size() > 0) {

			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

			for (Map<String, Object> ec : famliy) {
				String db_tab_name = MapUtil.getString(ec, "x_txn_name");
				if (!db_tab_name.startsWith("TMS")) {
					list.add(ec);
				}
			}

			if (list != null && list.size() > 0) {
				for (Map<String, Object> row : list) {

					String db_desc = MapUtil.getString(row, "x_desc");
					// String db_tab_name = MapUtil.getString(row, "x_txn_name");

					// if(!isParentName && db_tab_name.equals(tab_name)){
					// continue;
					// }

					if (tab_desc.equals(db_desc)) {
						sameList.add(row);
					}
				}
			}

		}

		return sameList;
	}

	/**
	 * id:主键id tab_desc:网上银行 tab_name:t01 isParentNmae:false/true 是不是父亲的tab_name type:sw/rule/tab/ref_tab isChg:add/mod
	 * 
	 */
	public void checkSingleParentAndAllSubSame(String id, String tab_desc, String tab_name, boolean isParentName, String type, boolean isChg) {

		List<Map<String, Object>> sameList = getSameInfoDetail(tab_desc, tab_name, isParentName, type);
		int sameCount = sameList.size();

		if (StringUtil.isNotEmpty(id) && !tab_desc.equals(getOriginalVal(id, type))) {
			isChg = true;
		}

		if (isChg && sameCount > 0) {
			throw new TmsMgrWebException(getErrMsg(tab_desc, type, sameList));
		} else if (!isChg && sameCount > 1) {
			throw new TmsMgrWebException(getErrMsg(tab_desc, type, sameList));
		}
	}

	private String getOriginalVal(String id, String type) {

		// select sw.sw_desc from tms_com_switch sw where sw.sw_txn in ('T090101', 'T0901', 'T09', 'T');
		String[] ids = id.split("\\|");
		List<Map<String, Object>> list = null;

		Object[] arg = getIdNameByType(type, ids);

		StringBuffer sb = new StringBuffer("SELECT ");
		sb.append(arg[0]).append(" x_desc FROM ").append(arg[1]).append(" WHERE ");
		sb.append(arg[2]);

		list = tmsSimpleDao.queryForList(sb.toString(), (Map) arg[3]);

		String val = null;
		if (list != null && list.size() > 0) {
			val = MapUtil.getString(list.get(0), "x_desc");
		}

		return val;
	}

	private String getErrMsg(String tab_desc, String type, List<Map<String, Object>> sameList) {
		String txnid = MapUtil.getString(sameList.get(0), "x_txn_name");

		String[] arg = getInfoDetailByType(type);
		StringBuffer err_msg = new StringBuffer();
		err_msg.append("当前");
		err_msg.append(arg[4]);
		err_msg.append("(");
		err_msg.append(tab_desc);
		err_msg.append(")与");
		err_msg.append("[");
		err_msg.append(getSelfAndParentTranDefAsStr(txnid));
		err_msg.append("]中");
		err_msg.append(arg[4]);
		err_msg.append("重复");
		return err_msg.toString();
	}

	private List<Map<String, Object>> getImmediateFamilyAttrInfos(String tab_name, String type, boolean isParentName) {

		// select sw.sw_desc from tms_com_switch sw where sw.sw_txn in ('T090101', 'T0901', 'T09', 'T');
		String[] arg = getInfoDetailByType(type);

		StringBuffer sb = new StringBuffer("SELECT ");
		sb.append(arg[0]).append(" x_desc,").append(arg[2]).append(" x_txn_name FROM ").append(arg[1]).append(" WHERE ");
		sb.append(arg[3]);
		sb.append("(").append(arg[2]).append(" IN (").append(TransCommon.cutToIdsForSql(tab_name)).append(")");
		sb.append(" or ").append(arg[2]).append(" like '").append(tab_name);
		if (isParentName) {
			sb.append("__')");
		} else {
			sb.append("%')");
		}
		// List<Map<String, Object>> immediateFamily = tmsSimpleDao.queryForList(sb.toString());
		return tmsSimpleDao.queryForList(sb.toString());
	}

	private String[] getInfoDetailByType(String type) {

		String[] arg = new String[5];

		if (type.equals("def")) {
			arg[0] = "tab_desc";
			arg[1] = "tms_com_tab";
			arg[2] = "tab_name";
			arg[3] = " tab_type='4' and ";
			arg[4] = "名称";
		} else if (type.equals("sw")) {
			arg[0] = "sw_desc";
			arg[1] = "tms_com_switch";
			arg[2] = "sw_txn";
			arg[3] = "";
			arg[4] = "开关名称";
		} else if (type.equals("fd")) {
			arg[0] = "ref_name";
			arg[1] = "tms_com_fd";
			arg[2] = "tab_name";
			arg[3] = "";
			arg[4] = "交易模型/属性代码";
		} else if (type.equals("fd_name")) {
			arg[0] = "name";
			arg[1] = "tms_com_fd";
			arg[2] = "tab_name";
			arg[3] = "";
			arg[4] = "交易模型/属性名称";
		} else if (type.equals("reffd_ref_desc")) {
			arg[0] = "ref_desc";
			arg[1] = "tms_com_reffd";
			arg[2] = "tab_name";
			arg[3] = "";
			arg[4] = "模型引用/属性名称";
		} else if (type.equals("reftab")) {
			arg[0] = "ref_desc";
			arg[1] = "tms_com_reftab";
			arg[2] = "tab_name";
			arg[3] = "";
			arg[4] = "引用表引用描述";
		} else if (type.equals("reffd")) {
			arg[0] = "ref_name";
			arg[1] = "tms_com_reffd";
			arg[2] = "tab_name";
			arg[3] = "";
			arg[4] = "模型引用/属性代码";
		} else if (type.equals("rule")) {
			arg[0] = "rule_shortdesc";
			arg[1] = "tms_com_rule";
			arg[2] = "rule_txn";
			arg[3] = "";
			arg[4] = "规则名称";
		} else {
			arg[0] = "ps_name";
			arg[1] = "tms_com_process";
			arg[2] = "ps_txn";
			arg[3] = "";
			arg[4] = "处置名称";
		}

		return arg;
	}

	private Object[] getIdNameByType(String type, String[] ids) {

		Object[] arg = new Object[4];
		Map<String, Object> cond = new HashMap<String, Object>();
		if (type.equals("def")) {
			arg[0] = "tab_desc";
			arg[2] = "tab_name=:tab_name";
			arg[1] = "tms_com_tab";
			cond.put("tab_name", ids[0]);
		} else if (type.equals("sw")) {
			arg[0] = "sw_desc";
			arg[2] = "sw_id=:sw_id";
			arg[1] = "tms_com_switch";
			cond.put("sw_id", Long.parseLong(ids[0]));
		} else if (type.equals("fd")) {
			arg[0] = "ref_name";
			arg[1] = "tms_com_fd";
			arg[2] = "ref_name=:ref_name and tab_name=:tab_name";
			cond.put("ref_name", ids[0]);
			cond.put("tab_name", ids[1]);
		} else if (type.equals("fd_name")) {
			arg[0] = "name";
			arg[1] = "tms_com_fd";
			arg[2] = "ref_name=:ref_name and tab_name=:tab_name";
			cond.put("ref_name", ids[0]);
			cond.put("tab_name", ids[1]);
		}

		else if (type.equals("reffd")) {
			arg[0] = "ref_name";
			arg[2] = "ref_name=:ref_name and tab_name=:tab_name";
			arg[1] = "TMS_COM_REFFD";
			cond.put("ref_name", ids[0]);
			cond.put("tab_name", ids[1]);
			if (ids.length == 3) {
				arg[2] = String.valueOf(arg[2]) + " and ref_id=:ref_id";
				cond.put("ref_id", ids[2]);
			}
		} else if (type.equals("reffd_ref_desc")) {
			arg[0] = "ref_desc";
			arg[2] = "ref_name=:ref_name and tab_name=:tab_name and ref_id=:ref_id";
			arg[1] = "TMS_COM_REFFD";
			cond.put("ref_name", ids[0]);
			cond.put("tab_name", ids[1]);
			cond.put("ref_id", Long.parseLong(ids[2]));
		}

		else if (type.equals("reftab")) {
			arg[0] = "ref_desc";
			arg[2] = "ref_id=:ref_id";
			arg[1] = "tms_com_reftab";
			cond.put("ref_id", Long.parseLong(ids[0]));
		} else if (type.equals("rule")) {
			arg[0] = "rule_shortdesc";
			arg[2] = "rule_id=:rule_id";
			arg[1] = "tms_com_rule";
			cond.put("rule_id", Long.parseLong(ids[0]));
		} else {
			arg[0] = "ps_name";
			arg[2] = "ps_id=:ps_id";
			arg[1] = "tms_com_process";
			cond.put("ps_id", Long.parseLong(ids[0]));
		}
		arg[3] = cond;
		return arg;
	}

	private void hasSub(String tab_name) {

		StringBuffer sb = new StringBuffer();
		Map<String, String> cond = new HashMap<String, String>();
		sb.append("SELECT * FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" WHERE ");
		// 失效的交易也计数
		// sb.append(TMS_COM_TAB.IS_ENABLE + " = :IS_ENABLE AND ");
		sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE AND ");
		sb.append(TMS_COM_TAB.PARENT_TAB + " = :PARENT_TAB");

		// 条件为:父id=?并且表类型为交易模型
		// cond.put(TMS_COM_TAB.IS_ENABLE, StaticParameters.STATUS_1);
		cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
		cond.put(TMS_COM_TAB.PARENT_TAB, tab_name);

		long count = tmsSimpleDao.count(sb.toString(), cond);

		if (count > 0) {
			throw new TmsMgrWebException("当前交易有下级交易,不能修改交易类型,渠道或交易标识");
		}
	}

	private void checkTxnId(String txnId, boolean isModify, String tab_name) {

		if (StringUtil.isEmpty(txnId))
			return;

		StringBuffer sb = new StringBuffer();
		Map<String, String> cond = new HashMap<String, String>();
		sb.append("SELECT * FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" WHERE ");
		// 失效的交易也计数
		sb.append(TMS_COM_TAB.TAB_TYPE + " = :TAB_TYPE AND ");
		sb.append(TMS_COM_TAB.TXNID + " = :TXNID ");
		// sb.append(TMS_COM_TAB.TAB_NAME + " = :TAB_NAME");

		// 条件为:父id=?并且表类型为交易模型
		cond.put(TMS_COM_TAB.TXNID, txnId);
		cond.put(TMS_COM_TAB.TAB_TYPE, StaticParameters.TAB_TYPE_4);
		// cond.put(TMS_COM_TAB.TAB_NAME, par_name);

		List<Map<String, Object>> list = tmsSimpleDao.queryForList(sb.toString(), cond);

		if (list.size() == 1 && isModify) {

			String exist_tab_name = MapUtil.getString(list.get(0), TMS_COM_TAB.TAB_NAME);
			if (!exist_tab_name.equals(tab_name)) {
				throw new TmsMgrServiceException("交易识别标识重复");
			}
		} else if (list.size() > 0) {
			throw new TmsMgrServiceException("交易识别标识重复");
		}
	}

	/*
	 * 根据其父交易主键,定义当前交易主键,即 同级交易个数+1,小于10补"0"
	 */
	private String checkAndGenericTabName(List<Map<String, Object>> brolist, String par_name) {

		int num = brolist != null && brolist.size() > 0 ? brolist.size() : 0;
		int last_comp_tmp = 0;

		if (++num > 99) {
			throw new TmsMgrWebException("当前业务下业务/交易过多,请尝试在其他的建立");
		}

		if (brolist != null && brolist.size() > 0) {
			for (Map<String, Object> brMap : brolist) { // 找最大id
				// root节点不允许建立同级交易,tab_name肯定有后两位
				String tab_name = MapUtil.getString(brMap, TMS_COM_TAB.TAB_NAME);
				String last_2_ch = tab_name.substring(tab_name.length() - 2, tab_name.length());
				int order = Integer.parseInt(last_2_ch);
				last_comp_tmp = last_comp_tmp > order ? last_comp_tmp : order;
			}
		}

		++last_comp_tmp;

		String last_2_ch = last_comp_tmp / 10 > 0 ? last_comp_tmp + "" : "0" + last_comp_tmp;

		return par_name + last_2_ch;
	}

	/**
	 * 取父节点信息
	 */
	public Map<String, Object> getParent(String tab_name) {
		// select *
		// from tms_com_tab t
		// where t.is_enable = '1'
		// and t.tab_type = '4'
		// and t.tab_name = (select parent_tab
		// from tms_com_tab tt
		// where tt.parent_tab = t.tab_name
		// and tt.tab_name = 'T01')
		StringBuffer sb = new StringBuffer();

		sb.append("SELECT * FROM ");
		sb.append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" par");
		sb.append(" WHERE ");
		sb.append(" par." + TMS_COM_TAB.IS_ENABLE + "= ? ");
		sb.append(" AND par." + TMS_COM_TAB.TAB_TYPE + "= ? ");
		sb.append(" AND par.").append(TMS_COM_TAB.TAB_NAME + " = ");

		sb.append(switchSql(tab_name));

		List<Map<String, Object>> rows = tmsSimpleDao.queryForList(sb.toString(), StaticParameters.STATUS_1, StaticParameters.TAB_TYPE_4, tab_name);

		if (rows != null && rows.size() != 1) {
			throw new TmsMgrWebException("查询当前交易的所属交易出错");
		}
		return rows.get(0);
	}

	/*
	 * 当查询T(root节点)时候,无上级节点,显示root信息 非T时,取其下级节点填充信息
	 */
	private String switchSql(String tab_name) {

		if (ROOT_NODE.TAB_NAME.equals(tab_name)) {
			return "?";
		}

		StringBuffer sb = new StringBuffer();

		sb.append(" (SELECT ");
		sb.append(TMS_COM_TAB.PARENT_TAB);
		sb.append(" FROM ").append(TMS_COM_TAB.TABLE_NAME).append(" son");
		sb.append(" WHERE ");
		sb.append(" son.").append(TMS_COM_TAB.PARENT_TAB).append("=").append("par.").append(TMS_COM_TAB.TAB_NAME);
		sb.append(" AND son." + TMS_COM_TAB.TAB_NAME + "= ?) ");

		return sb.toString();
	}

	/**
	 * 取节点的全部信息,包括其前一级父类的个别信息
	 */
	public Map<String, Object> getNodeFullInfos(String tab_name) {

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT son.*, par.* FROM TMS_COM_TAB son left join");
		sb.append(" (SELECT CHANN PAR_CHANN, TAB_DESC PAR_TAB_DESC, TAB_NAME PAR_TAB_NAME FROM TMS_COM_TAB where TAB_TYPE='4' ) par");
		sb.append(" on son.PARENT_TAB= par.PAR_TAB_NAME where son.tab_name=? and son.TAB_TYPE='4' ");
		// sb.append(" on son.PARENT_TAB= par.PAR_TAB_NAME where son.tab_name=? and son.TAB_TYPE='4' and son.is_enable='1'");

		List<Map<String, Object>> rows = tmsSimpleDao.queryForList(sb.toString(), tab_name);

		if (rows != null && rows.size() != 1) {
			throw new TmsMgrWebException("查询当前交易出错");
		}

		Map<String, Object> fullMap = rows.get(0);
		addParentInfosIfNess(fullMap);
		return fullMap;
	}

	/*
	 * 补全父节点信息
	 */
	private void addParentInfosIfNess(Map<String, Object> fullMap) {
		// 如果父节点为空,将其父节点置为自己,供页面显示
		if (StringUtil.isEmpty(MapUtil.getString(fullMap, TMS_COM_TAB.PARENT_TAB))) {

			fullMap.put(TMS_COM_TAB.PARENT_TAB, MapUtil.getString(fullMap, TMS_COM_TAB.TAB_NAME));
			fullMap.put("PAR_TAB_DESC", MapUtil.getString(fullMap, TMS_COM_TAB.TAB_DESC));
		}
	}

	/**
	 * 递归取当前id所有父 交易的定义
	 */
	public List<Map<String, Object>> getFartherTranDef(String txnid) {

		// --子取父
		// select * from tms_com_tab t start with t.tab_name='T01010101'
		// connect by prior t.parent_tab=t.tab_name
		//
		// --父取子
		// select * from tms_com_tab t start with t.tab_name='T'
		// connect by prior t.tab_name=t.parent_tab

		StringBuffer sb = new StringBuffer();
		sb.append("select * from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in (" + TransCommon.arr2str(TransCommon.cutToIds(txnid)) + ")");

		return tmsSimpleDao.queryForList(sb.toString());
	}

	/**
	 * 递归取当前id所有父 交易的定义
	 */
	public List<Map<String, Object>> getFartherTranDefName(String txnid) {

		// --子取父
		// select * from tms_com_tab t start with t.tab_name='T01010101'
		// connect by prior t.parent_tab=t.tab_name
		//
		// --父取子
		// select * from tms_com_tab t start with t.tab_name='T'
		// connect by prior t.tab_name=t.parent_tab

		StringBuffer sb = new StringBuffer();
		sb.append("select " + TMS_COM_TAB.TAB_NAME + ", " + TMS_COM_TAB.TAB_DESC + " from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in (" + TransCommon.arr2str(TransCommon.cutToIds(txnid)) + ")");

		return tmsSimpleDao.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getSelfAndParentTranDef(String tab_name) {

		StringBuffer sb = new StringBuffer();
		sb.append("select *");
		sb.append(" from ").append(TMS_COM_TAB.TABLE_NAME);
		sb.append(" where ").append(TMS_COM_TAB.TAB_NAME);
		sb.append(" in (").append(TransCommon.cutToIdsForSql(tab_name)).append(")");
		sb.append(" order by ").append(TMS_COM_TAB.TAB_NAME);

		return tmsSimpleDao.queryForList(sb.toString());
	}

	public String getSelfAndParentTranDefAsStr(String tab_name) {

		StringBuffer signPost = new StringBuffer();
		List<Map<String, Object>> txnDef = getSelfAndParentTranDef(tab_name);

		for (Map<String, Object> map : txnDef) {
			signPost.append(MapUtil.getString(map, TMS_COM_TAB.TAB_DESC) + StaticParameters.SIGNPOST);
		}

		signPost.delete(signPost.lastIndexOf(StaticParameters.SIGNPOST), signPost.length());

		return signPost.toString();
	}

	/**
	 * 
	 */
	public List<Map<String, Object>> getSingleParentAndAllSub(String table, String txnColName, String txnid) {

		StringBuffer sb = new StringBuffer();
		sb.append("select * from ");
		sb.append(table);
		sb.append(" where ");
		sb.append(txnColName).append(" in (").append(TransCommon.cutToIdsForSql(txnid)).append(")");
		sb.append(" or ").append(txnColName).append(" like '").append(txnid).append("%'");

		return tmsSimpleDao.queryForList(sb.toString());
	}

	public List<Map<String, Object>> getAllTxn() {
		String sql = "select t.tab_name,t.tab_desc from tms_com_tab t where t.tab_type='4' order by t.tab_name";
		return tmsSimpleDao.queryForList(sql);
	}

	@Transactional
	public Map saveTranDef(Map formMap) {

		List<Map<String, Object>> delList = MapUtil.getList(formMap, "del");
		List<Map<String, Object>> modList = MapUtil.getList(formMap, "mod");
		List<Map<String, Object>> addList = MapUtil.getList(formMap, "add");

		Map m = new HashMap();

		if (delList != null && delList.size() > 0) {
			m = deleteTran(delList, "1");
		}
		if (addList != null && addList.size() > 0) {
			m = createTran(addList);
		}
		if (modList != null && modList.size() > 0) {
			m = updateTran(modList);
		}

		return m;
	}

}
