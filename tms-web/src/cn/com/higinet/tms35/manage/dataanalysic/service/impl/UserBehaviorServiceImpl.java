package cn.com.higinet.tms35.manage.dataanalysic.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.cache.ip_cache.loc;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.manage.common.PropertiesUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.dataanalysic.model.ReportModel;
import cn.com.higinet.tms35.manage.dataanalysic.service.UserBehaviorService;
import cn.com.higinet.tms35.manage.tran.TransCommon;
import cn.com.higinet.tms35.stat.stat_func;
import cn.com.higinet.tms35.stat.stat_number_encode;
import cn.com.higinet.tms35.stat.txn_stat;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_avg;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_bindist;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_bindist_rang;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count_equals;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_count_uniq;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_max;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_min;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_status;
import cn.com.higinet.tms35.stat.stat_func_im.stat_func_sum;

@Service("userBehaviorService")
public class UserBehaviorServiceImpl implements UserBehaviorService{
    private static Logger logger = Logger.getLogger(UserBehaviorServiceImpl.class);
	@Autowired
	private SimpleDao tmsSimpleDao;
	@Autowired
	private SimpleDao tmpSimpleDao;
	
	@Override
	public List<Map<String, Object>> queryTransStatFunc(String txnId) {
		StringBuffer stat_sql = new StringBuffer();
		stat_sql.append("SELECT STAT_ID ID,STAT_TXN,STAT_NAME, stat_txn fid,STAT_DESC CODE_VALUE,'2' ftype,STAT_TXN FROM TMS_COM_STAT where STAT_VALID =1");
		stat_sql.append(" and STAT_TXN in("+TransCommon.arr2str(TransCommon.cutToIds(txnId))+")");
		// 查询统计
		List<Map<String,Object>> stat_list = tmsSimpleDao.queryForList(stat_sql.toString());
		
		if(stat_list == null || stat_list.size() == 0) {
			return  null;
		}
		
		for (Map<String, Object> map : stat_list) {
			String stat_txn = MapUtil.getString(map, "STAT_TXN");
			String stat_name = MapUtil.getString(map, "STAT_NAME");
			
			map.put("CODE_KEY",stat_txn+":"+stat_name);
			
		}
		
		return stat_list;
	}
	
	@Override
	public List<Map<String, Object>> queryTransBranches() {
		
		String txn_sql = "SELECT TAB_NAME ID,M.TAB_NAME CODE_KEY,m.parent_tab fid,m.tab_desc CODE_VALUE ,'1' ftype, TAB_NAME STAT_TXN FROM TMS_COM_TAB M WHERE M.is_enable='1' and M.tab_type='4' order by STAT_TXN";
		// 查询交易树
		List<Map<String,Object>> txn_list = tmsSimpleDao.queryForList(txn_sql);
		return txn_list;
	}
	
