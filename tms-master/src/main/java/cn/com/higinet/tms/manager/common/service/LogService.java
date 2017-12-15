/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.DBConstant;
import cn.com.higinet.tms.manager.common.service.LogService;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 日志实现类
 * @author zhangfg
 * @version 2.0.0, 2011-6-30
 */
@Transactional
@Service("cmcLogService")
public class LogService {

	/**
	 * 规格化时间数据样式
	 */
	private static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
	private static final Log logger = LogFactory.getLog(LogService.class);
	
	@Autowired
	private SimpleDao cmcSimpleDao;

	/**
	 * 服务列表接口
	 * @param conds 条件参数 （Map<列名称, 列值>）
	 * @return 页对象（分页查询后结果）
	 */
	public Page<Map<String, Object>> listLog(Map<String, Object> conds) {
		String sql  = "select log.LOG_ID,oper.REAL_NAME,oper.LOGIN_NAME,log.OPERATE_TIME,fun.FUNC_NAME,log.OPERATE_RESULT,log.OPERATE_DATA,log.LOG_MAC";
		sql+=" from "+DBConstant.CMC_OPERATE_LOG+" log left join "+DBConstant.CMC_OPERATOR+" oper on log.OPERATOR_ID=oper.OPERATOR_ID left join "+DBConstant.CMC_FUNC+" fun ";
		sql+=" on log.FUNC_ID=fun.FUNC_ID  where 1=1 and log.OPERATE_RESULT='1' ";
		
		String operId = CmcStringUtil.objToString(conds.get(DBConstant.CMC_OPERATOR_OPERATOR_ID));
		
		if(!CmcStringUtil.isBlank(operId) && !"1".equals(operId)){//除超级管理员外，只能查看自己的操作日志
			sql += " and log.OPERATOR_ID= :OPERATOR_ID";
		}
		if(conds.get("operator_name")!=null && !"".equals(conds.get("operator_name"))){
			sql +=" and oper.REAL_NAME = '"+conds.get("operator_name")+"' ";
		}
		if(conds.get("operate_time")!=null && !"".equals(conds.get("operate_time"))){
			String start_time = conds.get("operate_time").toString();
			if(start_time.length()<=10){
				start_time+=" 00:00:00";
			}
			sql +=" and log.OPERATE_TIME >=:startTime";
			try {
				conds.put("startTime", sf.parse(start_time));
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
		}
		if(conds.get("end_time")!=null && !"".equals(conds.get("end_time"))){
			String end_time = conds.get("end_time").toString();
			if(end_time.length()<=10){
				end_time+=" 23:59:59";
			}
			sql +=" and log.OPERATE_TIME <=:endTime";
			try {
				conds.put("endTime", sf.parse(end_time));
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
		}
//		if(conds.get("operate_result")!=null && !"".equals(conds.get("operate_result"))){
//			sql +=" and log.OPERATE_RESULT='"+conds.get("operate_result")+"'";
//		}
		if(conds.get("operate_data")!=null && !"".equals(conds.get("operate_data"))){
			String [] dataS = conds.get("operate_data").toString().split(",");
			for(String data:dataS){
				sql +=" and log.OPERATE_DATA like '%"+data+"%' ";
			}
		}
		if(conds.get("operate_func")!=null && !"".equals(conds.get("operate_func"))){
			sql +=" and log.FUNC_ID='"+conds.get("operate_func")+"'";
		}
		
		Order order = new Order().desc("OPERATE_TIME");
		return cmcSimpleDao.pageQuery(sql, conds, order);
	}
	/**
	 * 获取日志对象
	 * @param logId 日志ID
	 * @return 日志信息（Map<列名称, 列值>）
	 */
	public Map<String ,Object> getLog(String logId){
		String sql  = "select log.LOG_ID,oper.REAL_NAME,to_char(log.OPERATE_TIME,'yyyy-MM-dd hh24:mi:ss') as OPERATE_TIME,fun.FUNC_NAME,log.OPERATE_DATA,log.OPERATE_RESULT,log.OPERATE_DATA,log.LOG_MAC";
		sql+=" from "+DBConstant.CMC_OPERATE_LOG+" log left join "+DBConstant.CMC_OPERATOR+" oper on log.OPERATOR_ID=oper.OPERATOR_ID left join "+DBConstant.CMC_FUNC+" fun ";
		sql+=" on log.FUNC_ID=fun.FUNC_ID where  log.LOG_ID='"+logId+"'";
		List<Map<String ,Object>> loglist = cmcSimpleDao.queryForList(sql);
		return loglist.get(0); 
	}
	/**
	 * 记录日志
	 * @param cl 日志信息（Map<列名称, 列值>）
	 * @return 日志信息（Map<列名称, 列值>）
	 */
	public Map<String, Object> createLog(Map<String, Object> cl) {
		//如果已经生成操作日志，则使用现有的
		if(cl.get(DBConstant.CMC_OPERATE_LOG_LOG_ID)==null){
			cl.put(DBConstant.CMC_OPERATE_LOG_LOG_ID, Stringz.randomUUID().toUpperCase());
		}
		cmcSimpleDao.create(DBConstant.CMC_OPERATE_LOG, cl);
		return cl;
	}
}
