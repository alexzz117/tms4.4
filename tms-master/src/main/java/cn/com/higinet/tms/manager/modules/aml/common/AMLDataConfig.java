package cn.com.higinet.tms.manager.modules.aml.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;
import cn.com.higinet.tms.manager.modules.common.util.XMLUtil;

/**
 * 
 * @author wuc
 * @date 2015-1-29
 * 
 */
public class AMLDataConfig {

	private Map<String, Object> amlConfigMap = null;

	private static AMLDataConfig instance = null;

	public static AMLDataConfig getInstance() {
		if (instance == null) {
			synchronized (AMLDataConfig.class) {
				if (instance == null) {
					instance = new AMLDataConfig();
				}
			}
		}
		return instance;
	}

	private AMLDataConfig() {
		String path = "/resources/config/amlConfig.xml";
		InputStream in = AMLDataConfig.class.getResourceAsStream(path);
		if (in != null) {
			amlConfigMap = new HashMap<String, Object>();
			try {
				Document doc = XMLUtil.getDocumentByInputStream(in);
				parseXML(doc, amlConfigMap);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("解析amlConfig.xml失败！");
			}
		} else {
			throw new RuntimeException("未找到amlConfig.xml配置文件！");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseXML(Document doc, Map<String, Object> amlConfigMap) {
		Element root = doc.getRootElement();
		List<Element> nodes = root.elements(AMLConstant.NODE);
		for (int i = 0, len = nodes.size(); i < len; i++) {
			Element tEle = nodes.get(i);
			Map<String, Object> tNode = (Map<String, Object>) amlConfigMap.get(tEle.attributeValue(AMLConstant.NAME));
			if (MapUtil.isEmpty(tNode)) {
				tNode = new LinkedHashMap<String, Object>();
				amlConfigMap.put(tEle.attributeValue(AMLConstant.NAME), tNode);
			}
			List<Element> tgroups = tEle.elements(AMLConstant.GROUP);
			for (int j = 0, jlen = tgroups.size(); j < jlen; j++) {
				Element tgroup = tgroups.get(j);
				Map<String, Object> group = (Map<String, Object>) tNode.get(tgroup.attributeValue(AMLConstant.NAME));
				if (MapUtil.isEmpty(group)) {
					group = new LinkedHashMap<String, Object>();
					tNode.put(tgroup.attributeValue(AMLConstant.NAME), group);
				}
				List<Element> entities = tgroup.elements(AMLConstant.ENTITY);
				for (int k = 0, klen = entities.size(); k < klen; k++) {
					Element entity = entities.get(k);
					if (AMLConstant.COMMON.equals(tEle.attributeValue(AMLConstant.NAME))) {
						String groupName = tgroup.attributeValue(AMLConstant.NAME);
						if (groupName.equals(AMLConstant.QUERY)) {
							String ds = entity.attributeValue(AMLConstant.DS);
							Map<String, Object> sqlMap = new HashMap<String, Object>();
							sqlMap.put(AMLConstant.DS, ds);
							sqlMap.put(AMLConstant.SQL, entity.getText());
							group.put(entity.attributeValue(AMLConstant.NAME), sqlMap);
						} else {
							group.put(entity.attributeValue(AMLConstant.NAME), entity.getText());
						}
					} else if (AMLConstant.MESSAGE.equals(tEle.attributeValue(AMLConstant.NAME))) {
						Map<String, String> tmap = new HashMap<String, String>();
						tmap.put(AMLConstant.TYPE, entity.attributeValue(AMLConstant.TYPE));
						tmap.put(AMLConstant.SQL, entity.attributeValue(AMLConstant.SQL));
						tmap.put(AMLConstant.DEFAULT, entity.attributeValue(AMLConstant.DEFAULT));
						tmap.put(AMLConstant.OBJ, entity.attributeValue(AMLConstant.OBJ));
						tmap.put(AMLConstant.INDEX, entity.attributeValue(AMLConstant.INDEX));
						tmap.put(AMLConstant.VALUE, entity.getText());
						group.put(entity.attributeValue(AMLConstant.NAME), tmap);
					}
				}
			}
			List<Element> tEntities = tEle.elements(AMLConstant.VALUE);
			for (int j = 0, jlen = tEntities.size(); j < jlen; j++) {
				Element tEntity = tEntities.get(j);
				if (AMLConstant.TRANSDATA.equals(tEntity.getText())) {
					// 交易流水数据标签
					tNode.put(AMLConstant.TRANSDATA, tEntity.attributeValue(AMLConstant.NAME));
				} else if (AMLConstant.TXNCT.equals(tEntity.attributeValue(AMLConstant.TYPE))) {
					// 可疑交易主体设置
					Map<String, Object> ctMap = (Map<String, Object>) tNode.get(AMLConstant.TXNCT);
					if (MapUtil.isEmpty(ctMap)) {
						ctMap = new HashMap<String, Object>();
					}
					ctMap.put(AMLConstant.TYPE, tEntity.attributeValue(AMLConstant.TYPE).toLowerCase());// 主体类型
					String[] pks = tEntity.getText().split("\\,");
					for (int k = 0, klen = pks.length; k < klen; k++) {
						String pk = pks[k];
						if (!pk.equals(pk.trim())) {
							 pk = pk.trim();
						}
						pks[k] = pk.toUpperCase();
					}
					ctMap.put(AMLConstant.VALUE, pks);// 主体主键, 支持联合主键
					tNode.put(AMLConstant.TXNCT, ctMap);
				} else {
					tNode.put(tEntity.attributeValue(AMLConstant.NAME), tEntity.getText());
				}
			}
		}
	}

	public Map<String, Object> getAMLConfigMap() {
		return amlConfigMap;
	}
	
	public Map<String, Object> getAMLConfigMap(Map<String, Object> amlConfigMap, String xml) {
		Map<String, Object> cfgMap = new HashMap<String, Object>(amlConfigMap == null ? this.amlConfigMap : amlConfigMap);
		if (!StringUtil.isBlank(xml)) {
			try {
				Document doc = XMLUtil.getDocumentByString(xml);
				parseXML(doc, cfgMap);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("解析xml失败！");
			}
		}
		return cfgMap;
	}
	
	public Map<String, Object> getAMLConfigMap(String xml) {
		return getAMLConfigMap(this.amlConfigMap, xml);
	}
	
	public String map2Xml(Map<String, Object> amlConfigMap) {
		return null;
	}

	public static void main(String[] args) {
		AMLDataConfig aml = AMLDataConfig.getInstance();
		System.out.println(aml.getAMLConfigMap());
		System.out.println(aml.getAMLConfigMap("<?xml version=\"1.0\" encoding=\"UTF-8\"?><config><node name=\"common\"><value name=\"emptyValue\">@A</value></node></config>"));
	}
}