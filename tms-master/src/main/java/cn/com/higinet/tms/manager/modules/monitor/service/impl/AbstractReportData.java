package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.Order;
import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.DataVO;
import cn.com.higinet.tms.manager.modules.monitor.service.ReportData;

public abstract class AbstractReportData implements ReportData {

	@Autowired
	@Qualifier("dynamicSimpleDao")
	SimpleDao dynamicSimpleDao;

	@Autowired
	@Qualifier("onlineSimpleDao")
	SimpleDao onlineSimpleDao;

	@Autowired
	@Qualifier("offlineSimpleDao")
	SimpleDao offlineSimpleDao;
	
	@Autowired
	@Qualifier("tmsSqlMap")
	SqlMap tmsSqlMap;
	/**
	 * 
	 * @param dataVO
	 * @return
	 */
	public Object getDataList(DataVO dataVO, boolean isPage){
		String sql = dataVO.getSqlId();
		String extSql = dataVO.getExtSql() == null ? "" : dataVO.getExtSql();
		String strSql;
		if(CmcStringUtil.isBlank(sql)){
			strSql = extSql;
		}else{
			strSql = tmsSqlMap.getSql(sql) + extSql;
		}
		
		Map<String,Object> conds  = dataVO.getCond();
		
		if(conds != null && !CmcStringUtil.isBlank(MapUtil.getString(conds, "order"))){
			strSql += " order by " + conds.get("order") + " desc ";
		}
		if (dataVO.isUseTmp()) {
			if(isPage){
				Page<Map<String,Object>> pageData = offlineSimpleDao.pageQuery(strSql, conds, new Order());
				dataVO.setData(pageData);
			}else{
				List<Map<String,Object>> dataList =  offlineSimpleDao.queryForList(strSql, conds);
				dataVO.setData(dataList);
			}
		}else{
			if(isPage){
				Page<Map<String,Object>> pageData = onlineSimpleDao.pageQuery(strSql, conds, new Order());
				dataVO.setData(pageData);
			}else{
				List<Map<String,Object>> dataList =  onlineSimpleDao.queryForList(strSql, conds);
				dataVO.setData(dataList);
			}
		}
		return dataVO;
	}
	
	
}
