package cn.com.higinet.tms35.manage.aml.common;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrServiceException;

/**
 * 
 * @author wuc
 * @date 2015-2-3
 * 
 */
public class AMLMessage {

	private static Log log = LogFactory.getLog(AMLMessage.class);
	
	private Map<String, Object> PSTR;
	
	/**
	 * 公共信息
	 */
	private Map<String, Object> RBIF;
	
	/**
	 * 可疑主体列表
	 */
	private List<Map<String, Object>> CTIFs;
	
	/**
	 * 可疑交易列表
	 */
	private List<Map<String, Object>> STIFs;

	public AMLMessage() {
		PSTR = new LinkedHashMap<String, Object>();
		RBIF = new LinkedHashMap<String, Object>();
		CTIFs = new ArrayList<Map<String,Object>>();
		STIFs = new ArrayList<Map<String,Object>>();
		PSTR.put(AMLConstant.RBIF, RBIF);
		PSTR.put(AMLConstant.CTIFs, CTIFs);
		PSTR.put(AMLConstant.STIFs, STIFs);
	}
	
	public AMLMessage(Map<String, Object> RBIF, List<Map<String, Object>> CTIFs, 
			List<Map<String, Object>> STIFs) {
		this.RBIF = RBIF;
		this.CTIFs = CTIFs;
		this.STIFs = STIFs;
		
		PSTR = new LinkedHashMap<String, Object>();
		PSTR.put(AMLConstant.RBIF, RBIF);
		PSTR.put(AMLConstant.CTIFs, CTIFs);
		PSTR.put(AMLConstant.STIFs, STIFs);
	}

	public Map<String, Object> getPSTR() {
		return PSTR;
	}

	public void setPSTR(Map<String, Object> PSTR) {
		this.PSTR = PSTR;
	}

	public Map<String, Object> getRBIF() {
		return RBIF;
	}

	public void setRBIF(Map<String, Object> RBIF) {
		this.RBIF = RBIF;
	}

	public List<Map<String, Object>> getCTIFs() {
		return CTIFs;
	}

	public void setCTIFs(List<Map<String, Object>> CTIFs) {
		this.CTIFs = CTIFs;
	}

	public List<Map<String, Object>> getSTIFs() {
		return STIFs;
	}

	public void setSTIFs(List<Map<String, Object>> STIFs) {
		this.STIFs = STIFs;
	}

	public String toXmlString() {
		Map<String, Object> rootMap = getPSTR();
		Document doc = DocumentHelper.createDocument();
		Element root = doc.addElement(AMLConstant.ROOT_NODE);
		object2Xml(rootMap, root);
		return doc2String(doc);
	}
	
	public static AMLMessage parseXmlString(String xmlString) {
		try {
			Document doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			Map<String, Object> rootMap = (Map<String, Object>) element2Object(root);
			AMLMessage amlMesaage = new AMLMessage();
			amlMesaage.setPSTR(rootMap);
			amlMesaage.setRBIF(MapUtil.getMap(rootMap, AMLConstant.RBIF));
			amlMesaage.setCTIFs(MapUtil.getList(rootMap, AMLConstant.CTIFs));
			amlMesaage.setSTIFs(MapUtil.getList(rootMap, AMLConstant.STIFs));
			return amlMesaage;
		} catch (Exception e) {
			log.error("parseXmlString error, caused by ", e);
		}
		return null;
	}
	
