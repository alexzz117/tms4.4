package cn.com.higinet.tms35.manage.datamgr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.com.higinet.tms35.manage.common.PropertiesUtil;
import cn.com.higinet.tms35.manage.datamgr.common.ConfigAttrabute;
import cn.com.higinet.tms35.manage.datamgr.common.DataConfig;

/**
 * 解析数据同步配置的xml，authDataSync.xml
 * @author zhangfg
 */
public class ParseDataSyncConfigXml {
	private static ParseDataSyncConfigXml instance = null;
	private Map<String,Object> dataConfigs=null;
	
	public static ParseDataSyncConfigXml getInstance(){
		if(instance==null){
			instance = new ParseDataSyncConfigXml();
		}
		return instance;
	}
	
	public Map<String,Object> getDataConfigs(){
		return dataConfigs;
	}
	
	private ParseDataSyncConfigXml(){
		if(dataConfigs==null){
			dataConfigs = new HashMap<String,Object>();
		}
		try{
			String path = PropertiesUtil.getPropInstance().get("DATARULEPATH2");
			InputStream in = ParseDataSyncConfigXml.class.getResourceAsStream(path);
			
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(in);
	        //读取所有第一层元素
			Element root = doc.getDocumentElement();
	        //读取object业务数据描述的所有配置信息
			NodeList modelList = root.getElementsByTagName(ConfigAttrabute.TYPE_MODEL);
	        //循环每个第一级的元素
	        List dataRuleTags = getChildren(modelList);
	        
	        //循环处理数据对象的以名称ID做key，组装成Map结构
	        for (Iterator iter = dataRuleTags.iterator();iter.hasNext();){
	        	DataConfig dataConfig = (DataConfig)iter.next();
	        	dataConfigs.put(dataConfig.getId(),dataConfig);
	        }
	        
	    }catch(Exception e){
	        e.printStackTrace();
	        throw new RuntimeException("解析authDataSync.xml配置文件失败。");
	    }
	}
	
	
	public List getChildren(NodeList listTags){
		List resultList = new ArrayList<DataConfig>();
		//循环每一个xml元素
		for(int i=0;i<listTags.getLength();i++){
			Element model = (Element)listTags.item(i);
			
            //判断xml类型是filters,attachments,checks,references直接再次取得下面所有子元素并做为table对象的属性保存
			String nodeName = model.getNodeName();
            if (ConfigAttrabute.TYPE_FILTERS.equals(nodeName)||ConfigAttrabute.TYPE_REFERENCES.equals(nodeName)){
            	NodeList listChildren = null;
            	if(ConfigAttrabute.TYPE_FILTERS.equals(nodeName)){
            		listChildren = model.getElementsByTagName(ConfigAttrabute.TYPE_FILTER);
            	}else if(ConfigAttrabute.TYPE_REFERENCES.equals(nodeName)){
            		listChildren = model.getElementsByTagName(ConfigAttrabute.TYPE_REFERENCE);
            	}
            	
                //判断是否为空
                if (listChildren != null && listChildren.getLength()>0){
                    List tableProperties = getChildren(listChildren);
                    //添加当前元素类型，在后续返回增加到table对象属性时做为分类判断条件。
                    tableProperties.add(0,nodeName);
                    //统计添加到一个集合
                    resultList.add(tableProperties);
                }
                //如果filters,attachments,checks,references没必要执行下面程序
                continue;
            }
            
            DataConfig dataRule = new DataConfig();
            dataRule.setType(nodeName);//类型
            dataRule.setAttrabutes(attributeToMap(model.getAttributes()));//元素类型
            dataRule.setId(model.getAttribute(ConfigAttrabute.ID));//id
            dataRule.setName(model.getAttribute(ConfigAttrabute.NAME));//名称
            dataRule.setStatusKey(model.getAttribute(ConfigAttrabute.STATUSKEY));//表示状态的字段
            
            NodeList listChildren = null;
            NodeList listChildren2 = null;
            if(ConfigAttrabute.TYPE_MODEL.equals(dataRule.getType()) ||ConfigAttrabute.TYPE_REFERENCE.equals(dataRule.getType()) ){
        		listChildren = model.getElementsByTagName(ConfigAttrabute.TYPE_TABLE);
        	}else if(ConfigAttrabute.TYPE_TABLE.equals(dataRule.getType()) ){
        		listChildren = model.getElementsByTagName(ConfigAttrabute.TYPE_FILTERS);
        		listChildren2 = model.getElementsByTagName(ConfigAttrabute.TYPE_REFERENCES);
        	}
            //如果子集不为空进行处理
            if ((listChildren != null && listChildren.getLength()>0) || (listChildren2 != null && listChildren2.getLength()>0)){

                List lastListChildTags = new ArrayList();
            	//地归循环下面元素内容
                if(listChildren != null && listChildren.getLength()>0){
                    List listChildTags = getChildren(listChildren);
                    lastListChildTags.addAll(listChildTags);
                }
                if(listChildren2 != null && listChildren2.getLength()>0){
                    List listChildTags2 = getChildren(listChildren2);
                    lastListChildTags.addAll(listChildTags2);
                }
                
                
                //如果当前对象Object或reference那么下面的元素作为DataRule对象的子集
                if (ConfigAttrabute.TYPE_MODEL.equals(dataRule.getType())||ConfigAttrabute.TYPE_REFERENCE.equals(dataRule.getType())){
                    dataRule.setChildren(lastListChildTags);
                //如果当前操作是table，那么它的子元素做为DataRule对象的属性保存
                }else if (ConfigAttrabute.TYPE_TABLE.equals(dataRule.getType()) && lastListChildTags!=null 
                        && !lastListChildTags.isEmpty()){
                    //循环取得filters,attachments,checks,references内容保存
                    for (int j = 0;j< lastListChildTags.size();j++){
                        List listProperties = (List)lastListChildTags.get(j);
                        //取得0元素用于识别是对应相应的DataRule属性进行保存。
                        String type = (String)listProperties.get(0);
                        listProperties.remove(0);
                        
                        //过滤条件保存
                        if (ConfigAttrabute.TYPE_FILTERS.equals(type)){
                            dataRule.setFilters(listProperties);
                        //关联关系
                        }else if (ConfigAttrabute.TYPE_REFERENCES.equals(type)){
                            dataRule.setReferences(listProperties);
                        }
                    }
                } 
            }
            resultList.add(dataRule);
        }
		return resultList;
	}
	
	public Map<String,String> attributeToMap(NamedNodeMap attrabuteList){
		 Map attributeMap = new HashMap();
		 if(attrabuteList!=null && attrabuteList.getLength()>0){
		       for (int i=0;i<attrabuteList.getLength();i++){
		    	   Node attribute = (Node)attrabuteList.item(i);
		           attributeMap.put((String)attribute.getNodeName(),(String)attribute.getNodeValue());
		       }
		 }
        return attributeMap;
	}
	
	
	
}
