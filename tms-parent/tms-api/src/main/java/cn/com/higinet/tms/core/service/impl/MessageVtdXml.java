/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.service.MessageAbstractService;
import cn.com.higinet.tms.core.util.StringUtil;

import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
/**
 * 
 * @author zhangfg
 * @version 2.2.0
 * @date 2011-10-21
 * @description 对TMS服务返回的报文进行解析的服务类，采用VTD方式
 */
public class MessageVtdXml extends MessageAbstractService {

	// static Logger logger = LogManager.getLogger(MessageVtdXml.class);
	private static Logger logger = LoggerFactory.getLogger(MessageVtdXml.class);

	private static final String TYPE = StaticParameter.ELEMENT_ATTRBUTE_TYPE;

	public static final String TYPE_MAP = "map";
	public static final String TYPE_LIST = "list";
	public static final String VALUE = "value";
	
	/**
	 * 解析报文体,返回风险信息Risk对象
	 * @param bodyStr
	 * @param actionCode  操作代码，目前没有用到
	 * @return
	 */
	public void deCodeBodyXml(RiskResult risk, String bodyStr, String actionCode) {
		//logger.setLevel(Level.INFO);
		VTDGen vg = new VTDGen();     
        try {
            vg.setDoc(bodyStr.getBytes("UTF-8")); 
			vg.parse(false);
		} catch (Exception e) {
			logger.info("The Methode 'deCodeBodyXml' for the class MessageVtdXml error."+e.getMessage());
			throw new RuntimeException(e);
		}   
        VTDNav vn = vg.getNav();   
        try {
			if (vn.matchElement(StaticParameter.ROOT_ELEMENT_NAME)) {
				if (vn.toElement(VTDNav.FIRST_CHILD)) {
					Map riskObj = null;

					riskObj = (Map) simpleVn2Map(vn);
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TXNID))))
						risk.setTxnId(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TXNID)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TXNNAME))))
						risk.setTxnName(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TXNNAME)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_ERRORINFO))))
						risk.setErrorInfo(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_ERRORINFO)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TRANSCODE))))
						risk.setTransCode(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TRANSCODE)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_DISPOSAL))))
						risk.setDisposal(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_DISPOSAL)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_SCORE))))
						risk.setScore(Float.parseFloat(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_SCORE))));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_RISKID))))
						risk.setRiskId(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_RISKID)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_HIT_RULE_NUM))))
						risk.setHitRuleNum(Integer.parseInt(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_HIT_RULE_NUM))));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TRIG_RULE_NUM))))
						risk.setTrigRuleNum(Integer.parseInt(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_TRIG_RULE_NUM))));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_PROCESS_INFO))))
						risk.setProcessInfo(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_PROCESS_INFO)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_SWITCH_INFO))))
						risk.setSwitchInfo(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_SWITCH_INFO)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_ACTION_INFO))))
						risk.setActionInfo(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_ACTION_INFO)));
					if (!StringUtil.isBlank(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_HIT_RULES))))
						risk.setHitRules(StringUtil.objToString(riskObj.get(StaticParameter.NODE_NAME_HIT_RULES)));
				}
			}
		} catch (NavException e) {
			logger.info("The Methode 'deCodeBodyXml' for the class MessageVtdXml error.", e);
		}   
	}
	
	/**
	 * 将单层的报文转换成Map对象
	 * @param vn
	 * @return
	 */
	private static Map simpleVn2Map(VTDNav vn){
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		try {
			do {
				map.put(vn.toString(vn.getCurrentIndex()), vn.toString(vn.getText()));
			} while (vn.toElement(VTDNav.NEXT_SIBLING));
		} catch (NavException e) {
			e.printStackTrace();
		}
			
		return map;
	}
	
	/**
	 * 节点转化为对象
	 * @param element
	 * @return
	 */
	private static Object vn2Object(VTDNav vn){
		try {
			if (vn.getAttrVal(TYPE) == -1) {
				return vn.toString(vn.getText());
			}
			String type =vn.toString(vn.getAttrVal(TYPE));
			int index = vn.getCurrentIndex();
			Object o = null;
			if (TYPE_MAP.equalsIgnoreCase(type)) {
				o = vn2Map(vn);
			} else if (TYPE_LIST.equalsIgnoreCase(type)) {
				o = vn2List(vn);
			}
			if(o != null){
				vn.recoverNode(index);
				return o;
			}
		} catch (NavException e) {
			logger.info("The Methode 'vn2Object' for the class MessageVtdXml error."+e.getMessage());
			throw new RuntimeException(e);
		}
		throw new RuntimeException("can not convert vn: " + vn);
	}

	/**
	 * 节点转化为ArrayList对象
	 * @param element
	 * @return
	 */
	private static List<Object> vn2List(VTDNav vn) {
		ArrayList<Object> list = new ArrayList<Object>();
		try {
			if (vn.toElement(VTDNav.FIRST_CHILD)){   
			    do {   
			       	list.add(vn2Object(vn));
			    } while(vn.toElement(VTDNav.NEXT_SIBLING));   
			}
		} catch (NavException e) {
			logger.info("The Methode 'vn2List' for the class MessageVtdXml error."+e.getMessage());
			throw new RuntimeException(e);
		} 
		return list;
	}
	

	/**
	 * 节点转化为LinkedHashMap对象
	 * @param element
	 * @return
	 */
	private static Map<String, Object> vn2Map(VTDNav vn) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			if (vn.toElement(VTDNav.FIRST_CHILD)){   
			    do {   
			      map.put(vn.toString(vn.getCurrentIndex()), vn2Object(vn));
			    } while(vn.toElement(VTDNav.NEXT_SIBLING));   
			}
		} catch (NavException e) {
			logger.info("The Methode 'vn2Map' for the class MessageVtdXml error."+e.getMessage());
			throw new RuntimeException(e);
		}
		return map;
	}
	

//	public static void main(String [] args){
//		
//		MessageVtdXml mvx = new MessageVtdXml();
//		String bodyStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> ";
//		bodyStr +="<Message>";
//		bodyStr +="<txn type='map'>";
//		bodyStr +="<riskLevelCode>mid</riskLevelCode> ";
//		bodyStr +="<riskDetail type='map'>";
//		bodyStr +="<UserIdType>33</UserIdType> ";
//		bodyStr +="</riskDetail>";
//		bodyStr +="<riskScore>33</riskScore> ";
//		bodyStr +="<riskLevelName>中</riskLevelName>";
//		bodyStr +="<txnId>3a819d0d822249e28574a54c4ccad1a2</txnId> ";
//		bodyStr +="</txn>";
//		bodyStr +="</Message>";
//		String bodyStr1 = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> " +
//				"<Message><expCode>111</expCode><backInfo>222</backInfo><transCode>333</transCode></Message>";
//		mvx.deCodeBodyXml(null,bodyStr1,"");
//	}
	
}
