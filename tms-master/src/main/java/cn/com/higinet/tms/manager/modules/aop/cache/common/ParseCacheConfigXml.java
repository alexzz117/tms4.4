package cn.com.higinet.tms.manager.modules.aop.cache.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cn.com.higinet.tms.manager.modules.common.PropertiesUtil;
import cn.com.higinet.tms.manager.modules.datamgr.common.ConfigAttrabute;

/**
 * 解析refleshConfig.xml，获取所有method的配置信息
 * @author wangsch
 *
 */
public class ParseCacheConfigXml {
	private Map<String,MethodConfig> methodConfigMap=null;
	
	private static ParseCacheConfigXml instance=null;

	public static ParseCacheConfigXml getInstance(){
		if(instance==null){
			instance = new ParseCacheConfigXml();
		}
		return instance;
	}
	
	private ParseCacheConfigXml(){
		try{
			String path = PropertiesUtil.getPropInstance().get("REFRESHCONFIGPATH");
			InputStream in = ParseCacheConfigXml.class.getResourceAsStream(path);
			
			if(in!=null){
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(in);
				Element root = doc.getDocumentElement();
				
		        if(methodConfigMap==null){
		        	methodConfigMap = new HashMap<String, MethodConfig>();
		        }
		        
		        NodeList methodTags = root.getElementsByTagName(ConfigAttrabute.METHOD);
		        for(int i=0;i<methodTags.getLength();i++){
		        	MethodConfig mc = new MethodConfig();
		        	Element method = (Element)methodTags.item(i);
		        	
		        	String modelId = method.getAttribute(ConfigAttrabute.MODELID);
		        	mc.setModelId(modelId);
		        	
		        	String modelName = method.getAttribute(ConfigAttrabute.MODELNAME);
					mc.setModelName(modelName);
		        	
		        	String mcId = method.getAttribute(ConfigAttrabute.MID);
		            mc.setId(mcId);
		            
		            
		            String name = method.getAttribute(ConfigAttrabute.NAME);
		            mc.setName(name);
		            
		            String realOper = method.getAttribute(ConfigAttrabute.REALOPER);
		            mc.setRealOper(realOper);
		            
		            mc.setCacheRefreshService(method.getAttribute(ConfigAttrabute.CACHEREFRESHSERVICE));
		            mc.setTxnId(method.getAttribute(ConfigAttrabute.TXNID));
		            mc.setTableName(method.getAttribute(ConfigAttrabute.TABLENAME));
		            mc.setTablePk(method.getAttribute(ConfigAttrabute.TABLEPK));
		            mc.setTablePkType(method.getAttribute(ConfigAttrabute.TABLEPKTYPE));
		            mc.setSyncPk(method.getAttribute(ConfigAttrabute.SYNCPK));
		            mc.setOperateData(method.getAttribute(ConfigAttrabute.OPERATE_DATA));
		            mc.setIsAuth(method.getAttribute(ConfigAttrabute.ISAUTH));
		            mc.setIsRefresh(method.getAttribute(ConfigAttrabute.ISREFRESH));
		            mc.setRefreshMsg(method.getAttribute(ConfigAttrabute.REFRESHMSG));
		            mc.setDepPks(method.getAttribute(ConfigAttrabute.DEPPKS));
		            mc.setDepModels(method.getAttribute(ConfigAttrabute.DEPMODELS));
		            mc.setQueryTableName(method.getAttribute(ConfigAttrabute.QUERYTABLENAME));
		            mc.setQueryTablePk(method.getAttribute(ConfigAttrabute.QUERYTABLEPK));
		            
		            methodConfigMap.put(mcId, mc);
		        }
			}else{
			    throw new RuntimeException("未能读取到refreshConfig.xml配置文件！");
			}
		}catch(Exception e){
			e.printStackTrace();
		    throw new RuntimeException("解析refreshConfig.xml配置文件失败！");
		}
	}
	
	public MethodConfig getMethod(String methodName){
		return methodConfigMap.get(methodName);
	}
}
