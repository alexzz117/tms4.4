/*
 * Copyright © 2012 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.open4j.nxml.XmlParser;
import org.open4j.xml.XmlNode;
import org.open4j.xml.XmlTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.service.MessageAbstractService;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;
/**
 * 
 * @author zhangfg
 * @version 3.0.0
 * @date 2012-06-12
 * @description 对TMS服务返回的报文进行解析的服务类，采用open4j方式
 */
public class MessageFastXml extends MessageAbstractService{

	// static Logger logger = LogManager.getLogger(MessageFastXml.class);
	
	private static Logger logger = LoggerFactory.getLogger(MessageFastXml.class);
	/**
	 * 解析报文体,返回风险信息Risk对象
	 * @param bodyStr
	 * @param actionCode  操作代码，目前没有用到
	 * @return
	 */
	public void deCodeBodyXml(RiskResult risk,String bodyStr,String actionCode){
		//logger.setLevel(Level.INFO);
		
		//交易识别标识
		String txnId = "";
		//交易名称
		String txnName = "";
		//错误信息
		String errorInfo = "";
		//交易码
		String transCode = "";
		//处置方式代码
		String disposal=  "";
		//分值
		float score = 0;
		//报警id
		String riskId = "";
		//规则命中数
		int hitRuleNum = 0;
		//规则触发数
		int trigRuleNum = 0;
		//处置信息
		String processInfo = "";
		//开关信息
		String switchInfo = "";
		//动作信息
		String actionInfo = "";
		//命中规则信息
		String hitRules = "";
		//设备标识
		String devToken = "";
		//cookie名称
		String cookieName = "";
		
		try{
			XmlTree tree = XmlParser.parse(bodyStr);
			XmlNode root = tree.getRoot();// 获得根节点   
//			List txnNodeList = root.getChildrenByTagName(StaticParameter.RISK_NODE_NAME);	
			List txnNodeList = root.getChildren();
			if(!listIsNull(txnNodeList)) return;//如果没有评估结果，返回null
			
			if(risk==null){
				risk = new RiskResult();
			}
			
			for(int i=0;i<txnNodeList.size();i++){
				XmlNode txnNode = (XmlNode)txnNodeList.get(i);	
				String nodeName = txnNode.getTagName();
				String nodeValue = null;
				if(txnNode.getChildren()!=null&&txnNode.getChildren().size()>0 &&txnNode.getChildren().get(0)!=null){
					nodeValue = (String)txnNode.getChildren().get(0);
				}
				
				if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TXNID)){
					txnId = nodeValue;
					risk.setTxnId(txnId);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TXNNAME)){
					txnName = nodeValue;
					risk.setTxnName(txnName);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_ERRORINFO)){
					errorInfo = nodeValue;
					risk.setErrorInfo(errorInfo);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_DISPOSAL)){
					disposal = nodeValue;
					risk.setDisposal(disposal);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TRANSCODE)){
					transCode = nodeValue;
					risk.setTransCode(transCode);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_SCORE)){
					if (!StringUtil.isBlank(nodeValue)) 
						score = Float.parseFloat(nodeValue);
					risk.setScore(score);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_RISKID)){
					riskId = nodeValue;
					risk.setRiskId(riskId);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_HIT_RULE_NUM)){
					if (!StringUtil.isBlank(nodeValue)) 
						hitRuleNum = Integer.parseInt(nodeValue);
					risk.setHitRuleNum(hitRuleNum);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TRIG_RULE_NUM)){
					if (!StringUtil.isBlank(nodeValue)) 
						trigRuleNum = Integer.parseInt(nodeValue);
					risk.setTrigRuleNum(trigRuleNum);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_PROCESS_INFO)){
					processInfo = nodeValue;
					risk.setProcessInfo(processInfo);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_SWITCH_INFO)){
					switchInfo = nodeValue;
					risk.setSwitchInfo(switchInfo);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_ACTION_INFO)){
					actionInfo = nodeValue;
					risk.setActionInfo(actionInfo);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_HIT_RULES)){
					hitRules = nodeValue;
					risk.setHitRules(hitRules);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_DEVTOKEN)){
					devToken = nodeValue;
					risk.setDeviceToken(devToken);
				}else if(nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_COOKIENAME)){
					cookieName = nodeValue;
					risk.setCookieName(cookieName);
				}
				
			}
		}catch(Exception e){
			logger.info(DateUtil.dateConvert(new Date(),"yyyy-MM-dd HH:mm:ss")+"  The Methode 'deCodeBodyXml' for the class MessageFastXml error."+e.getMessage());
		}
		
	}
	/**
	 * 获取type=map的节点的值
	 * @param MapNode
	 * @return
	 */
	public Map<String,Object> getNodeMap(XmlNode MapNode){
		Map<String,Object> riskDetail = new HashMap<String, Object>();
		
		try{
			List<Object> MapNodeList = MapNode.getChildren();
			if(!listIsNull(MapNodeList)) return riskDetail;
			
			for(int i=0;i<MapNodeList.size();i++){
				XmlNode txnChild = (XmlNode)MapNodeList.get(i);
				String txnChildName = txnChild.getTagName();
				String tagValue = "";
				List<Object> valuelist = txnChild.getChildren();
				if(!listIsNull(valuelist)) continue;//如果为空，继续下个循环
				
				if(valuelist!=null && valuelist.size()>0){
					tagValue = StringUtil.objToString(valuelist.get(0));
				}
				riskDetail.put(txnChildName, tagValue);
			}
		}catch(Exception e){
			logger.info("The Methode 'getNodeMap' for the class MessageFastXml error."+e.getMessage());
		}
		
		return riskDetail;
	}
	
	/**
	 * 获取type=list的节点的值,节点下只有key-value的map组成
	 * @param MapNode
	 * @return
	 */
	public List<Map<String,Object>> getNodeMapList(XmlNode MapNode){
		List<Map<String,Object>> list  = new ArrayList<Map<String,Object>>();
		try{
			List nodeList = MapNode.getChildren();
			if(!listIsNull(nodeList)) return list;
			
			for(int i=0;i<nodeList.size();i++){
				XmlNode txnChild = (XmlNode)nodeList.get(i);
				list.add(getNodeMap(txnChild));
			}
		}catch(Exception e){
			logger.info("The Methode 'getNodeList' for the class MessageFastXml error."+e.getMessage());
		}
		return list;
	}
	
	/**
	 * 获取type=list的节点的值,节点下只有多个String 类型的value组成
	 * @param MapNode
	 * @return
	 */
	public List<String> getNodeList(XmlNode MapNode){
		List<String> list  = new ArrayList<String>();
		try{
			List<XmlNode> nodeList = MapNode.getChildrenByTagName("value");
			if(!listIsNull(nodeList)) return list;
			
			for(int i=0;i<nodeList.size();i++){
				XmlNode txnChild = (XmlNode)nodeList.get(i);
				String tagValue = "";
				List<Object> valuelist = txnChild.getChildren();
				if(!listIsNull(valuelist)) continue;//如果为空，继续下个循环
				
				if(valuelist!=null && valuelist.size()>0){
					tagValue = StringUtil.objToString(valuelist.get(0));
				}
				list.add(tagValue);
			}
		}catch(Exception e){
			logger.info("The Methode 'getNodeList' for the class MessageFastXml error."+e.getMessage());
		}
		return list;
	}
	
}
