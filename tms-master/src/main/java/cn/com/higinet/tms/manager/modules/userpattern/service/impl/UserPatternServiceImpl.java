/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.userpattern.service.impl;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.common.SequenceService;
import cn.com.higinet.tms.manager.modules.common.StaticParameters;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrServiceException;
import cn.com.higinet.tms.manager.modules.stat.service.StatService;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;
import cn.com.higinet.tms.manager.modules.tran.service.TransDefService;
import cn.com.higinet.tms.manager.modules.userpattern.service.UserPatternService;
import cn.com.higinet.tms35.comm.utf8_split;

/**
 * 功能/模块:
 * 
 * @author zhanglq
 * @version 1.0 Aug 29, 2013 类描述: 修订历史: 日期 作者 参考 描述
 *
 */
@Service("userPatternService35")
public class UserPatternServiceImpl implements UserPatternService {

	private static Log log = LogFactory.getLog(UserPatternServiceImpl.class);


	private static final String split_1 = "#";
	@Autowired
	private SimpleDao tmsSimpleDao;
	@Autowired
	@Qualifier("tmpSimpleDao")
	private SimpleDao tmpSimpleDao;
	@Autowired
	private SimpleDao officialSimpleDao;
	@Autowired
	private TransDefService transDefService;
	@Autowired
	private SequenceService sequenceService;
	@Autowired
	private IPLocationService ipLocationService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#listStatPattern(java.util.Map)
	 */
	public Page<Map<String, Object>> pageStatPattern(Map<String, Object> reqs) {
		Map<String, Object> sqlConds = new HashMap<String, Object>();

		String txnids = MapUtil.getString(reqs, "txnid");
		String stat_desc = MapUtil.getString(reqs, "stat_desc");
		String stat_name = MapUtil.getString(reqs, "stat_name");
		String stat_fn = MapUtil.getString(reqs, "stat_fn");
		String staflag = MapUtil.getString(reqs, "staflag");

		StringBuilder sql = null;

		if ("1".equals(staflag)) {
			sql = new StringBuilder("SELECT * FROM  TMS_COM_STAT, TMS_COM_TAB, TMS_COM_FUNC F WHERE F.FUNC_CODE=STAT_FN AND STAT_TXN=TAB_NAME AND STAT_PARAM = 'USERID' AND (F.FUNC_CODE = 'bin_dist' OR F.FUNC_CODE = 'rang_bin_dist') ");
		} else {
			sql = new StringBuilder("SELECT * FROM  TMS_COM_STAT, TMS_COM_TAB, TMS_COM_FUNC F WHERE F.FUNC_CODE=STAT_FN AND STAT_TXN=TAB_NAME AND STAT_PARAM = 'USERID'  ");
		}
		if (StringUtil.isNotEmpty(txnids)) {
			txnids = txnids.replace(",", "','");
		    sql.append(" AND STAT_TXN IN ('"+txnids+"') ");
			// txnids = txnids.replace(",", "','");
			// sql.append(" AND STAT_TXN IN (:TXNIDS) ");
			// sqlConds.put("TXNIDS", "'"+txnids+"'");

			//String txnIdSql = TransCommon.cutToIdsForSqlInj(txnids);
			//Map<String, Object> txnIdsMap = TransCommon.cutToMapIdsForSqlInj(txnids);

			//sql.append(" AND STAT_TXN IN (" + txnIdSql + ")");
			//sqlConds.putAll(txnIdsMap);
		}
		if (StringUtil.isNotEmpty(stat_desc)) {
			// sql.append(" AND STAT_DESC LIKE '%"+stat_desc+"%' ");
			String statDesc = "%" + stat_desc + "%";
			sql.append(" AND STAT_DESC LIKE :STAT_DESC");
			sqlConds.put("STAT_DESC", statDesc);
		}
		if (StringUtil.isNotEmpty(stat_name)) {
			// sql.append(" AND STAT_NAME LIKE '%"+stat_name+"%' ");
			String statName = "%" + stat_name + "%";
			sql.append(" AND STAT_NAME LIKE :STAT_NAME");
			sqlConds.put("STAT_NAME", statName);
		}
		if (StringUtil.isNotEmpty(stat_fn)) {
			// sql.append(" AND STAT_FN = '"+stat_fn+"' ");
			sql.append(" AND STAT_FN=:STAT_FN");
			sqlConds.put("STAT_FN", stat_fn);
		}

		Page<Map<String, Object>> r = tmsSimpleDao.pageQuery(sql.toString(), sqlConds, new Order().asc("STAT_NAME"));
		List<Map<String, Object>> list = r.getList();
		// String txnSql = tmsSqlMap.getSql("tms.userpattern.queryTxnFeature");
		for (Map<String, Object> map : list) {
			String txn = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_TXN);
			String txnFeature = MapUtil.getString(map, DBConstant.TMS_COM_STAT_STAT_DATAFD);

			String txnSql = "SELECT CODE_KEY,CODE_VALUE,TYPE,CODE fd_code FROM ( SELECT REF_NAME CODE_KEY,NAME CODE_VALUE,TYPE,CODE,TAB_NAME FROM TMS_COM_FD UNION SELECT REF_NAME CODE_KEY,REF_DESC CODE_VALUE,'' TYPE,'' CODE,TAB_NAME FROM TMS_COM_REFFD ) F WHERE F.TAB_NAME IN ("
					+ TransCommon.arr2str(TransCommon.cutToIds(txn)) + ") AND CODE_KEY = ? ORDER BY TAB_NAME";

			// 查询交易属性
			List<Map<String, Object>> l = tmsSimpleDao.queryForList(txnSql, txnFeature);

			if (l != null && l.size() > 0) {
				txnFeature = MapUtil.getString(l.get(0), "CODE_VALUE");
			}

			map.put(DBConstant.TMS_COM_STAT_STAT_DATAFD, txnFeature);// 翻译统计目标

			// 查询交易树
			map.put("TAB_DESC", transDefService.getSelfAndParentTranDefAsStr(txn));// 所属交易层级显示
		}
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#saveUserPattern(java.util.Map)
	 */
	@Transactional
	public Map<String, Object> saveUserPattern(Map<String, List<Map<String, ?>>> formList, String userId, String statid) {
		List<Map<String, Object>> delList = MapUtil.getList(formList, "del");
		List<Map<String, Object>> modList = MapUtil.getList(formList, "mod");
		List<Map<String, Object>> addList = MapUtil.getList(formList, "add");

		Map<String, Object> rmap = new HashMap<String, Object>();

		// 查询该用户自定义行为习惯
		Map<String, Object> upMap = getUserpattern(userId);

		Map<String, List<String[]>> patternMap = userPattern2Map(MapUtil.getString(upMap, "USER_PATTERN") + MapUtil.getString(upMap, "USER_PATTERN_1") + MapUtil.getString(upMap, "USER_PATTERN_C"));
		// 新增
		if (addList != null && addList.size() > 0) {
			for (Map<String, Object> map : addList) {

				// 校验重复的自定义行为习惯定义
				String[] new_us = checkDuplicateUserPattern(map, patternMap, statid);

				// insert TMS_COM_USERPATTERN
				Map<String, Object> im = new HashMap<String, Object>();

				im.put("USERID", userId);
				im.put("USER_PATTERN", new_us[0]);
				im.put("USER_PATTERN_1", new_us[1]);
				im.put("USER_PATTERN_C", new_us[2]);
				if (MapUtil.isEmpty(upMap)) {
					tmsSimpleDao.create("TMS_COM_USERPATTERN", im);
				} else {
					Map<String, Object> cond = new HashMap<String, Object>();
					cond.put("USERID", MapUtil.getString(upMap, "USERID"));
					tmsSimpleDao.update("TMS_COM_USERPATTERN", im, cond);
				}

				rmap = map;
			}
		}
		// 修改
		if (modList != null && modList.size() > 0) {
			for (Map<String, Object> map : modList) {

				// 校验重复的自定义行为习惯定义
				String[] new_us = checkDuplicateUserPattern(map, patternMap, statid);

				// insert TMS_COM_USERPATTERN
				Map<String, Object> im = new HashMap<String, Object>();
				im.put("USERID", userId);
				im.put("USER_PATTERN", new_us[0]);
				im.put("USER_PATTERN_1", new_us[1]);
				im.put("USER_PATTERN_C", new_us[2]);

				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put("USERID", MapUtil.getString(upMap, "USERID"));
				tmsSimpleDao.update("TMS_COM_USERPATTERN", im, cond);

				rmap = map;
			}
		}
		// 删除
		if (delList != null && delList.size() > 0) {
			String new_us[] = batchDel(statid, delList, patternMap);

			Map<String, Object> im = new HashMap<String, Object>();
			im.put("USERID", userId);
			if (new_us != null) {
				im.put("USER_PATTERN", new_us[0]);
				im.put("USER_PATTERN_1", new_us[1]);
				im.put("USER_PATTERN_C", new_us[2]);
				Map<String, Object> cond = new HashMap<String, Object>();
				cond.put("USERID", MapUtil.getString(upMap, "USERID"));
				tmsSimpleDao.update("TMS_COM_USERPATTERN", im, cond);
			} else {
				tmsSimpleDao.delete("TMS_COM_USERPATTERN", im);
			}

		}

		return rmap;
	}

