package cn.com.higinet.tms35.manage.dataanalysic.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.core.cache.db_stat;
import cn.com.higinet.tms35.manage.common.PropertiesUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.common.util.StringUtil;
import cn.com.higinet.tms35.manage.dataanalysic.service.UserStatValueService;
import cn.com.higinet.tms35.manage.tran.TransCommon;
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

@Service("userStatValueService")
public class UserStatValueServiceImpl implements UserStatValueService {

	private static Log log = LogFactory.getLog(UserStatValueService.class);
	@Autowired
	private SimpleDao tmsSimpleDao;

	// 查看统计
	@Override
	public Page<Map<String, Object>> statPage(Map<String, String> reqs) {

		String isFirstEnter = MapUtil.getString(reqs, "isFirstEnter");// 是否查询
		if (isFirstEnter.equals("TRUE")) {
			return null;
		}
		String isQuery = MapUtil.getString(reqs, "isQuery");// 是否查询
		String userId = MapUtil.getString(reqs, "USERID");// 用户编号
		String userName = MapUtil.getString(reqs, "USERNAME");// 用户名称
		String startTime = MapUtil.getString(reqs, "startTime");// 是更新起始时间
		String endTime = MapUtil.getString(reqs, "endTime");// 是更新结束时间
		String deviceId = MapUtil.getString(reqs, "deviceid");// 终端id
		String stat_id = MapUtil.getString(reqs, "statId");// 交易树
		String transBranches = MapUtil.getString(reqs, "transBranches");

		if (isQuery.equals("false")) {
			return null;
		}
		// 如果终端id不为空和交易树的统计不为空进行查询统计
		if (!StringUtil.isEmpty(deviceId)) {
			StringBuffer sb = new StringBuffer();
			sb.append("select a.storecolumn STORECOLUMN from tms_com_stat a where a.STAT_VALID =1 ");
			sb.append(" and STAT_ID=" + "'" + stat_id + "'");
			List<Map<String, Object>> statList = tmsSimpleDao.queryForList(sb
					.toString());
			Map<String, Object> storecolumn1 = statList.get(0);
			String storecolumn = (String) storecolumn1.get("STORECOLUMN");
			String statDesc = (String) storecolumn1.get("stat_desc");
			String stat_Fn = (String) storecolumn1.get("stat_Fn");
			BigDecimal counUnit = (BigDecimal) storecolumn1.get("counUnit");
			BigDecimal countRound = (BigDecimal) storecolumn1.get("countRound");
			BigDecimal statId = (BigDecimal) storecolumn1.get("stat_id");
			db_stat st = new db_stat();
			if (counUnit != null) {
				st.stat_unit_min = Integer.parseInt(counUnit.toString());
			}
			if (countRound != null) {
				st.stat_num_unit = Integer.parseInt(countRound.toString());
			}

			if (st.stat_num_unit == 0)
				st.stat_num_unit = 1;
			// 查询没有存储字段的统计值
			// if(storecolumn == null || "".equals(storecolumn)){ //20170609
			// herou 发现直接有存储字段的统计值，直接取字段是不对的。如果有挂起的时候，由于评估
			if (true) {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("desc", statDesc);
				dataMap.put("statId", statId);
				dataMap.put("statFn", stat_Fn);
				dataMap.put("db_stat", st);
				String desc = (String) dataMap.get("desc");
				String statFn = (String) dataMap.get("statFn");
				BigDecimal stat_Id = (BigDecimal) dataMap.get("statId");
				db_stat dbstat = (db_stat) dataMap.get("db_stat");
				long cm = System.currentTimeMillis();
				long curMin = cm / 60000;
				char split = txn_stat.ch_split;
				String sql = "select a.STAT_VALUE statValue from tms_run_stat a where a.STAT_PARAM="
						+ "'" + deviceId + split + "'";
				List<Map<String, Object>> queryStatValue = tmsSimpleDao
						.queryForList(sql, new HashMap<String, Object>());
				if (queryStatValue != null && queryStatValue.size() > 0) {
					Map<String, Object> StatValue = queryStatValue.get(0);
					String statValue = (String) StatValue.get("statValue");
					String[] statValueArray = statValue.split("\n");
					for (int j = 0; j < statValueArray.length; j++) {
						String finalStValTemp = statValueArray[j];
						String[] statValArray = finalStValTemp.split(":");
						String stIdTmp = statValArray[0];
						String d = PropertiesUtil.getPropInstance().get(
								"tms.stat.web.base64");
						stat_number_encode.setM_codec(Integer.parseInt(d));
						stIdTmp = String.valueOf(stat_number_encode
								.decode_long(stIdTmp));
						boolean isHasStatId = false;
						if (j == 0) {
							for (int t = 0; t < statValueArray.length; t++) {
								String st1 = statValueArray[t];
								String[] stTmp = st1.split(":");
								String statIdTmp = String
										.valueOf(stat_number_encode
												.decode_long(stTmp[0]));
								if (statIdTmp.equals(String.valueOf(statId))) {
									isHasStatId = true;
								}
							}
						}
						if (!stIdTmp.equals(String.valueOf(statId))) {
							continue;
						}

						finalStValTemp = finalStValTemp
								.substring(finalStValTemp.indexOf(":") + 1);

						// 计数
						if ("count".equals(statFn)) {
							stat_func_count count = new stat_func_count();
							Object statVal = count.get(finalStValTemp, dbstat,
									(int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());

							return statPage;
						}
						// 平均数
						if ("avg".equals(statFn)) {
							stat_func_avg avg = new stat_func_avg();
							Object statVal = avg.get(finalStValTemp, dbstat,
									(int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 区间分布
						if ("rang_bin_dist".equals(statFn)) {
							stat_func_bindist_rang bindistRang = new stat_func_bindist_rang();
							Object statVal = bindistRang.get(finalStValTemp,
									dbstat, (int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());

							return statPage;
						}
						// 合计值
						if ("sum".equals(statFn)) {
							stat_func_sum sum = new stat_func_sum();
							// 不需要去再查一次
							Object statVal = sum.get(finalStValTemp, dbstat,
									(int) curMin, null);

							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 唯一计数
						if ("count_uniq".equals(statFn)) {
							stat_func_count_uniq count_uniq = new stat_func_count_uniq();
							Object statVal = count_uniq.get(finalStValTemp,
									dbstat, (int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 快照
						if ("snapshot".equals(statFn)) {
							String[] snapShotTemp = finalStValTemp.split(":");
							String snapShotVal = snapShotTemp[1];
							Object statVal = snapShotVal.replace("|", "");
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t)";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 状态
						if ("status".equals(statFn)) {
							stat_func_status status = new stat_func_status();
							Object statVal = status.get(finalStValTemp, dbstat,
									(int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 最大值
						if ("max".equals(statFn)) {
							stat_func_max max = new stat_func_max();
							Object statVal = max.get(finalStValTemp, dbstat,
									(int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 二项分布
						if ("bin_dist".equals(statFn)) {
							stat_func_bindist bindist = new stat_func_bindist();
							Object statVal = bindist.get(finalStValTemp,
									dbstat, (int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 相同计数
						if ("count_equals".equals(statFn)) {
							stat_func_count_equals countEquals = new stat_func_count_equals();
							Object statVal = countEquals.get(finalStValTemp,
									dbstat, (int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
						// 最小值
						if ("min".equals(statFn)) {
							stat_func_min min = new stat_func_min();
							Object statVal = min.get(finalStValTemp, dbstat,
									(int) curMin, null);
							if (statVal == null) {
								statVal = 0;
							}
							String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.deviceId="
									+ "'"
									+ deviceId
									+ "'"
									+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.deviceId="
									+ "'" + deviceId + "'" + ")";
							List<Map<String, Object>> list = tmsSimpleDao
									.queryForList(trafficdataSql.toString());
							Map<String, Object> time = list.get(0);
							BigDecimal finishtime = (BigDecimal) time
									.get("FINISHTIME");
							String countSql = "select " + "'" + desc + "'"
									+ " STAT_DESC," + "'" + statVal + "'" + " "
									+ " STOREVALUE, " + "'" + finishtime + "'"
									+ " " + " FINISHTIME from dual";
							Page<Map<String, Object>> statPage = tmsSimpleDao
									.pageQuery(countSql.toString(), reqs,
											new Order());
							return statPage;
						}
					}
				}
				if (queryStatValue == null || queryStatValue.isEmpty()) {
					return null;
				}
				if (storecolumn == null || "".equals(storecolumn)) {
					return null;
				}

			}
			// else {
			// StringBuffer sb3 = new StringBuffer();
			// sb3.append("select  distinct(select b.stat_desc STAT_DESC from tms_com_stat b where b.stat_id = "
			// + "'"
			// + stat_id
			// + "'"
			// + ")"
			// + " STAT_DESC"
			// + ","
			// + "a."
			// + storecolumn
			// + " STOREVALUE "
			// + ","
			// + " a.finishtime  FINISHTIME"
			// +
			// " from tms_run_trafficdata a,tms_dfp_device t where 1=1 and a.finishtime =(select max (finishtime) from tms_run_trafficdata a  where 1=1 and a.txntype = "
			// + "'" + transBranches + "'");
			// sb3.append(" and a.DEVICEID=" + "'" + deviceId + "'" + ")");
			// SimpleDateFormat sdf = new SimpleDateFormat(
			// "yyyy-MM-dd HH:mm:ss");
			// Date startTime1 = null;
			// Date endTime1 = null;
			// if (!StringUtil.isEmpty(startTime)) {
			// // 把时间转成毫秒
			// try {
			// startTime1 = sdf.parse(startTime);
			// startTime = String.valueOf(startTime1.getTime());
			// } catch (ParseException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// sb3.append(" and  a.FINISHTIME>" + startTime);
			// }
			// if (!StringUtil.isEmpty(endTime)) {
			// // 把时间转成毫秒
			// try {
			// endTime1 = sdf.parse(endTime);
			// endTime = String.valueOf(endTime1.getTime());
			// } catch (ParseException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// sb3.append(" and  a.FINISHTIME<=" + endTime);
			//
			// }
			// Page<Map<String, Object>> statPage1 = tmsSimpleDao.pageQuery(
			// sb3.toString(), reqs, new Order());
			// return statPage1;
			// }
		}

		// 如果交易树一定不为空的前提下，根据用户id和用户名字查询统计的情况
		if (!StringUtil.isEmpty(stat_id)) {
			StringBuffer sb = new StringBuffer();
			sb.append("select  * from tms_com_stat a where a.STAT_VALID =1 ");
			sb.append(" and STAT_ID=" + "'" + stat_id + "'");
			List<Map<String, Object>> statList = tmsSimpleDao.queryForList(sb
					.toString());
			Map<String, Object> storecolumn1 = statList.get(0);
			String storecolumn = (String) storecolumn1.get("STORECOLUMN");
			String statDesc = (String) storecolumn1.get("stat_desc");
			String stat_Fn = (String) storecolumn1.get("stat_Fn");
			BigDecimal counUnit = (BigDecimal) storecolumn1.get("counUnit");
			BigDecimal countRound = (BigDecimal) storecolumn1.get("countRound");
			BigDecimal statId = (BigDecimal) storecolumn1.get("stat_id");
			db_stat st = new db_stat();

			if (counUnit != null) {
				st.stat_unit_min = Integer.parseInt(counUnit.toString());
			}
			if (countRound != null) {
				st.stat_num_unit = Integer.parseInt(countRound.toString());
			}

			if (st.stat_num_unit == 0)
				st.stat_num_unit = 1;

			// 20170609 herou 发现直接有存储字段的统计值，直接取字段是不对的。
			// 如果有挂起的时候，由于评估快照，取的值就是以前的值加本次的值。有多次挂起的数据。会分别去计算，不会累计计算。

			// 查询没有存储字段的统计值
			// if(storecolumn == null || "".equals(storecolumn)){ //20170609
			// herou 发现直接有存储字段的统计值，直接取字段是不对的。如果有挂起的时候，由于评估
			if (true) {
				Map<String, Object> dataMap = new HashMap<String, Object>();
				dataMap.put("desc", statDesc);
				dataMap.put("statId", statId);
				dataMap.put("statFn", stat_Fn);
				dataMap.put("db_stat", st);
				String desc = (String) dataMap.get("desc");
				String statFn = (String) dataMap.get("statFn");
				BigDecimal stat_Id = (BigDecimal) dataMap.get("statId");
				db_stat dbstat = (db_stat) dataMap.get("db_stat");
				long cm = System.currentTimeMillis();
				long curMin = cm / 60000;
				char split = txn_stat.ch_split;
				if (!StringUtil.isEmpty(userId)) {
					String sql = "select a.STAT_VALUE statValue from tms_run_stat a where a.STAT_PARAM="
							+ "'" + userId + split + "'";
					List<Map<String, Object>> queryStatValue = tmsSimpleDao
							.queryForList(sql, new HashMap<String, Object>());
					if (queryStatValue != null && queryStatValue.size() > 0) {
						Map<String, Object> StatValue = queryStatValue.get(0);
						String statValue = (String) StatValue.get("statValue");
						String[] statValueArray = statValue.split("\n");
						for (int j = 0; j < statValueArray.length; j++) {
							String finalStValTemp = statValueArray[j];
							String[] statValArray = finalStValTemp.split(":");
							String stIdTmp = statValArray[0];
							String d = PropertiesUtil.getPropInstance().get(
									"tms.stat.web.base64");
							stat_number_encode.setM_codec(Integer.parseInt(d));
							stIdTmp = String.valueOf(stat_number_encode
									.decode_long(stIdTmp));
							boolean isHasStatId = false;
							if (j == 0) {
								for (int t = 0; t < statValueArray.length; t++) {
									String st1 = statValueArray[t];
									String[] stTmp = st1.split(":");
									String statIdTmp = String
											.valueOf(stat_number_encode
													.decode_long(stTmp[0]));
									if (statIdTmp
											.equals(String.valueOf(statId))) {
										isHasStatId = true;
									}
								}
							}
							if (!stIdTmp.equals(String.valueOf(statId))) {

								continue;
							}
							finalStValTemp = finalStValTemp
									.substring(finalStValTemp.indexOf(":") + 1);

							// //计数
							if ("count".equals(statFn)) {
								stat_func_count count = new stat_func_count();
								Object statVal = count.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());

								return statPage;
							}
							// 平均数
							if ("avg".equals(statFn)) {
								stat_func_avg avg = new stat_func_avg();
								Object statVal = avg.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 区间分布
							if ("rang_bin_dist".equals(statFn)) {
								stat_func_bindist_rang bindistRang = new stat_func_bindist_rang();
								Object statVal = bindistRang.get(
										finalStValTemp, dbstat, (int) curMin,
										null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 合计值
							if ("sum".equals(statFn)) {
								stat_func_sum sum = new stat_func_sum();
								Object statVal = sum.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 唯一计数
							if ("count_uniq".equals(statFn)) {
								stat_func_count_uniq count_uniq = new stat_func_count_uniq();
								Object statVal = count_uniq.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 快照
							if ("snapshot".equals(statFn)) {
								String[] snapShotTemp = finalStValTemp
										.split(":");
								String snapShotVal = snapShotTemp[1];
								Object statVal = snapShotVal.replace("|", "");
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 状态
							if ("status".equals(statFn)) {
								stat_func_status status = new stat_func_status();
								Object statVal = status.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 最大值
							if ("max".equals(statFn)) {
								stat_func_max max = new stat_func_max();
								Object statVal = max.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 二项分布
							if ("bin_dist".equals(statFn)) {
								stat_func_bindist bindist = new stat_func_bindist();
								Object statVal = bindist.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 相同计数
							if ("count_equals".equals(statFn)) {
								stat_func_count_equals countEquals = new stat_func_count_equals();
								Object statVal = countEquals.get(
										finalStValTemp, dbstat, (int) curMin,
										null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							// 最小值
							if ("min".equals(statFn)) {
								stat_func_min min = new stat_func_min();
								Object statVal = min.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t  where t.userid="
										+ "'"
										+ userId
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid="
										+ "'" + userId + "'" + ")";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
						}
						if (queryStatValue == null || queryStatValue.isEmpty()) {
							return null;
						}

						if (storecolumn == null || "".equals(storecolumn)) {
							return null;
						}
					}
				}

				if (!StringUtil.isEmpty(userName)) {
					String sql2 = "select a.STAT_VALUE statValue from tms_run_stat a where a.STAT_PARAM="
							+ "'" + userName + split + "'";
					List<Map<String, Object>> queryStatValue1 = tmsSimpleDao
							.queryForList(sql2, new HashMap<String, Object>());
					if (queryStatValue1 != null && queryStatValue1.size() > 0) {
						Map<String, Object> StatValue = queryStatValue1.get(0);
						String statValue = (String) StatValue.get("statValue");
						String[] statValueArray = statValue.split("\n");
						for (int j = 0; j < statValueArray.length; j++) {
							String finalStValTemp = statValueArray[j];
							String[] statValArray = finalStValTemp.split(":");
							String stIdTmp = statValArray[0];
							String d = PropertiesUtil.getPropInstance().get(
									"tms.stat.web.base64");
							stat_number_encode.setM_codec(Integer.parseInt(d));
							stIdTmp = String.valueOf(stat_number_encode
									.decode_long(stIdTmp));
							boolean isHasStatId = false;
							if (j == 0) {
								for (int t = 0; t < statValueArray.length; t++) {
									String st1 = statValueArray[t];
									String[] stTmp = st1.split(":");
									String statIdTmp = String
											.valueOf(stat_number_encode
													.decode_long(stTmp[0]));
									if (statIdTmp
											.equals(String.valueOf(statId))) {
										isHasStatId = true;
									}
								}
							}
							if (!stIdTmp.equals(String.valueOf(statId))) {

								continue;
							}
							finalStValTemp = finalStValTemp
									.substring(finalStValTemp.indexOf(":") + 1);

							// //计数
							if ("count".equals(statFn)) {
								stat_func_count count = new stat_func_count();
								Object statVal = count.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("avg".equals(statFn)) {
								stat_func_avg avg = new stat_func_avg();
								Object statVal = avg.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("rang_bin_dist".equals(statFn)) {
								stat_func_bindist_rang bindistRang = new stat_func_bindist_rang();
								Object statVal = bindistRang.get(
										finalStValTemp, dbstat, (int) curMin,
										null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t  where t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("sum".equals(statFn)) {
								stat_func_sum sum = new stat_func_sum();
								// Object statVal = sum.get(finalStValTemp,
								// dbstat, (int) curMin, null);
								Object statVal = finalStValTemp.substring(
										finalStValTemp.indexOf(":") + 1,
										finalStValTemp.indexOf("|"));
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("count_uniq".equals(statFn)) {
								stat_func_count_uniq count_uniq = new stat_func_count_uniq();
								Object statVal = count_uniq.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("snapshot".equals(statFn)) {
								String[] snapShotTemp = finalStValTemp
										.split(":");
								String snapShotVal = snapShotTemp[1];
								Object statVal = snapShotVal.replace("|", "");
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("status".equals(statFn)) {
								stat_func_status status = new stat_func_status();
								Object statVal = status.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("max".equals(statFn)) {
								stat_func_max max = new stat_func_max();
								Object statVal = max.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("bin_dist".equals(statFn)) {
								stat_func_bindist bindist = new stat_func_bindist();
								Object statVal = bindist.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("count_equals".equals(statFn)) {
								stat_func_count_equals countEquals = new stat_func_count_equals();
								Object statVal = countEquals.get(
										finalStValTemp, dbstat, (int) curMin,
										null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t  where t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}
							if ("min".equals(statFn)) {
								stat_func_min min = new stat_func_min();
								Object statVal = min.get(finalStValTemp,
										dbstat, (int) curMin, null);
								if (statVal == null) {
									statVal = 0;
								}
								String trafficdataSql = "select t.finishtime  from tms_run_trafficdata t ,tms_run_user u where t.userid=u.userid and  u.username="
										+ "'"
										+ userName
										+ "'"
										+ " and t.finishtime=(select max(t.finishtime) from tms_run_trafficdata t where  t.userid=u.userid)";
								List<Map<String, Object>> list = tmsSimpleDao
										.queryForList(trafficdataSql.toString());
								Map<String, Object> time = list.get(0);
								BigDecimal finishtime = (BigDecimal) time
										.get("FINISHTIME");
								String countSql = "select " + "'" + desc + "'"
										+ " STAT_DESC," + "'" + statVal + "'"
										+ " " + " STOREVALUE, " + "'"
										+ finishtime + "'" + " "
										+ " FINISHTIME from dual";
								Page<Map<String, Object>> statPage = tmsSimpleDao
										.pageQuery(countSql.toString(), reqs,
												new Order());
								return statPage;
							}

						}

					}
					if (queryStatValue1 == null || queryStatValue1.isEmpty()) {
						return null;
					}
					if (storecolumn == null || "".equals(storecolumn)) {
						return null;
					}
				}
			}
			// else {
			// StringBuffer sb2 = new StringBuffer();
			// sb2.append("select distinct(select b.stat_desc STAT_DESC from tms_com_stat b where b.stat_id = "
			// + "'"
			// + stat_id
			// + "'"
			// + ")"
			// + " STAT_DESC"
			// + ","
			// + "a."
			// + storecolumn
			// + " STOREVALUE "
			// + ","
			// +
			// " a.finishtime  FINISHTIME from tms_run_trafficdata a,tms_run_user c where 1=1 and c.userid=a.userid and a.USERID="
			// + "'"
			// + userId
			// + "'"
			// + " and a.txntype = "
			// + "'"
			// + transBranches
			// + "'"
			// + " and a.txnstatus='1'"
			// +
			// " and  a.finishtime = (select max (finishtime)  from tms_run_trafficdata a,tms_run_user d where 1=1 and a.userid=d.userid and a.txntype = "
			// + "'" + transBranches + "'" + " and a.txnstatus='1'");
			// SimpleDateFormat sdf = new SimpleDateFormat(
			// "yyyy-MM-dd HH:mm:ss");
			// Date startTime1 = null;
			// Date endTime1 = null;
			// if (!StringUtil.isEmpty(userId)) {
			// sb2.append(" and a.USERID=" + "'" + userId + "'" + ")");
			// }
			// if (!StringUtil.isEmpty(userName)) {
			// sb2.append(" and d.USERNAME=" + "'" + userName + "'" + ")");
			// }
			// if (!StringUtil.isEmpty(startTime)) {
			// // 把时间转成毫秒
			// try {
			// startTime1 = sdf.parse(startTime);
			// startTime = String.valueOf(startTime1.getTime());
			// } catch (ParseException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// sb2.append(" and  a.FINISHTIME>" + startTime);
			// }
			// if (!StringUtil.isEmpty(endTime)) {
			// // 把时间转成毫秒
			// try {
			// endTime1 = sdf.parse(endTime);
			// endTime = String.valueOf(endTime1.getTime());
			// } catch (ParseException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// sb2.append(" and  a.FINISHTIME<=" + endTime);
			//
			// }
			// Page<Map<String, Object>> statPage = tmsSimpleDao.pageQuery(
			// sb2.toString(), reqs, new Order());
			// return statPage;
			// }

		}
		return null;
	}

	@Override
	public List<Map<String, Object>> queryTransBranches() {

		String txn_sql = "SELECT TAB_NAME ID,M.TAB_NAME CODE_KEY,m.parent_tab fid,m.tab_desc CODE_VALUE ,'1' ftype, TAB_NAME STAT_TXN FROM TMS_COM_TAB M WHERE M.is_enable='1' and M.tab_type='4' order by STAT_TXN";
		// 查询交易树
		List<Map<String, Object>> txn_list = tmsSimpleDao.queryForList(txn_sql);
		return txn_list;
	}

	@Override
	public List<Map<String, Object>> queryTransStatFunc(String txnId) {
		StringBuffer stat_sql = new StringBuffer();
		stat_sql.append("SELECT STAT_ID ID,STAT_TXN,STAT_NAME, stat_txn fid,STAT_DESC CODE_VALUE,'2' ftype,STAT_TXN FROM TMS_COM_STAT where STAT_VALID =1");
		stat_sql.append(" and STAT_TXN in("
				+ TransCommon.arr2str(TransCommon.cutToIds(txnId)) + ")");
		// 查询统计
		List<Map<String, Object>> stat_list = tmsSimpleDao
				.queryForList(stat_sql.toString());

		if (stat_list == null || stat_list.size() == 0) {
			return null;
		}

		for (Map<String, Object> map : stat_list) {
			String stat_txn = MapUtil.getString(map, "STAT_TXN");
			String stat_name = MapUtil.getString(map, "STAT_NAME");

			map.put("CODE_KEY", stat_txn + ":" + stat_name);

		}

		return stat_list;
	}
}
