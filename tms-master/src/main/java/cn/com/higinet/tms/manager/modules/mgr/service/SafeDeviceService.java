package cn.com.higinet.tms.manager.modules.mgr.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.Constant;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.util.MapWrap;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

@Service("safeDeviceService")
public class SafeDeviceService  {

	@Autowired
	private SimpleDao tmsSimpleDao;

	public Page<Map<String, Object>> getSafeDeviceList(Map<String, String> reqs) {
		StringBuilder sql = new StringBuilder();
		sql.append("select s.device_id,s.descr,s.status,s.created_date,s.created_by,s.updated_date,s.updated_by,d.user_id,u.userid,u.cusname,u.risk_level from TMS_MGR_SAFE_DEVICE s  left join TMS_DFP_USER_DEVICE d on s.device_id =d.device_id left join TMS_RUN_USER u on d.user_id=u.userid");
		if(!StringUtil.isEmpty(reqs.get("DEVICE_ID")))
			sql.append(" and s.DEVICE_ID like '%'||:DEVICE_ID||'%'");
		if(!StringUtil.isEmpty(reqs.get("USERID")))
			sql.append(" and u.USERID like '%'||:USERID||'%'");
		if(!StringUtil.isEmpty(reqs.get("startTime"))){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startTime =null;
			try {
				startTime=sdf.parse(reqs.get("startTime"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reqs.put("startTime", String.valueOf(startTime.getTime()));
			sql.append(" and s.created_date>=:startTime ");
		}
		if(!StringUtil.isEmpty(reqs.get("endTime"))){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date endTime=null;
			try {
				endTime=sdf.parse(reqs.get("endTime"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reqs.put("endTime", String.valueOf(endTime.getTime()));
			sql.append(" and s.created_date<= :endTime");
		}
		Page<Map<String, Object>> SafeDevicePage = tmsSimpleDao.pageQuery(sql.toString().replaceFirst("and", "where"),reqs, new Order().desc("UPDATED_DATE"));
		return SafeDevicePage;
	}

	@SuppressWarnings("unchecked")
	public void addSafeDeviceAction(Map<String, String> reqs,HttpServletRequest request) {
		StringBuilder sql = new StringBuilder();
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute(Constant.SESSION_KEY_OPERATOR);
		reqs.put("OPERATOR_ID", (String) operator.get("OPERATOR_ID"));
		reqs.put("CREATED_DATE",  reqs.get("t"));
		reqs.put("STATUS",  "1");
		sql.append("insert into TMS_MGR_SAFE_DEVICE(DEVICE_ID,STATUS,DESCR,CREATED_DATE,CREATED_BY,UPDATED_DATE,UPDATED_BY) values(:DEVICE_ID,:STATUS,:DESCR,:CREATED_DATE,:OPERATOR_ID,:CREATED_DATE,:OPERATOR_ID)");
		tmsSimpleDao.executeUpdate(sql.toString(), reqs);
	}

	public Map<String, Object> getTableList(String tableName,String cond,String value) {
		Map<String, Object> List = tmsSimpleDao.retrieve(tableName, MapWrap.map(cond, value).getMap());
		return List;
	}
	
	@SuppressWarnings("unchecked")
	public void updateSafeDeviceByUuid(Map<String, String> reqs,HttpServletRequest request) {
		StringBuffer sql=new StringBuffer();
		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute(Constant.SESSION_KEY_OPERATOR);
		reqs.put("OPERATOR_ID", (String) operator.get("OPERATOR_ID"));
		reqs.put("UPDATED_DATE",  reqs.get("t"));
		sql.append("update TMS_MGR_SAFE_DEVICE s set s.STATUS=:STATUS,s.DESCR=:DESCR,s.UPDATED_DATE=:UPDATED_DATE,UPDATED_BY=:OPERATOR_ID where s.DEVICE_ID=:DEVICE_ID");
		tmsSimpleDao.executeUpdate(sql.toString(), reqs);	
	}

		
}