	private static Object element2Object(Element element) {
		try {
			String eleName = element.getName();
			if (eleName.endsWith("s") && element.element(eleName.substring(0, eleName.length() - 1)) != null &&
					!StringUtil.isBlank(element.element(eleName.substring(0, eleName.length() - 1)).attributeValue("seqno"))) {
				return xml2List(element.asXML());
			}
			return xml2Map(element.asXML());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Map xml2Map(String xml) {
		try {
			Map map = new LinkedHashMap();
			Document doc = DocumentHelper.parseText(xml);
			Element root = doc.getRootElement();
			List node = root.elements();
			for (Iterator it = node.iterator(); it.hasNext();) {
				Element ele = (Element) it.next();
				if (!ele.isTextOnly()) {
					map.put(ele.getName(), element2Object(ele));
				} else {
					map.put(ele.getName(), ele.getText());
				}
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List xml2List(String xml) {
		try {
			List<Map> list = new ArrayList<Map>();
			Document document = DocumentHelper.parseText(xml);
			Element nodesElement = document.getRootElement();
			List nodes = nodesElement.elements();
			for (Iterator its = nodes.iterator(); its.hasNext();) {
				Element nodeElement = (Element) its.next();
				Map map = xml2Map(nodeElement.asXML());
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Element object2Xml(Object object, Element element) {
		try {
			if (object instanceof Map) {
				return map2Xml((Map) object, element);
			} else if (object instanceof List) {
				return list2Xml((List) object, element);
			} else {
				throw new TmsMgrServiceException("object2Xml error, only apply for datatype 'map' and 'list', actual type:" + object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Element map2Xml(Map map, Element element) {
		for (Object obj : map.keySet()) {
			Object o = map.get(obj);
			if (o instanceof String || o instanceof Integer) {
				Element keyElement = element.addElement(String.valueOf(obj));
				keyElement.setText(String.valueOf(map.get(obj)));
			} else {
				Element keyElement = element.addElement(String.valueOf(obj));
				object2Xml(map.get(obj), keyElement);
			}
		}
		return element;
	}
	
	private static Element list2Xml(List list, Element element) {
		int i = 0;
		for (Object o : list) {
			String nodeName = element.getName();
			i++;
			Element nodeElement = element.addElement(nodeName.substring(0, nodeName.length() - 1));
			nodeElement.addAttribute(AMLConstant.SEQNO, String.valueOf(i));
			if (o instanceof String || o instanceof Integer) {
				Element keyElement = nodeElement.addElement(String.valueOf(i));
				keyElement.setText(String.valueOf(o));
			} else {
				object2Xml(o, nodeElement);
			}
		}
		return element;
	}
	
	private static String doc2String(Document document) {
		String s = "";
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 使用UTF-8编码
			OutputFormat format = new OutputFormat("    ", true, "UTF-8");
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(document);
			s = out.toString("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static void main(String[] args) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><PSTR>    <RBIF>        <RINM>国付宝科技信息有限公司</RINM>        <FIRC>110113</FIRC>        <FICE>Z2019446000011</FICE>        <RFSG>1</RFSG>        <ORXN>NPSZ2019446000011-20140910-2467-2466</ORXN>        <SSTM>01</SSTM>        <STCR>01</STCR>        <SSDS>01</SSDS>        <UDSI>2466</UDSI>        <SCTN>1</SCTN>        <TTNM>2</TTNM>    </RBIF>    <CTIFs>        <CTIF seqno=\"1\">            <CTNM>福建省聚散网络科技有限公司</CTNM>            <SMID>@I</SMID>            <CITP>@I</CITP>            <CTID>@I</CTID>            <CCIF>                <CTAR>厦门市湖里区禾山路79号</CTAR>                <CCTL>213-21321321</CCTL>                <CEML>youyicicivip@163.com</CEML>            </CCIF>            <CTVC>@I</CTVC>            <CRNM>@I</CRNM>            <CRIT>@I</CRIT>            <CRID>@I</CRID>        </CTIF>    </CTIFs>    <STIFs>        <STIF seqno=\"1\">            <CTNM>福建省聚散网络科技有限公司</CTNM>            <CITP>null</CITP>            <CTID>@I</CTID>            <CBAT>03</CBAT>            <CBAC>@I</CBAC>            <CABM>@I</CABM>            <CTAT>02</CTAT>            <CTAC>@I</CTAC>            <CPIN>国付宝科技有限公司</CPIN>            <CPBA>@I</CPBA>            <CPBN>@I</CPBN>            <CTIP>218.65.64.227</CTIP>            <TSTM>20140910000008</TSTM>            <CTTP>@I</CTTP>            <TSDR>收</TSDR>            <CRPP>@I</CRPP>            <CRTP>156</CRTP>            <CRAT>10.00</CRAT>            <TCNM>null</TCNM>            <TSMI>null</TSMI>            <TCIT>null</TCIT>            <TCID>null</TCID>            <TCAT>null</TCAT>            <TCBA>null</TCBA>            <TCBN>中国建设银行</TCBN>            <TCTT>03</TCTT>            <TCTA>null</TCTA>            <TCPN>null</TCPN>            <TCPA>@I</TCPA>            <TPBN>null</TPBN>            <TCIP>218.65.64.227</TCIP>            <TMNM>@I</TMNM>            <BPTC>2014091046205278</BPTC>            <PMTC>512041140909235930</PMTC>            <TICD>@I</TICD>        </STIF>        <STIF seqno=\"2\">            <CTNM>福建省聚散网络科技有限公司</CTNM>            <CITP>null</CITP>            <CTID>@I</CTID>            <CBAT>03</CBAT>            <CBAC>@I</CBAC>            <CABM>@I</CABM>            <CTAT>02</CTAT>            <CTAC>@I</CTAC>            <CPIN>国付宝科技有限公司</CPIN>            <CPBA>@I</CPBA>            <CPBN>@I</CPBN>            <CTIP>114.252.69.65</CTIP>            <TSTM>20140910000043</TSTM>            <CTTP>@I</CTTP>            <TSDR>收</TSDR>            <CRPP>@I</CRPP>            <CRTP>156</CRTP><CRAT>2000.00</CRAT><TCNM>null</TCNM><TSMI>null</TSMI><TCIT>null</TCIT><TCID>null</TCID><TCAT>null</TCAT><TCBA>null</TCBA><TCBN>广发银行</TCBN><TCTT>03</TCTT><TCTA>null</TCTA><TCPN>null</TCPN><TCPA>@I</TCPA><TPBN>null</TPBN><TCIP>114.252.69.65</TCIP><TMNM>@I</TMNM><BPTC>UM14091046205311</BPTC><PMTC>20140910000030-5637</PMTC><TICD>@I</TICD></STIF></STIFs></PSTR>";
		AMLMessage aml = parseXmlString(xml);
		System.out.println(aml.getPSTR());
		System.out.println(aml.toXmlString());
	}
}
