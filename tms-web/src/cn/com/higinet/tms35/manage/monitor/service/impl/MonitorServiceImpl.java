package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.DBConstant.TMS_COM_TAB;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.DataVO;
import cn.com.higinet.tms35.manage.monitor.service.MonitorService;
import cn.com.higinet.tms35.manage.monitor.service.ReportData;
import cn.com.higinet.tms35.manage.tran.TransCommon;

/**
 * 实时运行监控服务类
 * @author wangsch modified since 2013-05-24
 */
@Service("monitorService")
public class MonitorServiceImpl extends ApplicationObjectSupport implements MonitorService{
	@Autowired
	private SimpleDao officialSimpleDao;
	
	public void setOfficialSimpleDao(SimpleDao officialSimpleDao) {
		this.officialSimpleDao = officialSimpleDao;
	}

	public DataVO getDataList(DataVO dataVO, boolean isPage) {
		String dataType = dataVO.getDataType();
		return (DataVO)getApplicationContext().getBean(dataType,
				ReportData.class).getDataList(dataVO, isPage);
	}
	
	public Map<String, Integer> compareDescTop5(Map<String, Integer> dataMap) {
		Map<String,Integer> map = new LinkedHashMap<String,Integer>();
		List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>( 
				dataMap.entrySet()); 
			//排序 
			Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() { 
			public int compare(Map.Entry<String, Integer> o1, 
			Map.Entry<String, Integer> o2) { 
			return (o2.getValue() - o1.getValue()); 
			} 
		});
		for(Entry<String, Integer> e : infoIds) {  
            map.put(e.getKey(),e.getValue());  
        }  
		return map;
	}

	/**
	 * 获取“规则运行监控”表格数据
	 */
	public Page<Map<String, Object>> getRuleGrid(
			List<Map<String, Object>> dataList) {
		Page<Map<String,Object>> page = new Page<Map<String,Object>>();
		for(Map<String,Object> data:dataList){
			if(data.get("RULERATE")!=null){
				data.put("RULERATE", data.get("RULERATE")+"%");
			}
			String txntype = MapUtil.getString(data, "TXNTYPE");
	    	if(txntype!=null && !"".equals(txntype)){
	    		data.put("TXNNAME", getFullTxnPath(txntype,DBConstant.TMS_COM_TAB.TAB_DESC));
	    	}
		}
		page.setList(dataList);
		
		return page;
	}
	
	/**
	 * 获取交易名称，完整路径
	 * @param txnid
	 * @return
	 */
	public String getFullTxnPath(String txnid, String txnField) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("select " + TMS_COM_TAB.TAB_NAME +", " + TMS_COM_TAB.TAB_DESC +" from " + TMS_COM_TAB.TABLE_NAME + " where " + TMS_COM_TAB.TAB_NAME + " in("+TransCommon.arr2str(TransCommon.cutToIds(txnid))+") ORDER BY TAB_NAME desc");
		
		List<Map<String, Object>> fartherTranDef = officialSimpleDao.queryForList(sb.toString());
		
		//交易名称，全路径
		String txnName = "";
		for (Map<String, Object> tran : fartherTranDef) {
			String parentTxnName = MapUtil.getString(tran, txnField);
			txnName = parentTxnName + "-" + txnName;
		}
		if(!txnName.equals("")){
			txnName = txnName.substring(0, txnName.length()-1);
		}
		
		//渠道名称
//		String channelName = "";
//		String sql = "SELECT CHANN.CHANNELNAME FROM TMS_DP_CHANNEL CHANN,TMS_COM_TAB TAB WHERE CHANN.CHANNELID=TAB.CHANN AND TAB.TAB_NAME = '"+txnid+"'";
//		List<Map<String, Object>> channList = officialSimpleDao.queryForList(sql);
//		if(channList!=null && channList.size()!=0){
//			channelName = MapUtil.getString(channList.get(0), "CHANNELNAME");
//		}
//		if(!StringUtil.isEmpty(channelName)){
//			return channelName+"-"+txnName;
//		}
		
		return txnName;
	}
}
