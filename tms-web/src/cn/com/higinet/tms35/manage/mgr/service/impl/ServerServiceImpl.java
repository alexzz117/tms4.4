package cn.com.higinet.tms35.manage.mgr.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.mgr.service.ServerService;

@Service("serverService")
public class ServerServiceImpl implements ServerService {
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
