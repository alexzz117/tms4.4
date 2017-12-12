package cn.com.higinet.tms35.manage.dataanalysic.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.dataanalysic.service.DateValueService;

@Service("dateValueService")
public class DateValueServiceIml implements DateValueService{
	@Autowired
	private SimpleDao tmsSimpleDao;

	
	public List<Map<String,Object>> queryDateAnnaList(String userId,String deviceId,String thingType,String transBeginTime,String transEndTime,HttpServletRequest request){

		////thingType： 交易事件01，报警事件02
		if(thingType.equals("01")){
			
			List<Map<String,Object>>  trafficDataList = this.getTrafficData(userId,deviceId,thingType,transBeginTime,transEndTime);
		    return trafficDataList;
		
		}
		//报警事件02
		if(thingType.equals("02")){
			
			List<Map<String,Object>>  trafficDataList = this.getTrafficData(userId,deviceId,thingType,transBeginTime,transEndTime);
		    return trafficDataList;
		}
		return null;
		
	}
	
	private List<Map<String,Object>> getTrafficData(String userId,String deviceId,String thingType,String transBeginTime,String transEndTime){
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date starTimeDb = null;
		Date endTimeDb = null;
		if(!StringUtil.isEmpty(transBeginTime)){	
			try {
				starTimeDb = sdf.parse(transBeginTime);
				transBeginTime = String.valueOf(starTimeDb.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if(!StringUtil.isEmpty(transEndTime)){
			//把时间转成毫秒
			try {
				endTimeDb = sdf.parse(transEndTime);
				transEndTime = String.valueOf(endTimeDb.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		 //sb.append("select * from tms_run_trafficdata where txntime between "+"'"+transBeginTime+"'"+" and "+"'"+transEndTime+"'"+" and userid= "+"'"+ userId +"'"+" and disposal = 'PS01'");
		sb.append("select a.txncode,a.txnid,a.userid,a.txntime,b.txnid,b.tab_desc from tms_run_trafficdata a,tms_com_tab b");
		sb.append(" where b.txnid=a.txnid"); 
		if(userId!=null&&!"".equals(userId)){
			sb.append(" and a.userid="+"'"+userId+"'" );
		}
		if(transEndTime!=null&&!"".equals(transEndTime)){
			sb.append(" and a.txntime <="+"'"+transEndTime+"'" );
		}
		if(transBeginTime!=null&&!"".equals(transBeginTime)){
			sb.append(" and a.txntime >="+"'"+transBeginTime+"'" );
		}
		if(deviceId!=null&&!"".equals(deviceId)){
			sb.append(" and a.deviceid ="+"'"+deviceId+"'" );
		}
		if("02".equals(thingType)){
			sb.append(" and disposal != 'PS01' " );
		}else{
			sb.append(" and disposal = 'PS01' " );
		}
		List<Map<String,Object>> list = tmsSimpleDao.queryForList(sb.toString());
		return list;
	}
	
	

}	