	private String getUserPatternSeqId() {
		// 查询SEQUENCE
		return sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_COM_USERPATTER_ID);
	}

	private String[] batchDel(String statid, List<Map<String, Object>> delList, Map<String, List<String[]>> patternMap) {
		List<String[]> list = (List<String[]>) MapUtil.getObject(patternMap, statid);

		List<String[]> new_list = new ArrayList<String[]>();

		for (String[] s : list) {
			new_list.add(s);
		}

		for (Map<String, Object> map : delList) {
			map.put("STAT_ID", statid);// 授权使用
			String in_id = MapUtil.getString(map, "UP_ID");

			for (String[] s : list) {
				String db_id = s[StaticParameters.UPID_INDEX];
				if (in_id.equals(db_id)) {
					new_list.remove(s);
				}
			}
		}

		if (new_list.size() == 0) {
			patternMap.remove(statid);
		} else {
			patternMap.put(statid, new_list);
		}

		return this.map2UserPattern(patternMap);
	}

	public Map<String, Object> getOneUserPattern(String user_id, String stat_id, String pattern_id, String up_s) {
		// 查找临时库中的数据
		Map<String, List<String[]>> up_map_t = this.userPattern2Map(up_s);
		// 通过行为习惯ID查找行为习惯
		Object object = MapUtil.getObject(up_map_t, stat_id);
		List<String[]> s_u_l_t = object != null ? (List<String[]>) object : new ArrayList<String[]>();
		Map<String, Object> r = new HashMap<String, Object>();
		for (String[] s : s_u_l_t) {
			if (s.length > StaticParameters.UPID_INDEX && s[StaticParameters.UPID_INDEX].equals(pattern_id)) {
				r.put("STATID", s[StaticParameters.STATID_INDEX]);
				r.put("UPVALUE", s[StaticParameters.UPVALUE_INDEX]);
				r.put("STARTDATE", s[StaticParameters.STARTDATE_INDEX]);
				r.put("ENDDATE", s[StaticParameters.ENDDATE_INDEX]);
				break;
			}
		}
		return r;
	}

	private String[] map2UserPattern(Map<String, List<String[]>> m) {
		StringBuilder userPatter = new StringBuilder();

		if (MapUtil.isEmpty(m))
			return null;

		Set<Entry<String, List<String[]>>> enSet = m.entrySet();
		String[] r = new String[3];

		for (Entry<String, List<String[]>> entry : enSet) {
			List<String[]> oneStat = entry.getValue();
			for (String[] s : oneStat) {
				if (s.length < 5)
					continue;
				userPatter.append(s[StaticParameters.STATID_INDEX]).append(split_1).append(s[StaticParameters.UPVALUE_INDEX]).append(split_1).append(s[StaticParameters.STARTDATE_INDEX]).append(split_1).append(s[StaticParameters.ENDDATE_INDEX]).append(split_1)
						.append(s[StaticParameters.UPID_INDEX]).append("|");
			}
			userPatter.append("\n");
		}

		utf8_split s = new utf8_split(userPatter.toString(), 4000);

		/*
		 * r[0] = userPatter.length() <= 4000 ? userPatter.substring(0, userPatter.length()):userPatter.substring(0, 4000); r[1] = (userPatter.length() < 4000) ? "" :(userPatter.length() > 4000 && userPatter.length() <= 8000) ? userPatter.substring(4001, userPatter.length()):userPatter.substring(4001, 8000); r[2] = (userPatter.length() > 8000) ? userPatter.substring(8001, userPatter.length()):"";
		 */
		r[0] = s.s1;
		r[1] = s.s2;
		r[2] = s.s3;
		return r;
	}

	private Map<String, List<String[]>> userPattern2Map(String us) {

		Map<String, List<String[]>> r = new HashMap<String, List<String[]>>();

		if (StringUtil.isEmpty(us))
			return r;

		String[] oneStat = us.split("\n");

		for (int i = 0; i < oneStat.length; i++) {
			String[] oneStat_p = oneStat[i].split("\\|");

			List<String[]> list = new ArrayList<String[]>();

			String stat_id = "";

			for (int j = 0; j < oneStat_p.length; j++) {
				String[] oneStat_p_split = oneStat_p[j].split(split_1);
				list.add(oneStat_p_split);

				stat_id = oneStat_p_split[StaticParameters.STATID_INDEX];
			}

			r.put(stat_id, list);
		}

		return r;
	}

	/**
	 * 方法描述:
	 * 
	 * @param userId
	 * @return
	 */
	private Map<String, Object> getUserpattern(String userId) {
		List<Map<String, Object>> upList = tmsSimpleDao.queryForList("select * from TMS_COM_USERPATTERN where USERID = ?", userId);
		return upList == null || upList.size() <= 0 ? new HashMap() : upList.get(0);
	}

	/**
	 * 方法描述:校验重复的自定义行为习惯定义
	 * 
	 * @param map
	 */
	private String[] checkDuplicateUserPattern(Map<String, Object> map, Map<String, List<String[]>> upMap, String statid) {
		String in_value1 = MapUtil.getString(map, "UP_NAME1");
		String in_value2 = MapUtil.getString(map, "UP_NAME2");

		String up_text = MapUtil.getString(map, "UP_TEXT");
		String in_id = MapUtil.getString(map, "UP_ID");
		String in_value = MapUtil.getString(map, "UP_VALUE");
		String in_startDate = MapUtil.getString(map, "START_DATE");
		String in_endDate = MapUtil.getString(map, "END_DATE");
		String stat_fn = MapUtil.getString(map, "STAT_FN");
		String stat_datafd = MapUtil.getString(map, "STAT_DATAFD");

		String[] split = in_value.split("\\|");
		if (stat_datafd.equalsIgnoreCase("COUNTRYCODE")) {
			in_value = split.length > 0 ? split[0] : in_value;
		}
		if (stat_datafd.equalsIgnoreCase("REGIONCODE")) {
			in_value = split.length > 1 ? split[1] : in_value;
		}
		if (stat_datafd.equalsIgnoreCase("CITYCODE")) {
			in_value = split.length > 2 ? split[2] : in_value;
		}

		map.put("STAT_ID", statid);// 授权使用

		List<String[]> list = (List<String[]>) MapUtil.getObject(upMap, statid);
		if (list == null)
			list = new ArrayList<String[]>();
		String[] old = null;
		for (String[] s : list) {
			String db_value = s[StaticParameters.UPVALUE_INDEX];
			String db_startDate = s[StaticParameters.STARTDATE_INDEX];
			String db_endDate = s[StaticParameters.ENDDATE_INDEX];
			String db_id = s[StaticParameters.UPID_INDEX];

			if (stat_fn.equals("rang_bin_dist") || stat_fn.equals("bin_dist")) {
				if (db_value.equals(in_value)) {
					if (in_id.compareTo(db_id) != 0 && ((in_startDate.compareTo(db_startDate) >= 0 && (in_endDate.compareTo(db_endDate) <= 0 || StringUtil.isEmpty(db_endDate))))) {
						throw new TmsMgrServiceException("行为习惯值[" + up_text + "]已定义");
					}
				}
			} else {
				if (in_id.compareTo(db_id) != 0
						&& (in_startDate.compareTo(db_startDate) <= 0 && (in_endDate.compareTo(db_startDate) >= 0 || StringUtil.isEmpty(in_endDate)) || (in_startDate.compareTo(db_startDate) >= 0 && (in_startDate.compareTo(db_endDate) <= 0 || StringUtil.isEmpty(db_endDate))))) {
					throw new TmsMgrServiceException("一个有效期内，只能配置一个行为习惯值");
				}
			}

			// 修改自定义行为习惯
			if (StringUtil.isNotEmpty(in_id)) {
				if (db_id.compareTo(in_id) == 0) {
					old = s;
				}
			}
		}

		// 如果是修改，先把原来的删除
		if (old != null)
			list.remove(old);

		if (StringUtil.isEmpty(in_id)) {
			in_id = getUserPatternSeqId();
			map.put("UP_ID", in_id);
		}

		String[] a = { statid, in_value, in_startDate, in_endDate, in_id };

		list.add(a);

		upMap.put(statid, list);

		return this.map2UserPattern(upMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#pageUser(java.util.Map)
	 */
	public Page<Map<String, Object>> pageUser(Map<String, Object> reqs) {
		Map<String, Object> sqlConds = new HashMap<String, Object>();
		StringBuilder sql = new StringBuilder("select * from TMS_RUN_USER ");
		String userid = MapUtil.getString(reqs, "USER_ID");
		String username = MapUtil.getString(reqs, "USER_NAME");
		if (StringUtil.isNotEmpty(userid) || StringUtil.isNotEmpty(username)) {
			sql.append(" where 1=1 ");
			if (StringUtil.isNotEmpty(userid)) {
				// sql.append(" and USERID = '"+userid+"'");
				sql.append(" AND USERID =:USERID");
				sqlConds.put("USERID", userid);
			}
			if (StringUtil.isNotEmpty(username)) {
				// sql.append(" and USERNAME = '"+username+"'");
				sql.append(" AND USERNAME =:USERNAME");
				sqlConds.put("USERNAME", username);
			}

		}
		return tmsSimpleDao.pageQuery(sql.toString(), reqs, new Order().asc("USERID"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#queryUserPatternList(java.lang.String, java.lang.String)
	 */
	public List<Map<String, Object>> queryUserPatternList(Map<String, Object> reqs, Map<String, Object> statInfo) {
		String userId = MapUtil.getString(reqs, "user_id");
		String stat_id = MapUtil.getString(reqs, "stat_id");

		String data_fd = MapUtil.getString(statInfo, DBConstant.TMS_COM_STAT_STAT_DATAFD);
		String data_fn = MapUtil.getString(statInfo, DBConstant.TMS_COM_STAT_STAT_FN);

		Map<String, Object> userPatternMap = this.getUserpattern(userId);
		String ps = MapUtil.getString(userPatternMap, "USER_PATTERN");
		String ps1 = MapUtil.getString(userPatternMap, "USER_PATTERN_1");
		String ps2 = MapUtil.getString(userPatternMap, "USER_PATTERN_C");
		if (ps.length() > 0 && ps1.length() > 0) {
			ps += ps1;
		}
		if (ps.length() > 0 && ps2.length() > 0) {
			ps += ps2;
		}

		Map<String, List<String[]>> p = this.userPattern2Map(ps);

		List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();

		List<String[]> o = p.get(stat_id);
		if (o != null && o.size() > 0) {
			for (String[] ol : o) {
				Map<String, Object> oneStat = new HashMap<String, Object>();
				oneStat.put("STAT_DATAFD", data_fd);
				oneStat.put("STAT_FN", data_fn);
				oneStat.put("UP_ID", ol.length > StaticParameters.UPID_INDEX ? ol[StaticParameters.UPID_INDEX] : "");

				String up_value = ol.length > StaticParameters.UPVALUE_INDEX ? ol[StaticParameters.UPVALUE_INDEX] : "";

				String up_name = "";
				String up_value_t = "";

				Map<String, Object> c = new HashMap<String, Object>();
				if (data_fd.equalsIgnoreCase("COUNTRYCODE")) {
					c.put("country_code", up_value);
					List<Map<String, Object>> a = this.getCountry(c);
					up_name = a != null && a.size() > 0 ? MapUtil.getString(a.get(0), "CODE_VALUE") : "";
				} else if (data_fd.equalsIgnoreCase("CITYCODE")) {
					List<Map<String, Object>> re_list = officialSimpleDao.queryForList(
							"select c.CITYCODE,c.CITYNAME,r.REGIONNAME,r.REGIONCODE,co.COUNTRYNAME,co.COUNTRYCODE from " + ipLocationService.getLocationCurrName("TMS_MGR_CITY") + " c ," + ipLocationService.getLocationCurrName("TMS_MGR_REGION") + " r,"
									+ ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY") + " co where c.REGIONCODE=r.REGIONCODE and c.COUNTRYCODE = co.COUNTRYCODE and r.COUNTRYCODE=co.COUNTRYCODE and c.CITYCODE = ?", up_value);
					up_name = re_list != null && re_list.size() > 0 ? MapUtil.getString(re_list.get(0), "COUNTRYNAME") + "-" + MapUtil.getString(re_list.get(0), "REGIONNAME") + "-" + MapUtil.getString(re_list.get(0), "CITYNAME") : "";
					up_value_t = re_list != null && re_list.size() > 0 ? MapUtil.getString(re_list.get(0), "COUNTRYCODE") + "|" + MapUtil.getString(re_list.get(0), "REGIONCODE") + "|" + MapUtil.getString(re_list.get(0), "CITYCODE") : "";
				} else if (data_fd.equalsIgnoreCase("REGIONCODE")) {
					List<Map<String, Object>> re_list = officialSimpleDao.queryForList(
							"select c.COUNTRYNAME,c.COUNTRYCODE,r.REGIONCODE,r.REGIONNAME from " + ipLocationService.getLocationCurrName("TMS_MGR_REGION") + " r," + ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY")
									+ " c where r.COUNTRYCODE=c.COUNTRYCODE and r.REGIONCODE = ?", up_value);
					up_name = re_list != null && re_list.size() > 0 ? MapUtil.getString(re_list.get(0), "COUNTRYNAME") + "-" + MapUtil.getString(re_list.get(0), "REGIONNAME") : "";
					up_value_t = re_list != null && re_list.size() > 0 ? MapUtil.getString(re_list.get(0), "COUNTRYCODE") + "|" + MapUtil.getString(re_list.get(0), "REGIONCODE") : "";
				}

				oneStat.put("UP_NAME", up_name.length() == 0 ? up_value : up_name);
				oneStat.put("UP_VALUE", up_value_t.length() == 0 ? up_value : up_value_t);

				oneStat.put("START_DATE", ol.length > StaticParameters.STARTDATE_INDEX ? ol[StaticParameters.STARTDATE_INDEX] : "");
				oneStat.put("END_DATE", ol.length > StaticParameters.ENDDATE_INDEX ? ol[StaticParameters.ENDDATE_INDEX] : "");
				r.add(oneStat);
			}
		}

		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#queryStatPatternList(java.util.Map)
	 */
	public List<Map<String, Object>> queryStatPatternList(Map<String, Object> reqs) {
		return null;
	}

	/**
	 * 方法描述:
	 * 
	 * @param stat_value
	 * @param stat_fd
	 * @param code
	 * @return
	 */

	private String code2Text(String stat_value, String stat_fd, String code) {
		if (code != null && code.length() > 0) {
			String sql = "select * from CMC_CODE where CATEGORY_ID = ? and CODE_KEY = ?";
			List<Map<String, Object>> codeList = tmsSimpleDao.queryForList(sql, code, stat_fd);
			if (codeList == null || codeList.size() == 0)
				return stat_value;
			stat_value = MapUtil.getString(codeList.get(0), "CODE_VALUE");
		} else if ("COUNTRYCODE".equalsIgnoreCase(stat_fd)) {
			String sql = "select * from " + ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY") + " where COUNTRYCODE = ?";
			List<Map<String, Object>> codeList = tmsSimpleDao.queryForList(sql, stat_value);
			if (codeList == null || codeList.size() == 0)
				return stat_value;
			stat_value = MapUtil.getString(codeList.get(0), "COUNTRYNAME");
		} else if ("REGIONCODE".equalsIgnoreCase(stat_fd)) {
			String sql = "select * from " + ipLocationService.getLocationCurrName("TMS_MGR_REGION") + " where REGIONCODE = ?";
			List<Map<String, Object>> codeList = tmsSimpleDao.queryForList(sql, stat_value);
			if (codeList == null || codeList.size() == 0)
				return stat_value;
			stat_value = MapUtil.getString(codeList.get(0), "REGIONNAME");
		} else if ("CITYCODE".equalsIgnoreCase(stat_fd)) {
			String sql = "select * from " + ipLocationService.getLocationCurrName("TMS_MGR_CITY") + " where CITYCODE = ?";
			List<Map<String, Object>> codeList = tmsSimpleDao.queryForList(sql, stat_value);
			if (codeList == null || codeList.size() == 0)
				return stat_value;
			stat_value = MapUtil.getString(codeList.get(0), "CITYNAME");
		}
		return stat_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#synUserPatternData(java.lang.String, java.lang.String, boolean)
	 */
	public void synUserPatternData(String userId, String statId, String patternId, String operate, boolean isPass) {

		Map<String, Object> tmp_p = tmpSimpleDao.retrieve("TMS_COM_USERPATTERN", MapWrap.map("USERID", userId).getMap());
		Map<String, Object> off_p = officialSimpleDao.retrieve("TMS_COM_USERPATTERN", MapWrap.map("USERID", userId).getMap());

		// 查找临时库中的数据
		Map<String, List<String[]>> up_map_t = this.userPattern2Map(MapUtil.getString(tmp_p, "USER_PATTERN") + MapUtil.getString(tmp_p, "USER_PATTERN_1") + MapUtil.getString(tmp_p, "USER_PATTERN_C"));
		// 通过行为习惯ID查找行为习惯
		Object object = MapUtil.getObject(up_map_t, statId);
		List<String[]> s_u_l_t = object != null ? (List<String[]>) object : new ArrayList<String[]>();
		String[] sysup_t = null;
		for (String[] s : s_u_l_t) {
			if (s.length > StaticParameters.UPID_INDEX && s[StaticParameters.UPID_INDEX].equals(patternId)) {
				sysup_t = s;
				break;
			}
		}

		// 查找正式库中的数据
		Map<String, List<String[]>> up_map_o = this.userPattern2Map(MapUtil.getString(off_p, "USER_PATTERN") + MapUtil.getString(off_p, "USER_PATTERN_1") + MapUtil.getString(off_p, "USER_PATTERN_C"));
		// 通过行为习惯ID查找行为习惯
		Object object_o = MapUtil.getObject(up_map_o, statId);
		List<String[]> s_u_l_o = object_o != null ? (List<String[]>) object_o : new ArrayList<String[]>();
		String[] sysup_o = null;
		for (String[] s : s_u_l_o) {
			if (s.length > StaticParameters.UPID_INDEX && s[StaticParameters.UPID_INDEX].equals(patternId)) {
				sysup_o = s;
				break;
			}
		}

		if ("add".equals(operate)) {
			// 临时库中没有找到行为习惯，直接返回
			if (sysup_t == null)
				return;

			// 授权通过，临时库同步到正式库
			if (isPass) {
				// 把找到的行为习惯插入到正式库
				s_u_l_o.add(sysup_t);

				up_map_o.remove(statId);
				up_map_o.put(statId, s_u_l_o);

				String[] up_value = this.map2UserPattern(up_map_o);

				if (MapUtil.isEmpty(off_p)) {
					// 插入正式库
					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);
					officialSimpleDao.create("TMS_COM_USERPATTERN", in_db);
				} else {
					// 更新正式库
					Map<String, Object> cond_db = new HashMap<String, Object>();
					cond_db.put("USERID", userId);

					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);

					officialSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
				}
			} else {
				// 授权不通过，把临时库中的数据删除
				s_u_l_t.remove(sysup_t);
				if (s_u_l_t.size() == 0)
					up_map_t.remove(statId);

				if (MapUtil.isEmpty(up_map_t)) {
					// 删除临时库
					tmpSimpleDao.delete("TMS_COM_USERPATTERN", MapWrap.map("USERID", userId).getMap());
				} else {
					// 更新临时库
					Map<String, Object> cond_db = new HashMap<String, Object>();
					cond_db.put("USERID", userId);

					String[] up_value = this.map2UserPattern(up_map_t);
					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);

					tmpSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
				}

			}
		} else if ("mod".equals(operate)) {

			// 临时库中没有找到行为习惯，直接返回
			if (sysup_t == null)
				return;

			// 正式库中没有找到行为习惯，直接返回
			if (sysup_o == null)
				return;

			// 授权通过，临时库同步到正式库
			if (isPass) {
				s_u_l_o.remove(sysup_o);// 删除旧数据
				s_u_l_o.add(sysup_t);// 增加新数据

				up_map_o.remove(statId);
				up_map_o.put(statId, s_u_l_o);

				String[] up_value = this.map2UserPattern(up_map_o);

				// 更新正式库
				Map<String, Object> cond_db = new HashMap<String, Object>();
				cond_db.put("USERID", userId);

				Map<String, Object> in_db = new HashMap<String, Object>();
				in_db.put("USERID", userId);
				in_db.put("USER_PATTERN", up_value[0]);
				in_db.put("USER_PATTERN_1", up_value[1]);
				in_db.put("USER_PATTERN_C", up_value[2]);

				officialSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
			} else {
				// 授权不通过，正式库同步到临时库
				s_u_l_t.remove(sysup_t);// 删除新数据
				s_u_l_t.add(sysup_o);// 增加旧数据

				up_map_t.remove(statId);
				up_map_t.put(statId, s_u_l_t);

				String[] up_value = this.map2UserPattern(up_map_t);

				// 更新临时库
				Map<String, Object> cond_db = new HashMap<String, Object>();
				cond_db.put("USERID", userId);

				Map<String, Object> in_db = new HashMap<String, Object>();
				in_db.put("USERID", userId);
				in_db.put("USER_PATTERN", up_value[0]);
				in_db.put("USER_PATTERN_1", up_value[1]);
				in_db.put("USER_PATTERN_C", up_value[2]);

				tmpSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
			}
		} else if ("del".equals(operate)) {

			// 正式库中没有找到行为习惯，直接返回
			if (sysup_o == null)
				return;

			if (isPass) {
				// 授权通过，更新或删除正式库
				s_u_l_o.remove(sysup_o);
				if (s_u_l_o.size() == 0)
					up_map_o.remove(statId);

				if (MapUtil.isEmpty(up_map_o)) {
					// 删除临时库
					officialSimpleDao.delete("TMS_COM_USERPATTERN", MapWrap.map("USERID", userId).getMap());
				} else {
					// 更新临时库
					Map<String, Object> cond_db = new HashMap<String, Object>();
					cond_db.put("USERID", userId);

					String[] up_value = this.map2UserPattern(up_map_o);
					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);

					officialSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
				}
			} else {
				// 授权不通过，更新或增加临时库
				s_u_l_t.add(sysup_o);

				up_map_t.remove(statId);
				up_map_t.put(statId, s_u_l_t);

				String[] up_value = this.map2UserPattern(up_map_t);

				if (MapUtil.isEmpty(tmp_p)) {
					// 插入正式库
					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);
					tmpSimpleDao.create("TMS_COM_USERPATTERN", in_db);
				} else {
					// 更新正式库
					Map<String, Object> cond_db = new HashMap<String, Object>();
					cond_db.put("USERID", userId);

					Map<String, Object> in_db = new HashMap<String, Object>();
					in_db.put("USERID", userId);
					in_db.put("USER_PATTERN", up_value[0]);
					in_db.put("USER_PATTERN_1", up_value[1]);
					in_db.put("USER_PATTERN_C", up_value[2]);

					tmpSimpleDao.update("TMS_COM_USERPATTERN", in_db, cond_db);
				}
			}
		} else {
			log.debug("unmatch oper type");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#getCountry(java.util.Map)
	 */
	public List<Map<String, Object>> getCountry(Map<String, Object> reqs) {
		StringBuilder sql = new StringBuilder("SELECT COUNTRYNAME CODE_VALUE,COUNTRYCODE CODE_KEY FROM " + ipLocationService.getLocationCurrName("TMS_MGR_COUNTRY") + " where 1=1 ");
		Map<String, Object> c = new HashMap<String, Object>();
		String country_code = MapUtil.getString(reqs, "country_code");
		if (country_code.length() > 0) {
			sql.append(" and COUNTRYCODE = :countrycode");
			c.put("countrycode", country_code);
		}
		sql.append(" order by COUNTRYNAME");
		return officialSimpleDao.queryForList(sql.toString(), c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#getCity(java.util.Map)
	 */
	public List<Map<String, Object>> getCity(Map<String, Object> reqs) {
		StringBuilder sql = new StringBuilder("SELECT CITYNAME CODE_VALUE,CITYCODE CODE_KEY FROM " + ipLocationService.getLocationCurrName("TMS_MGR_CITY") + " WHERE 1=1 ");
		Map<String, Object> c = new HashMap<String, Object>();
		String country_code = MapUtil.getString(reqs, "country_code");
		String region_code = MapUtil.getString(reqs, "region_code");
		String city_code = MapUtil.getString(reqs, "city_code");
		if (country_code.length() > 0) {
			sql.append(" and COUNTRYCODE = :countrycode");
			c.put("countrycode", country_code);
		}
		if (region_code.length() > 0) {
			sql.append(" and REGIONCODE = :regioncode");
			c.put("regioncode", region_code);
		}
		if (city_code.length() > 0) {
			sql.append(" and CITYCODE = :citycode");
			c.put("citycode", city_code);
		}
		sql.append(" order by CITYNAME");
		return officialSimpleDao.queryForList(sql.toString(), c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.com.higinet.tms35.manage.userpattern.service.UserPatternService#getRegion(java.util.Map)
	 */
	public List<Map<String, Object>> getRegion(Map<String, Object> reqs) {
		StringBuilder sql = new StringBuilder("SELECT REGIONNAME CODE_VALUE,REGIONCODE CODE_KEY FROM TMS_MGR_REGION_N where 1=1 ");
		Map<String, Object> c = new HashMap<String, Object>();
		String country_code = MapUtil.getString(reqs, "country_code");
		if (country_code.length() > 0) {
			sql.append(" and COUNTRYCODE = :countrycode");
			c.put("countrycode", country_code);
		}
		sql.append(" order by REGIONNAME");
		return officialSimpleDao.queryForList(sql.toString(), c);
	}

}
