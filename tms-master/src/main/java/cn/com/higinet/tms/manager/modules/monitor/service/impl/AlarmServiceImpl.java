package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.common.IPLocationService;
import cn.com.higinet.tms.manager.modules.monitor.service.AlarmService;

@Transactional
@Service("alarmService")
public class AlarmServiceImpl implements AlarmService { 

	@Autowired
	@Qualifier("tmsSimpleDao")
	private SimpleDao tmsSimpleDao;
	@Autowired
	private IPLocationService ipLocationService;

	/**
	 * 查询所有国家
	 */
	
	public List<Map<String, Object>> listAllCountry(){
//		String sql = "select COUNTRYCODE,COUNTRYNAME from TMS_MGR_COUNTRY";
		StringBuffer strSql = new StringBuffer();
		strSql.append(" SELECT ").append(DBConstant.TMS_MGR_COUNTRY_COUNTRYCODE)
		.append(",").append(DBConstant.TMS_MGR_COUNTRY_COUNTRYNAME)
		.append(",").append(DBConstant.TMS_MGR_REGION_COUNTRYORDER)
		.append(" FROM ").append(ipLocationService.getLocationCurrName(DBConstant.TMS_MGR_COUNTRY))
		.append(" ORDER BY ").append(DBConstant.TMS_MGR_REGION_COUNTRYORDER);
		return tmsSimpleDao.queryForList(strSql.toString());
	}
	
	/**
	 * 查询某国家的地区
	 */
	
	public List<Map<String, Object>> listRegionByCountry(String country) {
		StringBuffer strSql = new StringBuffer();
		strSql.append(" SELECT ").append(DBConstant.TMS_MGR_REGION_REGIONCODE)
		.append(",").append(DBConstant.TMS_MGR_REGION_REGIONNAME)
		.append(" FROM ").append(ipLocationService.getLocationCurrName(DBConstant.TMS_MGR_REGION))
		.append(" WHERE ").append(DBConstant.TMS_MGR_REGION_COUNTRYCODE)
		.append("='").append(country+"'").append(" ORDER BY ")
		.append(DBConstant.TMS_MGR_REGION_COUNTRYCODE).append(",")
		.append(DBConstant.TMS_MGR_REGION_REGIONCODE);
		return tmsSimpleDao.queryForList(strSql.toString());
	}
	
	/**
	 * 查询某地区的城市
	 */
	
	public List<Map<String, Object>> listCityByRegion(String region) {
		StringBuffer strSql = new StringBuffer();
		strSql.append(" SELECT ").append(DBConstant.TMS_MGR_CITY_CITYCODE)
		.append(",").append(DBConstant.TMS_MGR_CITY_CITYNAME)
		.append(" FROM ").append(ipLocationService.getLocationCurrName(DBConstant.TMS_MGR_CITY))
		.append(" WHERE ").append(DBConstant.TMS_MGR_CITY_REGIONCODE)
		.append("='").append(region+"'").append(" ORDER BY ")
		.append(DBConstant.TMS_MGR_CITY_COUNTRYCODE).append(",")
		.append(DBConstant.TMS_MGR_CITY_REGIONCODE).append(",")
		.append(DBConstant.TMS_MGR_CITY_CITYCODE);
		return tmsSimpleDao.queryForList(strSql.toString());
	}
	
	public List<Map<String, Object>> listTransactionTrafficAlarm(Map<String, String> conds) {
		String trafficid = (String)conds.get("trafficid");
		
		StringBuffer strSql = new StringBuffer();
		//SELECT
		strSql.append(" SELECT");
		//告警主键值
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_ALERTID).append("||'' ALERTID,");
		//命中规则数量
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_HITRULENUM).append(" HITRULENUM,");
		//触发规则数量
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_TRIGRULENUM).append(" TRIGRULENUM,");
		//分值
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_SCORE).append(" SCORE,");
		//处置方式
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_DISPOSAL).append(" DISPOSAL,");
		//风险等级
		strSql.append(" tra." + DBConstant.TMS_RUN_ALERT_RISKLEVEL).append(" RISKLEVEL");
		//FROM
		strSql.append(" FROM ");
		strSql.append(DBConstant.TMS_RUN_ALERT + " tra");
		//WHERE
		strSql.append(" WHERE tra.");
		strSql.append(DBConstant.TMS_RUN_ALERT_TRAFFICID).append("=?");
		
		return tmsSimpleDao.queryForList(strSql.toString(), new BigDecimal(trafficid));
	}
}