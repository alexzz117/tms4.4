package cn.com.higinet.tms35.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.core.cache.refresh.RefreshChain;

//@Service("cacheRefreshService")
public class service_cache_refresh extends AsyncService {
	Logger log = LoggerFactory.getLogger(service_cache_refresh.class);

	@Autowired
	private RefreshChain refreshChain;
//
//	@Autowired
//	private RiskModelCache riskModelCache;

	private void original_service(Request req, Response res) {
		io_env ie = new io_env(req, res);
		try {
			Map<String, Object> p_m = req.getParameterMap();
			log.debug("{}", p_m);

			String param = (String) p_m.get("tableName");

			String tableName = "";
			String userid = "";
			if (param != null) {
				String[] s = param.split(",");
				tableName = s.length > 0 ? s[0] : "";
				userid = s.length > 1 ? s[1] : "";
			}
			/*if (tableName.equals("TMS_COM_USERPATTERN")) {
				// 刷新行为习惯的缓存
				log.debug("userpattern cache refresh");
				//db_userpattern.cache.del(userid);
				dao_userpattern.remove_cache(userid);
			} else if (tableName.equals("TMS_MGR_IPLOCATION")) {
				// 刷新IP地址库
				log.debug("iplocation cache refresh");
				dao_ip.reset();
				ip_cache.reset();
				ip_cache.Instance();
			} else if (tableName.startsWith("TMS_RUN_")) {

			} else if ("TMS_MGR_ROSTER".equals(tableName)) {
				// 名单
				if (log.isDebugEnabled()) {
					log.error("update roster cache.");
				}
				synchronized (txn.class) {
					List<Map<String, String>> list = (List<Map<String, String>>) p_m.get("rosters");
					if (list != null && list.size() > 0) {
						db_cache.get().roster().update_roster(list);
					}
				}
			} else if ("TMS_MGR_ROSTERVALUE".equals(tableName)) {
				// 名单值
				if (log.isDebugEnabled()) {
					log.debug("update rostervalue cache.");
				}
				synchronized (txn.class) {
					List<Map<String, Object>> list = (List<Map<String, Object>>) p_m.get("rosters");
					if (list != null && list.size() > 0) {
						db_cache.get().roster().update_rostervalue(list);
					}
				}
			} else if ("ADD_ROSTER".equals(tableName)) {
				synchronized (txn.class) {
					db_cache.get().roster().add_value((String) p_m.get("rosterName"), (String) p_m.get("rosterValue"), Long.parseLong((String) p_m.get("txnTime")));
				}
			}
			//			else if(tableName.equals("TMS_MGR_SAFE_CARD")) {
			//				dao_safe_card.del_cache(userid);
			//			} else if(tableName.equals("TMS_MGR_SAFE_DEVICE")) {
			//				dao_safe_device.del_cache(userid);
			//			} 
			//			else if(tableName.equals("TMS_BANK_SWITCH")) {
			//				// 刷新通道开关的缓存
			//				log.info("刷新通道开关的缓存");
			//				db_cache.get().bank_user_switch().load(new data_source());
			//				log.info("通道开关的缓存成功");
			//			}
			else {
				// 默认刷新配置缓存
				txn.init(new data_source(), false);
				// 风险模型缓存
				riskModelCache._initTms();
			}*/
			refreshChain.refresh(tableName, p_m, 0);
			ie.setReturnCode("000000");
		} catch (Exception e) {
			ie.setReturnCode("000001");
			ie.setData("ERROR", e.getLocalizedMessage());
			log.error(e.getLocalizedMessage(), e);
		} finally {
			ie.done();
		}
	}

	@SuppressWarnings("unchecked")
	public void doService(Request req, Response res) {
		long start_time = System.currentTimeMillis();
		try {
			original_service(req, res);
		} finally {
			log.info(String.format("service=%s,tcode=%s,used time=%dms", this.getClass().getName(), req.getTransactionCode(), System.currentTimeMillis() - start_time));
		}
	}
}