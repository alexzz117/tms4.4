package cn.com.higinet.tms35.manage.datamgr.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import cn.com.higinet.cmc.util.MapUtil;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.auth.common.AuthStaticParameters;
import cn.com.higinet.tms35.manage.auth.exception.TmsMgrAuthDataSyncException;
import cn.com.higinet.tms35.manage.common.util.StringUtil;
import cn.com.higinet.tms35.manage.datamgr.common.ConfigAttrabute;
import cn.com.higinet.tms35.manage.datamgr.common.DataConfig;
import cn.com.higinet.tms35.manage.datamgr.service.DataOperService;
import cn.com.higinet.tms35.manage.datamgr.service.TableOperService;
import cn.com.higinet.tms35.manage.rule.service.RuleService;
import cn.com.higinet.tms35.manage.userpattern.service.UserPatternService;

/**
 * 数据操作服务公用实现服务
 * @author zhangfg
 * @version 1.0.0 2012-09-19
 */
@Service("abstractDataOperService")
public class AbstractDataOperService extends ApplicationObjectSupport implements DataOperService {

	private final static Log logger = LogFactory.getLog(AbstractDataOperService.class);
	
	@Autowired
	private SimpleDao tmpSimpleDao;
	
	/**
	 * 数据同步
	 */
	public void dataSync(DataConfig dataconfig,String pkValue,String authFlag) {
		//同步规则和路由，以及规则路由图
		/*try{
			if("tranRule".equals(dataconfig.getId())){
				String realOper = getRealOper(pkValue,"TMS_COM_RULE");
				boolean flag = AuthStaticParameters.AUTH_STATUS_1.equals(authFlag)?true:false;
				System.out.println("ruleId="+pkValue+", "+"operate="+realOper+", result="+flag);
				getApplicationContext().getBean("ruleService35", RuleService.class).auditRule(pkValue, realOper, flag);
				return;
			} else if("ruleChild".equals(dataconfig.getId())){
				String realOper = getRealOper(pkValue,"TMS_COM_RULE_CHILD");
				boolean flag = AuthStaticParameters.AUTH_STATUS_1.equals(authFlag)?true:false;
				System.out.println("lineId="+pkValue+", "+"operate="+realOper+", result="+flag);
				getApplicationContext().getBean("ruleService35", RuleService.class).auditLine(pkValue, realOper, flag);
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new TmsMgrAuthDataSyncException("未能成功同步交易规则数据。");
		}*/
		
		//用户行为习惯管理数据同步
		try{
			if("userpattern".equals(dataconfig.getId())){
				String[] split = pkValue.split(",");
				String userId = "";
				String statId = "";
				String patternId = "";
				if(split.length==3){
					userId = split[0];
					statId = split[1];
					patternId = split[2];
				}
				String realOper = getRealOper(pkValue,"TMS_COM_USERPATTERN");
				boolean flag = AuthStaticParameters.AUTH_STATUS_1.equals(authFlag)?true:false;
				System.out.println("userId="+userId+", "+"operate="+realOper+", "+"statId="+statId+", "+"patternId="+patternId+", result="+flag);
				
				getApplicationContext().getBean("userPatternService35", UserPatternService.class)					
					.synUserPatternData(userId, statId, patternId, realOper, flag);
				return;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new TmsMgrAuthDataSyncException("未能成功同步用户行为习惯数据。");
		}
		
		
		dataconfig.setPkValue(pkValue);
		List childList = dataconfig.getChildren();
		
		for (Iterator iter = childList.iterator();iter.hasNext();){
            //循环取得每个table元素定义内容
            Object obj = iter.next();
            DataConfig tableConfig=null;
            if(obj instanceof DataConfig){
            	tableConfig = (DataConfig)obj;
            }
            searchEachTable(tableConfig,dataconfig,authFlag);
        }
		dataconfig.getDataValues().clear();
	}
	
	/**
	 * 同步每一个表
	 * @param tableConfig
	 * @param dataconfig
	 */
	private void searchEachTable(DataConfig tableConfig,DataConfig dataconfig,String authFlag){
		String dataOperService = tableConfig.getAttrabute(ConfigAttrabute.DATAOPER_SERVICE);
		String tableName = tableConfig.getAttrabute(ConfigAttrabute.TABLE_NAME);
		if(StringUtil.isEmpty(dataOperService)){
			logger.error(tableName+"没有配置数据同步的服务类。");
			return ;
		}
		
		//同步其他数据
		String sqlType = tableConfig.getAttrabute(ConfigAttrabute.SQL_TYPE);
		if(StringUtil.isEmpty(sqlType)){
			logger.error(tableConfig.getAttrabute(ConfigAttrabute.TABLE_NAME)+"没有配置数据同步的方式。");
			return ;
		}
		
		//查询对应的数据集合
		List<Map<String, Object>> tempdatlist = getApplicationContext().getBean(dataOperService, TableOperService.class).queryDataList(tableConfig, dataconfig,authFlag);
		
		if(tempdatlist==null || tempdatlist.size()==0){
			return ;
		}
		// 获得从表的关系,循环
//		List referenceList = tableConfig.getReferences();
//		if(referenceList!=null){
//			for(Iterator references = referenceList.iterator(); references.hasNext();){
//				DataConfig reference = (DataConfig) references.next();
//				String reference_fk = "";
//				String source = reference.getAttrabute(ConfigAttrabute.SOURCE);
//				if(ConfigAttrabute.SOURCE_PARENT.equals(source)){
//					reference_fk = reference.getAttrabute(ConfigAttrabute.REFERENCE_FK);
//					//dataConfig的dataValues中添加reference的reference_fk和table_pk从上层表获取referenceValue，用逗号分隔
//					getReferenceValue(tempdatlist,reference,dataconfig);
//				}
//				List reTableList = reference.getChildren();
//				if(reTableList!=null){
//					for(Iterator iterRf = reTableList.iterator(); iterRf.hasNext();){
//						DataConfig reTable = (DataConfig)iterRf.next();
//						reTable.setParentReferenceFk(reference_fk);
//						searchEachTable(reTable,dataconfig,authFlag);
//					}
//				}
//			}
//		}
			
	}
	
	private String getRealOper(String tablePkValue, String tableName) {
		Map<String,String> conds = new HashMap<String, String>();
		conds.put("TABLE_PKVALUE", tablePkValue);
		conds.put("IS_MAIN", "1");
		conds.put("TABLE_NAME", tableName);
		
		Map<String, Object> authRecordMap = tmpSimpleDao.retrieve("TMS_MGR_AUTHRECORD",conds);// 授权已转移到里线库
		
		String realOper = MapUtil.getString(authRecordMap, "REAL_OPER");
		String realOper2 = "";
		if("c".equals(realOper)){
			realOper2="add";
		}else if("p".equals(realOper)){
			realOper2="copy";
		} else if("u".equals(realOper)){
			realOper2="mod";
		} else if("d".equals(realOper)){
			realOper2="del";
		} else {
			realOper2 = realOper;
		}
		return realOper2;
	}
	/**
	 * 根据reference的配置，拼装查询以下节点查询是的条件
	 * （从已经查询数据结果集中获取table_pk对应的值的字段，拼装成字符串，然后将其一reference_fk作为key放到map中）
	 * @param dataList
	 * @param reference
	 * @param dataconfig
	 */
//	private void getReferenceValue(List<Map<String,Object>> dataList,DataConfig reference,DataConfig dataconfig){
//		
//		if(dataList!=null && !dataList.isEmpty()){
//			Map<String, Object> datavalue = dataconfig.getDataValues();
//			String reference_fk = reference.getAttrabute(ConfigAttrabute.REFERENCE_FK);
//			if(!StringUtil.isEmpty(StringUtil.parseToString(datavalue.get(reference_fk)))){
//				return ;
//			}
//			String table_pk = reference.getAttrabute(ConfigAttrabute.TABLE_PK);
//			String referenceValue = "";
//			for(int i=0;i<dataList.size();i++){
//				Map<String,Object> data = dataList.get(i);
//				if(i>0){
//					referenceValue += ConfigAttrabute.REFERENCE_APLIT;
//				}
//				referenceValue += data.get(table_pk);
//			}
//			datavalue.put(reference_fk, referenceValue);
//			dataconfig.setDataValues(datavalue);
//		}
//		
//	}
	
	
	/**
	 * 数据打包
	 */
	public boolean pack(DataConfig dataconfig) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 数据解包
	 */
	public boolean unPack(DataConfig dataconfig) {
		// TODO Auto-generated method stub
		return false;
	}

}
