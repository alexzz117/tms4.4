package cn.com.higinet.tms.engine.core.cache.refresh;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.dao.dao_userpattern;

public class UserPatternCacheRefresh implements CacheRefresh {
	private static final Logger log = LoggerFactory.getLogger(UserPatternCacheRefresh.class);

	private static final String NAME = "TMS_COM_USERPATTERN";

	@Override
	public boolean refresh(String cacheName, Map<String, Object> params, int index, RefreshChain chain) throws Exception {
		if (NAME.equals(cacheName)) {
			String param = (String) params.get("tableName");
			String userid = "";
			if (param != null) {
				String[] s = param.split(",");
				userid = s.length > 1 ? s[1] : "";
			}
			// 刷新行为习惯的缓存
			log.debug("userpattern cache refresh");
			//db_userpattern.cache.del(userid);
			dao_userpattern.remove_cache(userid);
			return true;
		} else {
			return chain.refresh(cacheName, params, ++index);
		}
	}

}
