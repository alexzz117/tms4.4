package cn.com.higinet.tms35.manage.alarm.service;

import java.util.List;
import java.util.Map;

public interface SendMessageService {
	public String sendMessage(Map<String,Object> transaction, String actionCode, List<Map<String,Object>> servList);
}