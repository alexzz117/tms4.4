package cn.com.higinet.tms.manager.modules.auth.common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cn.com.higinet.tms.manager.modules.aop.cache.common.ParseCacheConfigXml;
import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.common.util.StringUtil;

/**
 * @author wangsch
 * @author zhang.lei
 */
public class ParseTableConfigXML {
	private Map<String,TableConfig> tableConfigMap=null;
	
	private static ParseTableConfigXML instance=null;

	public static ParseTableConfigXML getInstance(){
		if(instance==null){
			instance = new ParseTableConfigXML();
		}
		return instance;
	}
	
	private ParseTableConfigXML(){
		String path = PropertiesUtil.getPropInstance().get("FIELDNAMECONFIGPATH");
		InputStream in = ParseCacheConfigXml.class.getResourceAsStream(path);
		
		if(in!=null){
			tableConfigMap = new HashMap<String, TableConfig>();
			DocumentBuilder db;
			Document doc;
			try {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				doc = db.parse(in);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("解析tableconfig.xml失败！");
			}
			
			Element root = doc.getDocumentElement();
	        NodeList tableTags = root.getElementsByTagName("table");
	        for(int i=0;i<tableTags.getLength();i++){
	        	TableConfig tc = new TableConfig();
	        	List<Map<String, String>> fields = new ArrayList<Map<String,String>>();
	        	
	        	Element tEle = (Element)tableTags.item(i);
	        	String tid = tEle.getAttribute("id");
	        	tc.setId(tid);
	        	NodeList fieldTags = tEle.getElementsByTagName("field");
	        	for(int j=0; j<fieldTags.getLength(); j++){
	        		Element fieldEle = (Element)fieldTags.item(j);
	        		Map<String, String> field = new HashMap<String, String>();
	        		
	        		String id = fieldEle.getAttribute("id");
	        		Assert.hasText(id,"tableconfig.xml失败，字段名（id）不能为空");
	        		field.put("id", id);
	        		
	        		String name = fieldEle.getAttribute("name");
	        		Assert.hasText(id,"tableconfig.xml失败，字段显示名（name）不能为空");
					field.put("name", name);
					
					String code = fieldEle.getAttribute("code");
					if(!StringUtil.isEmpty(code)) {
						field.put("code", code);
					}
	        		
	        		String type = fieldEle.getAttribute("type");
	        		if(!StringUtil.isEmpty(type)){
	        			field.put("type", type);
	        			
	        			if(type.equals("query")){
	        				String refTable = fieldEle.getAttribute("refTable");
	        				Assert.hasText(refTable,"tableconfig.xml失败，引用表名（refTable）不能为空");
	        				field.put("refTable", refTable);
	        				
	        				String refField = fieldEle.getAttribute("refField");
	        				Assert.hasText(refField,"tableconfig.xml失败，引用字段名（refField）不能为空");
	        				field.put("refField", refField);
	        				
	        				String refPk = fieldEle.getAttribute("refPk");
	        				Assert.hasText(refPk,"tableconfig.xml失败，引用主键值（refPk）不能为空");
	        				field.put("refPk", refPk);
	        			}
	        		}
	        		fields.add(field);
	        	}
	        	tc.setFields(fields );
	        	tableConfigMap.put(tid, tc);
	        }
		}else{
			throw new RuntimeException("未找到tableconfig.xml配置文件！");
		}
	}
	
	public TableConfig getTableConfig(String tableId){
		return tableConfigMap.get(tableId);
	}
}
