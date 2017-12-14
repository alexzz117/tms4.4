package cn.com.higinet.tms.manager.modules.mgr.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;

@Service("serverService")
public class ServerService   {
	@Autowired
	private SimpleDao tmsSimpleDao;
	
	@Autowired
	private SqlMap tmsSqlMap;

	public void setCmcSimpleDao(SimpleDao tmsSimpleDao) {
		this.tmsSimpleDao = tmsSimpleDao;
	}

	/**
	 * 获取所有的服务器的列表
	 * @param conds 查询条件
	 * @return 服务器列表
	 */
	public List<Map<String, Object>> listServer(Map<String, String> conds) {
		String servType = MapUtil.getString(conds, "SERVTYPE");
		
		String sql2 = tmsSqlMap.getSql("TMS.SERVER.LIST");
		
		return tmsSimpleDao.queryForList(sql2,servType);
	}
}
