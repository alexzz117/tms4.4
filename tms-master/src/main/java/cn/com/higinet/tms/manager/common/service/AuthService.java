package cn.com.higinet.tms.manager.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.service.AuthService;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;

@Service("cmcAuthService")
public class AuthService{

	@Autowired
	private SimpleDao cmcSimpleDao;
	
	public List<Map<String, Object>> getAuthLogByLogId(String logId) {
		String sql = "SELECT * FROM TMS_MGR_AUTHLOG WHERE LOG_ID = '"+logId+"' ORDER BY LOG_ORDER";
		List<Map<String, Object>> logList = cmcSimpleDao.queryForList(sql);
		return logList;
	}

	public List<Map<String, Object>> getLogByLogId(String logId) {
		String sql = "SELECT * FROM CMC_OPERATE_LOG WHERE LOG_ID = '"+logId+"' ORDER BY ORDER_ID";
		List<Map<String, Object>> logList = cmcSimpleDao.queryForList(sql);
		return logList;
	}

	public void batchUpdateAuthLog(String[] updateSqls) {
		for(String s : updateSqls){
			if(CmcStringUtil.isEmpty(s)){
				return;
			}
		}
		cmcSimpleDao.batchUpdate(updateSqls);
	}
}
