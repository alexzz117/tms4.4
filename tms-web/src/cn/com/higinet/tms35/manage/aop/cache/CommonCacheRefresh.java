package cn.com.higinet.tms35.manage.aop.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms35.manage.mgr.service.ServerService;

/**
 * 缓存刷新
 * @author yanghui
 * @date 2011-7-28
 */
@Service("commonCacheRefresh")
public class CommonCacheRefresh extends AbstractCacheRefresh {
	@Autowired
	private ServerService serverService;

	public void setServerService(ServerService serverService) {
		this.serverService = serverService;
	}

	protected List<Map<String, Object>> getIpList() {
		Map<String, String> conds = new HashMap<String, String>();
		conds.put("SERVTYPE", "1");
		return serverService.listServer(conds);
	}
	
	protected int againNum() {
		return 3;
	}
}