	@Override
	public Map<String, List<Map<String, Object>>> queryWeiDus(
			String[] userIdArray, String[] weiDus, String txnId) {
		String tab_name = txnId;
		//查询txnId节点及其父节点下引用的表
		StringBuffer comFefTabSql = new StringBuffer();
		comFefTabSql.append("select a.ref_tab_name refTabName, a.ref_id refId from tms_com_reftab a where a.tab_name in(");
		comFefTabSql.append(TransCommon.arr2str(TransCommon.cutToIds(tab_name))+")");
		List<Map<String, Object>> queryComFefTab = tmsSimpleDao.queryForList(comFefTabSql.toString(), new HashMap<String, Object>());
		/*Map<String, Object> comFefTabMap = queryComFefTab.get(0);
		String  refTabName = (String) comFefTabMap.get("refTabName");*/
		StringBuffer refIdSb = new StringBuffer();
		for(Map<String,Object> comFefTab:queryComFefTab){
			BigDecimal refId = (BigDecimal) comFefTab.get("refId");
			if(refId!=null&&!"".equals(refId)){
				refIdSb.append("'"+refId+"',");
			}
		}
		String refIdSt = refIdSb.toString();
		refIdSt = refIdSt.substring(0,refIdSt.length()-1);
		//查询txnId节点及其父节点引用表中被引用的字段
		StringBuffer comFdSql = new StringBuffer();
		comFdSql.append("select a.REF_FD_NAME fdName,a.ref_name refName,a.STORECOLUMN storeColumn,a.REF_DESC refDesc,a.ref_id refId,b.ref_tab_name refTabName,a.tab_name tabName from tms_com_reffd a, tms_com_reftab b where a.ref_id=b.ref_id and a.ref_id in("+refIdSt+")");
		List<Map<String, Object>> refFdNameList = tmsSimpleDao.queryForList(comFdSql.toString(), new HashMap<String, Object>());
		
		Map<String,List<Map<String,Object>>> weiDuListMap = new HashMap<String,List<Map<String,Object>>>();
		List<Map<String,Object>> trafficStoreClolumnList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> refStoreClolumnList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> noStatStoreClolumnList = new ArrayList<Map<String,Object>>();
		
		String weiDuTemps = weiDus[0];
		String[] weiDuTempsArray = weiDuTemps.split(",");
		String userIdTemps = userIdArray[0];
		String[] userIdTempsArray = userIdTemps.split(",");
		//遍历指定用户的行为习惯
		for(int i = 0;i<weiDuTempsArray.length;i++){
			String weiDuTemp = weiDuTempsArray[i];
			String weiDuName = "";
			String weiDuType = "";
			if(weiDuTemp.contains("-")){
				String[] weiDuNameAndType = weiDuTemp.split("-");
				weiDuName = weiDuNameAndType[0];
				weiDuType = weiDuNameAndType[1];
			}
			boolean isRef = false;
			//先判断是否统计，是则查统计中的存储字段并跳到下一次循环
			if(weiDuTemp.contains(":")){
				String[] weiDu = weiDuTemp.split(":");
				String weiDuTxnTab = weiDu[0];
				String weiDuTxnStat = weiDu[1];
				getStatStoreClumn(trafficStoreClolumnList,
						noStatStoreClolumnList, weiDuTxnTab, weiDuTxnStat);
				
				continue;
			}
			//如果引用表不为空，则遍历所有引用的字段，如果存在则跳到下一次循环
			for(Map<String, Object> fdNameMap:refFdNameList){
				String fdName = (String) fdNameMap.get("fdName");
				String storeColumn = (String) fdNameMap.get("storeColumn");
				String refDesc = (String) fdNameMap.get("refDesc");
				String refTabName = (String) fdNameMap.get("refTabName");
				String tabName = (String) fdNameMap.get("tabName");
				String refName = (String) fdNameMap.get("refName");
				Map<String,Object> dataMap = new HashMap<String,Object>();
					
				if(weiDuType.equals(tabName)&&weiDuName.equals(refName)){
					dataMap.put("storeColumn",storeColumn);
					dataMap.put("desc",refDesc);
					dataMap.put("fdName",fdName);
					dataMap.put("refTabName",refTabName);
					if("".equals(storeColumn)||storeColumn==null){
						refStoreClolumnList.add(dataMap);
							
					}else{
						trafficStoreClolumnList.add(dataMap);
					}
					isRef = true;
					break;
				}
			}
			
			//如果没有引用的字段，则遍历txnId及其以上的所有父节点的存储字段
			if(!isRef){
				StringBuffer sql = new StringBuffer();
				sql.append("select  a.fd_name fdName,a.ref_name refName,a.NAME fdDesc,a.tab_name tabName from TMS_COM_fd a ");
				sql.append("where a.tab_name in ("+TransCommon.arr2str(TransCommon.cutToIds(txnId))+")");
				List<Map<String, Object>> fdNameList = tmsSimpleDao.queryForList(sql.toString(), new HashMap<String, Object>());
				for(Map<String, Object> fdNameMap :fdNameList){
					String fdName = (String) fdNameMap.get("fdName");
					String refName = (String) fdNameMap.get("refName");
					String desc = (String) fdNameMap.get("fdDesc");
					String tabName = (String) fdNameMap.get("tabName");
					Map<String,Object> dataMap = new HashMap<String,Object>();
					if(weiDuType.equals(tabName)&&weiDuName.equals(refName)){
						dataMap.put("storeColumn",fdName);
						dataMap.put("desc",desc);
						trafficStoreClolumnList.add(dataMap);
						break;
					}
				}
			}
			
		}
		
		//查询指定用户的行为习惯
		for(int i = 0;i<userIdTempsArray.length;i++){
			//查询每个存储字段在tms_run_trafficdata的值
			List<Map<String,Object>> weiDuMapList = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> storeCloumnMap :trafficStoreClolumnList){
				String storeCloumn = (String) storeCloumnMap.get("storeColumn");
				String desc = (String) storeCloumnMap.get("desc");
				Object weiDuValue = null;
				if(!"".equals(storeCloumn) && storeCloumn!=null){
					
					StringBuffer queryUserIdWeiDu = new StringBuffer();
					queryUserIdWeiDu.append("select a."+storeCloumn);
					queryUserIdWeiDu.append(" storeCloumn from tms_run_trafficdata a");
					queryUserIdWeiDu.append(" where a.userid="+"'"+userIdTempsArray[i].trim()+"'");
					
					queryUserIdWeiDu.append(" and a.createtime=(select max(b.createtime) from tms_run_trafficdata b where b.userid=");
					queryUserIdWeiDu.append("'"+userIdTempsArray[i].trim()+"'");
					queryUserIdWeiDu.append(" and b.txntype="+"'"+txnId+"')");
					List<Map<String, Object>> queryFdName = tmsSimpleDao.queryForList(queryUserIdWeiDu.toString(), new HashMap<String, Object>());
					if(queryFdName!=null&&queryFdName.size()>0){
						Map<String, Object> weiDuMap = queryFdName.get(0);
						
						try{
							weiDuValue = (String) weiDuMap.get("storeCloumn");
						}catch(Exception e){
							weiDuValue = (BigDecimal) weiDuMap.get("storeCloumn");
						}
						
						Map<String,Object> panDuanMap = panDuan(weiDuValue);
						boolean isHasCn = (Boolean) panDuanMap.get("isHasCn");
						boolean isHasLt = (Boolean) panDuanMap.get("isHasLt");
						
						Map<String,Object> weDuMap = new HashMap<String,Object>();
						if(weiDuValue==null||isHasCn||isHasLt){
							weiDuValue=0;
						}
						weDuMap.put("desc",desc);
						weDuMap.put(storeCloumn,weiDuValue);
						weiDuMapList.add(weDuMap);
					}else{
						Map<String,Object> weDuMap = new HashMap<String,Object>();
						weDuMap.put("desc",desc);
						weDuMap.put("statValue",0);
						weiDuMapList.add(weDuMap);
					}
					
					
				}else{
					Map<String,Object> weDuMap = new HashMap<String,Object>();
					weDuMap.put("desc",desc);
					weDuMap.put("statValue",0);
					weiDuMapList.add(weDuMap);
				}
				
				
			}
			//查询引用表中的存储字段的值
			for(Map<String,Object> storeCloumnMap :refStoreClolumnList){
				String refTabName = (String) storeCloumnMap.get("refTabName");
				String storeCloumn = (String) storeCloumnMap.get("storeColumn");
				String desc = (String) storeCloumnMap.get("desc");
				String fdName = (String) storeCloumnMap.get("fdName");
				Object weiDuValue = null;
				if("TMS_RUN_USER".equals(refTabName)){
					if(!"".equals(storeCloumn) && storeCloumn!=null){
						StringBuffer queryUserIdWeiDu = new StringBuffer();
						queryUserIdWeiDu.append("select a."+storeCloumn);
						queryUserIdWeiDu.append(" storeCloumn from tms_run_trafficdata a");
						queryUserIdWeiDu.append(" where a.userid="+"'"+userIdTempsArray[i].trim()+"'");
						
						queryUserIdWeiDu.append(" and a.createtime=(select max(b.createtime) from tms_run_trafficdata b where b.userid=");
						queryUserIdWeiDu.append("'"+userIdTempsArray[i].trim()+"'");
						queryUserIdWeiDu.append(" and b.txntype="+"'"+txnId+"')");
						List<Map<String, Object>> queryFdName = tmsSimpleDao.queryForList(queryUserIdWeiDu.toString(), new HashMap<String, Object>());
						if(queryFdName!=null&&queryFdName.size()>0){
							Map<String, Object> weiDuMap = queryFdName.get(0);
							
							try{
								weiDuValue = (String) weiDuMap.get("storeCloumn");
							}catch(Exception e){
								weiDuValue = (BigDecimal) weiDuMap.get("storeCloumn");
							}
							
							Map<String,Object> panDuanMap = panDuan(weiDuValue);
							boolean isHasCn = (Boolean) panDuanMap.get("isHasCn");
							boolean isHasLt = (Boolean) panDuanMap.get("isHasLt");
							
							Map<String,Object> weDuMap = new HashMap<String,Object>();
							if(weiDuValue==null||isHasCn||isHasLt){
								weiDuValue=0;
							}
							weDuMap.put("desc",desc);
							weDuMap.put("statValue",weiDuValue);
							weiDuMapList.add(weDuMap);
						}
						
					}else{
						StringBuffer queryUserIdWeiDu = new StringBuffer();
						queryUserIdWeiDu.append("select a."+fdName);
						queryUserIdWeiDu.append(" storeCloumn from "+refTabName +" a");
						queryUserIdWeiDu.append(" where userid="+"'"+userIdTempsArray[i].trim()+"'");
						List<Map<String, Object>> queryFdName = tmsSimpleDao.queryForList(queryUserIdWeiDu.toString(), new HashMap<String, Object>());
						if(queryFdName!=null&&queryFdName.size()>0){
							Map<String, Object> weiDuMap = queryFdName.get(0);
							
							try{
								weiDuValue = (String) weiDuMap.get("storeCloumn");
							}catch(Exception e){
								weiDuValue = (BigDecimal) weiDuMap.get("storeCloumn");
							}
							
							Map<String,Object> panDuanMap = panDuan(weiDuValue);
							boolean isHasCn = (Boolean) panDuanMap.get("isHasCn");
							boolean isHasLt = (Boolean) panDuanMap.get("isHasLt");
							Map<String,Object> weDuMap = new HashMap<String,Object>();
							if(weiDuValue==null||isHasCn||isHasLt){
								weiDuValue=0;
							}
							weDuMap.put("desc",desc);
							weDuMap.put("statValue",weiDuValue);
							weiDuMapList.add(weDuMap);
						}else{
							Map<String,Object> weDuMap = new HashMap<String,Object>();
							weDuMap.put("desc",desc);
							weDuMap.put("statValue",0);
							weiDuMapList.add(weDuMap);
						}
					}
				}else{
					Map<String,Object> weDuMap = new HashMap<String,Object>();
					weDuMap.put("desc",desc);
					weDuMap.put("statValue",0);
					weiDuMapList.add(weDuMap);
				}
			}
		
			
		
		
			if(noStatStoreClolumnList!=null && noStatStoreClolumnList.size()>0){
				for(Map<String,Object> statMap :noStatStoreClolumnList){
					String desc = (String) statMap.get("desc");
//					String stat_name = (String) statMap.get("stat_name");
//					String stat_txn = (String) statMap.get("stat_txn");
					String statFn = (String) statMap.get("statFn");
					BigDecimal statId = (BigDecimal) statMap.get("statId");
					db_stat dbstat = (db_stat) statMap.get("db_stat");
					long cm = System.currentTimeMillis();
					long curMin = cm / 60000;
					
					char split = txn_stat.ch_split;
//					int statIdTemp = Integer.parseInt(String.valueOf(statId));
					String sql = "select a.STAT_VALUE statValue from tms_run_stat a where a.STAT_PARAM="+"'"+userIdTempsArray[i]+split+"'";
					List<Map<String, Object>> queryStatValue = tmsSimpleDao.queryForList(sql, new HashMap<String, Object>());
					if(queryStatValue!=null&&queryStatValue.size()>0){
						Map<String, Object> StatValue = queryStatValue.get(0);
						String statValue = (String) StatValue.get("statValue");
						String[] statValueArray = statValue.split("\n");
						this.getStatValFromRunStat(userIdTempsArray[i],desc, statFn, statId, dbstat,
								curMin, statValueArray, weiDuMapList);
						
					}
					
				}
				
			}
			weiDuListMap.put(userIdTempsArray[i],weiDuMapList);
	}
		return weiDuListMap;
	}
	
	/**
	 * 根据不同的统计函数从tms_run_stat中获取统计值
	 * @param desc
	 * @param statFn
	 * @param statId
	 * @param dbstat
	 * @param curMin
	 * @param statValueArray
	 * @param weiDuMapList
	 */
	public void getStatValFromRunStat(String userId,String desc, String statFn,
			BigDecimal statId, db_stat dbstat, long curMin,
			String[] statValueArray, List<Map<String, Object>> weiDuMapList) {
			for(int j = 0; j<statValueArray.length;j++){
				String statValueTemp = statValueArray[j];
				String[] statValArray = statValueTemp.split(":");
				String stIdTmp = statValArray[0];
				String d = PropertiesUtil.getPropInstance().get("tms.stat.web.base64");
				stat_number_encode.setM_codec(Integer.parseInt(d));
				stIdTmp = String.valueOf(stat_number_encode.decode_long(stIdTmp));
				boolean isHasStatId = false;
				if(j == 0){
					for(int t = 0; t<statValueArray.length;t++){
						String st = statValueArray[t];
						String[] stTmp = st.split(":");
						String statIdTmp = String.valueOf(stat_number_encode.decode_long(stTmp[0]));
						if(statIdTmp.equals(String.valueOf(statId))){
							isHasStatId = true;
						}
					}
					if(!isHasStatId){
						Map<String,Object> weDuMap = new HashMap<String,Object>();
						weDuMap.put("desc",desc);
						weDuMap.put("statValue",0);
						weiDuMapList.add(weDuMap);
						break;
					}
				}
				
			if(!stIdTmp.equals(String.valueOf(statId))){
				continue;
			}
				
			statValueTemp = statValueTemp.substring(statValueTemp.indexOf(":")+1);
			//计数
			if("count".equals(statFn)){
				stat_func_count count = new stat_func_count();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, count);
				
				break;
			}
			//平均值
			if("avg".equals(statFn)){
				stat_func_avg avg = new stat_func_avg();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, avg);
				break;
			}
			//区间分布
			if("rang_bin_dist".equals(statFn)){
				stat_func_bindist_rang bindistRang = new stat_func_bindist_rang();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, bindistRang);
				break;
			}
			//合计值
			if("sum".equals(statFn)){
				stat_func_sum sum = new stat_func_sum();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, sum);
				break;
			}
			//计算表达式
			if("calculat_expressions".equals(statFn)){
				Map<String,Object> weDuMap = new HashMap<String,Object>();
				weDuMap.put("desc",desc);
				weDuMap.put("statValue",0);
				weiDuMapList.add(weDuMap);
			}
			//唯一计数
			if("count_uniq".equals(statFn)){
				stat_func_count_uniq count_uniq = new stat_func_count_uniq();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, count_uniq);
				break;
			}
			//快照
			if("snapshot".equals(statFn)){
				String[] snapShotTemp = statValueTemp.split(":");
				String snapShotVal = snapShotTemp[1];
				Object statVal = snapShotVal.replace("|","");
				
				Map<String,Object> weDuMap = new HashMap<String,Object>();
				weDuMap.put("desc",desc);
				weDuMap.put("statValue",statVal);
				weiDuMapList.add(weDuMap);
				break;
			}
			//状态
			if("status".equals(statFn)){
				stat_func_status status = new stat_func_status();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, status);
				break;
			}
			//最大值
			if("max".equals(statFn)){
				stat_func_max max = new stat_func_max();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, max);
				break;
			}
			//二项分布
			if("bin_dist".equals(statFn)){
				stat_func_bindist bindist = new stat_func_bindist();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, bindist);
				
				break;
			}
			//相同计数
			if("count_equals".equals(statFn)){
				stat_func_count_equals countEquals = new stat_func_count_equals();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, countEquals);
				break;
			}
			// 最小值
			if("min".equals(statFn)){
				stat_func_min min = new stat_func_min();
				this.getStatValue(userId,weiDuMapList, desc, dbstat,
						curMin, statValueTemp, min);
				break;
			}
			
		}
	}
	
	/**
	 * 获取统计字段
	 * @param trafficStoreClolumnList
	 * @param noStatStoreClolumnList
	 * @param weiDuTxnTab
	 * @param weiDuTxnStat
	 */
	public void getStatStoreClumn(
			List<Map<String, Object>> trafficStoreClolumnList,
			List<Map<String, Object>> noStatStoreClolumnList,
			String weiDuTxnTab, String weiDuTxnStat) {
		StringBuffer sql = new StringBuffer();
		sql.append("select a.storecolumn storeColumn,a.stat_desc statDesc,a.STAT_ID statId,a.STAT_FN statFn," +
				"a.STAT_NAME statName,a.STAT_TXN statTxn,a.STAT_PARAM statParam,a.STAT_COND statCond,a.RESULT_COND resultCond,a.STAT_DATAFD statDatafd, " +
				"a.COUNUNIT counUnit,a.COUNTROUND countRound,a.STAT_UNRESULT statUnResult,a.CONTINUES continues,a.STAT_VALID statValid,a.FN_PARAM fnParam," +
				"a.DATATYPE dataType from tms_com_stat a ");
		sql.append("where a.stat_name="+"'"+weiDuTxnStat+"'");
		sql.append(" and a.stat_txn="+"'"+weiDuTxnTab+"'");
		List<Map<String, Object>> queryFdName = tmsSimpleDao.queryForList(sql.toString(), new HashMap<String, Object>());
		if(queryFdName!=null&&queryFdName.size()>0){
			Map<String, Object> FdNameMap = queryFdName.get(0);
			
			String storeColumn = (String) FdNameMap.get("storeColumn");
			String statDesc = (String) FdNameMap.get("statDesc");
			String statFn = (String)FdNameMap.get("statFn");
			BigDecimal counUnit = (BigDecimal)FdNameMap.get("counUnit");
			BigDecimal countRound = (BigDecimal)FdNameMap.get("countRound");
			BigDecimal statId = (BigDecimal)FdNameMap.get("statId");
			
			db_stat st = new db_stat();
			if(counUnit!=null){
				st.stat_unit_min = Integer.parseInt(counUnit.toString());
			}
			if(countRound!=null){
				st.stat_num_unit = Integer.parseInt(countRound.toString());
			}
			
			if (st.stat_num_unit == 0)
				st.stat_num_unit = 1;
			if(!"".equals(storeColumn)&&storeColumn!=null){
				Map<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("storeColumn",storeColumn);
				dataMap.put("desc",statDesc);
				trafficStoreClolumnList.add(dataMap);
			}
			
			if(storeColumn==null||"".equals(storeColumn)){
				Map<String,Object> dataMap = new HashMap<String,Object>();
				dataMap.put("desc",statDesc);
				dataMap.put("stat_name",weiDuTxnStat);
				dataMap.put("stat_txn",weiDuTxnTab);
				dataMap.put("statId",statId);
				dataMap.put("statFn",statFn);
				dataMap.put("db_stat",st);
				logger.info("=========统计Id:"+statId+" 统计名称:"+statDesc+"统计函数:"+statFn+" 统计单位:"+st.stat_unit_min+" 统计周期"+st.stat_num_unit);
				noStatStoreClolumnList.add(dataMap);
			}
		}
		
	}

	/**
	 * 根据不同的统计函数获取统计值
	 * @param weiDuMapList
	 * @param desc
	 * @param dbstat
	 * @param curMin
	 * @param statValueTemp
	 * @param count
	 */
	public void getStatValue(String userId,List<Map<String, Object>> weiDuMapList,
			String desc, db_stat dbstat, long curMin, String statValueTemp,
			stat_func count) {
		logger.info("=========用户号:"+userId+"统计名称:"+desc+"统计数据:"+statValueTemp+"统计单位:"+dbstat.stat_unit_min+" 统计周期"+dbstat.stat_num_unit+" 当前时间:"+curMin);
		Object statVal = count.get(statValueTemp, dbstat, (int) curMin, null);
//		System.out.println(statVal);
		/*String reg = ".*[a-zA-Z]+.*";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(statVal);
		boolean isHasLetter = matcher.matches();
		if(isHasLetter){
			int hashId = (hash.clac());
		}*/
		
		logger.info("=========用户号:"+userId+"统计名称:"+desc+"统计结果:"+statVal);
		if(statVal==null){
			statVal = 0;
		}
		Map<String,Object> weDuMap = new HashMap<String,Object>();
		weDuMap.put("desc",desc);
		weDuMap.put("statValue",statVal);
		weiDuMapList.add(weDuMap);
	}
	
	
	
	
	@Override
	public List<Map<String, Object>> queryRechargeRegion(Map<String, String> reqs,ReportModel reportModel) {
		loc.entry fromLocEntry = null;
		loc.entry toLocEntry = null;
		String account = reqs.get("ACCOUNTNO").toString().toString();
		String level = reqs.get("LEVEL").toString().toString();
		String startTime = reqs.get("startTime").toString();
		String endTime = reqs.get("endTime").toString();
		Long txnstarttime = date_tool.parse(startTime).getTime();
		Long txnendtime = date_tool.parse(endTime).getTime();
		StringBuffer sql = new StringBuffer();
		
		sql.append("with zztb as (select distinct t.txnid,t.txntype,t.txncode,t.fromuserid,t.fromip,t.touserid,t.toip  from report_syndata t  where t.txntype ='1' and t.fromip is not null and t.toip is not null  and t.txntime between ");
		sql.append(txnstarttime.toString()).append(" and ").append(txnendtime.toString()).append(" ) ");
		
		sql.append(",sktb as (select distinct t.txnid,t.txntype,t.txncode,t.fromuserid,t.fromip,t.touserid,t.toip  from report_syndata t  where t.txntype ='2' and t.fromip is not null and t.toip is not null  and t.txntime between ");
		sql.append(txnstarttime.toString()).append(" and ").append(txnendtime.toString()).append(" ) ");
		
		if (!str_tool.is_empty(account)){
			sql.append(" select distinct * from zztb start with (zztb.fromuserid!=zztb.touserid and zztb.fromuserid = '").append(account).append("') connect by  NOCYCLE  prior zztb.touserid =  zztb.fromuserid and level<=").append(level);
			sql.append(" union all");
			sql.append(" select distinct * from zztb start with (zztb.fromuserid!=zztb.touserid and zztb.touserid = '").append(account).append("') connect by  NOCYCLE  prior zztb.fromuserid =  zztb.touserid and level<=").append(level);
			sql.append(" union all");
			sql.append(" select distinct * from sktb start with (sktb.fromuserid!=sktb.touserid and sktb.fromuserid = '").append(account).append("') connect by  NOCYCLE  prior sktb.touserid =  sktb.fromuserid and level<=").append(level);
			sql.append(" union all");
			sql.append(" select distinct * from sktb start with (sktb.fromuserid!=sktb.touserid and sktb.touserid = '").append(account).append("') connect by  NOCYCLE  prior sktb.fromuserid =  sktb.touserid and level<=").append(level);
		}
		logger.info(sql.toString());
		//转账交易数据
		List<Map<String,Object>> geoCoordMapList = new ArrayList<Map<String,Object>>();
		//源点列表
		List<Map<String,Object>> sourceMapList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> data_list = tmpSimpleDao.queryForList(sql.toString());
		
		List<Map<String,Object>> remove_data_list = new ArrayList<Map<String,Object>>();
		Set<String> citySet  = new HashSet<String>();
		int i=0;
		for (Map<String,Object> dataMap:data_list){
			String fromCity="",toCity="";
			try {
				fromLocEntry = ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_IP, str_tool.to_str(dataMap.get("fromip")));
				toLocEntry = ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_IP, str_tool.to_str(dataMap.get("toip")));
				if (null==fromLocEntry||null==toLocEntry){
					remove_data_list.add(dataMap);
					continue;
				}
				fromCity = fromLocEntry.cityname;
				toCity = toLocEntry.cityname;
				if(!citySet.contains(fromCity)){
					citySet.add(fromCity);
					HashMap<String,Object> geoCoordMap = new HashMap<String,Object>();
					geoCoordMap.put("city", fromCity);
					geoCoordMap.put("latitude", fromLocEntry.latitude);  //维度
					geoCoordMap.put("longitude", fromLocEntry.longitude);//经度
					geoCoordMapList.add(geoCoordMap);
				}
				if(!citySet.contains(toCity)){
					citySet.add(toCity);
					HashMap<String,Object> geoCoordMap = new HashMap<String,Object>();
					geoCoordMap.put("city", toCity);
					geoCoordMap.put("latitude", toLocEntry.latitude);  //维度
					geoCoordMap.put("longitude", toLocEntry.longitude);//经度
					geoCoordMapList.add(geoCoordMap);
				}
				if(!account.equals(dataMap.get("fromuserid").toString())&&i==0){
					i++;
					HashMap<String,Object> sourceMap = new HashMap<String,Object>();
					sourceMap.put("city", toCity);
					sourceMap.put("latitude", toLocEntry.latitude);  //维度
					sourceMap.put("longitude", toLocEntry.longitude);//经度
					sourceMapList.add(sourceMap);
				}
				
			} catch (Exception e) {
				logger.error("经纬度转换异常",e);
				fromCity="";
				toCity="";
			}
			if(str_tool.is_empty(fromCity)||str_tool.is_empty(toCity)){
				remove_data_list.add(dataMap);
				continue;
			}
			dataMap.put("fromCity", fromCity);
			dataMap.put("toCity", toCity);
		}
		data_list.removeAll(remove_data_list);
		reportModel.setList(data_list);
		reportModel.setGeoCoordMaplist(geoCoordMapList);
		reportModel.setSourceMaplist(sourceMapList);
		return data_list;
	}
	
	
	
	
	@Override
	public List<Map<String, Object>> queryPhoneRechargeRegion(Map<String, String> reqs,ReportModel reportModel) {
		loc.entry fromLocEntry = null;
		loc.entry toLocEntry = null;
		String account = reqs.get("ACCOUNTNO").toString().trim();
		String startTime = reqs.get("startTime").toString();
		String endTime = reqs.get("endTime").toString();
		Long txnstarttime = date_tool.parse(startTime).getTime();
		Long txnendtime = date_tool.parse(endTime).getTime();
		StringBuffer sql = new StringBuffer();
		sql.append("select distinct t.txnid,t.txncode,t.fromuserid,t.fromip,t.touserid,t.toip,t.fromphonenumber,t.tophonenumber from report_syndata t ");
		sql.append(" where t.txntype = '3' and t.fromuserid = '").append(account).append("'");
		sql.append(" and t.txntime between ").append(txnstarttime.toString()).append(" and ").append(txnendtime.toString());
		logger.info(sql.toString());
		//转账交易数据
		List<Map<String,Object>> geoCoordMapList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> data_list = tmpSimpleDao.queryForList(sql.toString());
		List<Map<String,Object>> remove_data_list = new ArrayList<Map<String,Object>>();
		Set<String> citySet  = new HashSet<String>();
		for (Map<String,Object> dataMap:data_list){
			String fromCity="",toCity="";
			try {
				fromLocEntry = ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_MOBILE, str_tool.to_str(dataMap.get("fromphonenumber")));
				toLocEntry = ip_cache.Instance().find_loc_entry(ip_cache.LOCATE_TYPE_MOBILE, str_tool.to_str(dataMap.get("tophonenumber")));
				if (null==fromLocEntry||null==toLocEntry){
					remove_data_list.add(dataMap);
					continue;
				}
				fromCity = fromLocEntry.cityname;
				toCity = toLocEntry.cityname;
				if(!citySet.contains(fromCity)){
					citySet.add(fromCity);
					HashMap<String,Object> geoCoordMap = new HashMap<String,Object>();
					geoCoordMap.put("city", fromCity);
					geoCoordMap.put("latitude", fromLocEntry.latitude);  //维度
					geoCoordMap.put("longitude", fromLocEntry.longitude);//经度
					geoCoordMapList.add(geoCoordMap);
				}
				if(!citySet.contains(toCity)){
					citySet.add(toCity);
					HashMap<String,Object> geoCoordMap = new HashMap<String,Object>();
					geoCoordMap.put("city", toCity);
					geoCoordMap.put("latitude", toLocEntry.latitude);  //维度
					geoCoordMap.put("longitude", toLocEntry.longitude);//经度
					geoCoordMapList.add(geoCoordMap);
				}
				
				
			} catch (Exception e) {
				logger.error("经纬度转换异常",e);
				fromCity="";
				toCity="";
			}
			if(str_tool.is_empty(fromCity)||str_tool.is_empty(toCity)){
				remove_data_list.add(dataMap);
				continue;
			}
			dataMap.put("fromCity", fromCity);
			dataMap.put("toCity", toCity);
		}
		data_list.removeAll(remove_data_list);
		reportModel.setList(data_list);
		reportModel.setGeoCoordMaplist(geoCoordMapList);
		return data_list;
	}
	
	/**
	 * 判断维度值是否含有汉字/母,有则维度值设为0
	 * @param weiDuValue
	 * @return
	 */
	private Map<String,Object> panDuan(Object weiDuValue){
		Map<String,Object> panDuanMap = new HashMap<String,Object>();
		boolean isHasCn = false;//是否含有汉字
		boolean isHasLt = false;//是否含有字母
		if(weiDuValue instanceof String){
			int a = ((String) weiDuValue).getBytes().length;
			int b = ((String)weiDuValue).length();
			if(a!=b){
				isHasCn=true;
			}
			String regex = ".*[a-zA-Z]+.*";
			Matcher m = Pattern.compile(regex).matcher((String)weiDuValue);
			if(m.matches()){
				isHasLt = true;
			};
		}
		panDuanMap.put("isHasCn",isHasCn);
		panDuanMap.put("isHasLt",isHasLt);
		return panDuanMap;
	}
	
	public List<Map<String,Object>> queryTranpropertiesWeiDus(Map<String, String> reqs){
		String txn_id = MapUtil.getString(reqs, "txn_id");
		
		String txn_rule_sql = "SELECT CODE_KEY||'-'||TAB_NAME CODE_KEY, CODE_VALUE, type, code fd_code FROM (SELECT REF_NAME CODE_KEY, NAME CODE_VALUE, type, code, TAB_NAME FROM TMS_COM_FD UNION SELECT crf.REF_NAME CODE_KEY, crf.REF_DESC CODE_VALUE, cf.type TYPE, cf.code code, crf.TAB_NAME from TMS_COM_REFFD crf left join tms_com_reftab crt on crf.ref_id = crt.ref_id left join tms_com_fd cf on crt.ref_tab_name = cf.tab_name and crf.ref_name = cf.fd_name) F where TAB_NAME in ("+TransCommon.arr2str(TransCommon.cutToIds(txn_id))+") ORDER BY TAB_NAME"; 
		
		List<Map<String,Object>> txn_rule_list = tmsSimpleDao.queryForList(txn_rule_sql);
		return txn_rule_list;
		
	}
	
	
	public static void main(String[] v) {
		stat_func_count count = new stat_func_count();
		db_stat stat = new db_stat();
		stat.stat_unit_min = 2;// 统计单位 2 小时 3天
		stat.stat_num_unit = 25;// 统计周期
		String data = "2101620:3|";

		int cur_minute = (int) (System.currentTimeMillis() / 60000);
		System.out.println("--cur_minute:" + cur_minute);

//		for (int i = 0; i < 25; i++) {
			System.out.println( count.get(data, stat, 24718041, null));
//		}

		long baseTime = date_tool.parse("2013-1-1 0:0:0").getTime() / 60000;
		long minWin = System.currentTimeMillis() / 60000 - baseTime;
		System.out.println("--minWin:" + minWin);
		long hours = (minWin - 1834560) / 60;
		System.out.println("--hours:" + hours);

	}
}
