package cn.com.higinet.tms.core.service.impl;

import java.io.ByteArrayInputStream;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.service.MessageAbstractService;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.StringUtil;

public class SaxXml extends MessageAbstractService {
	private static Logger logger = LoggerFactory.getLogger(MessageFastXml.class);

	private static final SAXParserFactory sf = SAXParserFactory.newInstance();

	@Override
	public void deCodeBodyXml(final RiskResult risk, String bodyStr, String actionCode) {
		try {
			SAXParser sp = sf.newSAXParser();
			sp.parse(new ByteArrayInputStream(bodyStr.getBytes()), new DefaultHandler() {
				private String nodeName = null;

				private String nodeValue = null;

				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					nodeName = qName;
				}

				public void characters(char ch[], int start, int length) throws SAXException {
					nodeValue = new String(ch, start, length);
					if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TXNID)) {
						risk.setTxnId(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TXNNAME)) {
						risk.setTxnName(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_ERRORINFO)) {
						risk.setErrorInfo(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_DISPOSAL)) {
						risk.setDisposal(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TRANSCODE)) {
						risk.setTransCode(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_SCORE)) {
						float score = 0.0f;
						if (!StringUtil.isBlank(nodeValue))
							score = Float.parseFloat(nodeValue);
						risk.setScore(score);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_RISKID)) {
						risk.setRiskId(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_HIT_RULE_NUM)) {
						int hitRuleNum = 0;
						if (!StringUtil.isBlank(nodeValue))
							hitRuleNum = Integer.parseInt(nodeValue);
						risk.setHitRuleNum(hitRuleNum);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_TRIG_RULE_NUM)) {
						int trigRuleNum = 0;
						if (!StringUtil.isBlank(nodeValue))
							trigRuleNum = Integer.parseInt(nodeValue);
						risk.setTrigRuleNum(trigRuleNum);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_PROCESS_INFO)) {
						risk.setProcessInfo(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_SWITCH_INFO)) {
						risk.setSwitchInfo(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_ACTION_INFO)) {
						risk.setActionInfo(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_HIT_RULES)) {
						risk.setHitRules(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_DEVTOKEN)) {
						risk.setDeviceToken(nodeValue);
					} else if (nodeName.equalsIgnoreCase(StaticParameter.NODE_NAME_COOKIENAME)) {
						risk.setCookieName(nodeValue);
					}
				}
			});
		} catch (Exception e) {
			logger.info(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "  The Methode 'deCodeBodyXml' for the class SAXXml error." + e.getMessage());
		}
	}

}
