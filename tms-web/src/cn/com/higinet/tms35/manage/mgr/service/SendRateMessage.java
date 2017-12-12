package cn.com.higinet.tms35.manage.mgr.service;

import java.util.Map;



public interface SendRateMessage {
	public void userRate(Map<String,Object> transaction,String actionCode,Map<Integer,Map<String,Object>> ser_map,String signal);
	
	public int getCount();

}
