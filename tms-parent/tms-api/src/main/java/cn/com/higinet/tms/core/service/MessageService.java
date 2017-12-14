/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.core.common.SocketClient;
import cn.com.higinet.tms.core.model.OperatCycle;
import cn.com.higinet.tms.core.model.RiskResult;
/**
 * 
 * @author zhangfg
 * @version 2.2.0
 * @date 2011-10-21
 * @description 公用的消息服务接口
 */
public interface MessageService {
	/**
	 * 获取返回信息
	 * @param backCode
	 * @return
	 */
	public String getBackInfo(String backCode);
	/**
	 * 发送报文并获取返回值
	 * @param model
	 * @param runId
	 * @param head
	 * @param body
	 * @param timeOut
	 * @param s
	 * @return
	 */
	public String sendMessage(OperatCycle model, String runId, String head, String body, int timeOut, SocketClient client);
	/**
	 * 解析报文体,返回风险信息Risk对象
	 * @param bodyStr
	 * @param actionCode
	 * @return
	 */
	public void deCodeBodyXml(RiskResult risk,String bodyStr,String actionCode);
	/**
	 * 拼装报文体部分xml的头信息
	 * @return
	 */
	public String getXmlHead();
	/**
	 * 拼装filter报文体部分xml的头信息
	 * @return
	 */
	public String getHttpXmlHead();
	/**
	 * 将map中的信息拼装成xml格式，key作为节点，value作为值
	 * @param map
	 * @return
	 */
	public String getXmlExtMap(Map<String,Object> map);
	/**
	 * 判断list是否为空，为空返回false，不为空返回true
	 * @param list
	 * @return
	 */
	public boolean listIsNull(List list);
	
	public String composeBackHead(String actionCode, String backCode, int bodyLength);
}