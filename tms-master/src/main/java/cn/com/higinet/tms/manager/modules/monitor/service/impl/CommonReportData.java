package cn.com.higinet.tms.manager.modules.monitor.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.monitor.model.DataVO;

@Service("commonReportData")
public class CommonReportData extends AbstractReportData {
	
	public Object getDetailRegionGridData(DataVO dataVO){
		String sql = dataVO.getSqlId();
		//1 执行相应的SQL语句，获取数据
		String strSql = tmsSqlMap.getSql(sql);
		Map<String,Object> cond = dataVO.getCond();
		String str = MapUtil.getString(cond, "regioncity");
		strSql = strSql.replaceFirst("\\$\\{regioncity\\}", str);
		
		//zhangfg 2011-12-30 用于排序
		if(strSql!=null && !"".equals(strSql)){
			strSql += " order by "+cond.get("order").toString() +" desc ";
		}
		
		List<Map<String,Object>> dataList =  tmsSimpleDao.queryForList(strSql, cond);
		dataVO.setData(dataList);
	
		return dataVO;
	}
}
