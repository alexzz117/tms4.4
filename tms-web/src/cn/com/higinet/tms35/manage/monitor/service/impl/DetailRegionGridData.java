package cn.com.higinet.tms35.manage.monitor.service.impl;

import java.util.List;

import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.monitor.model.DataVO;

@Service("detailRegionGridData")
public class DetailRegionGridData extends AbstractReportData{

	
	public Object getDataList(DataVO dataVO) {
		// TODO Auto-generated method stub
		String sql = dataVO.getSqlId();
		//1 执行相应的SQL语句，获取数据
		String strSql = tmsSqlMap.getSql(sql);
		Map<String,Object> cond = dataVO.getCond();
		String str = MapUtil.getString(cond, "regioncity");
		strSql = strSql.replaceFirst("\\$\\{regioncity\\}", str);
		List<Map<String,Object>> dataList =  tmsSimpleDao.queryForList(strSql, cond);
		dataVO.setData(dataList);
	
		return dataVO;
	}

}
