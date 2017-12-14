package cn.com.higinet.tms35.service;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.JacksonMapper;
import cn.com.higinet.tms35.comm.MapUtil;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.core.cache.db_device;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("httpClodConfirmService")
public class service_http_cload_confirm extends AbstractHttpService
{
	static Logger log = LoggerFactory.getLogger(service_http_cload_confirm.class);
	private static final boolean isDebug = log.isDebugEnabled();
	
	public void doService(Request request, Response response)
	{
		
		// 校验请求数据
		String resultJson = validate(request);
		
		if(resultJson == null) {
			try {
				// 获取交易数据JSON串
				String json = parseJsonString(request);
				if(isDebug) {
					log.debug("风险确认数据："+json);
				}
				
				//风险确认
				Map transactionObj = JacksonMapper.getInstance().readValue(json, TreeMap.class);
	            
				Map<String, Object> transaction  = MapUtil.getMap(transactionObj,StaticParameters.Transaction.TRANSACTION);

					
				//风险确认
				io_env_http io = new io_env_http(request,response,transaction,true,false);
				run_env re = run_env.identification(io, true);
				if (re == null)
					return;
				tms_worker<run_env> worker = risk_serv_init.init_pool();
				worker.request(re);
				
				if (!db_device.IS_DEVFINGER_ON)
				{
					io.setReturnCode(StaticParameters.SYSTEM_SUCCESS);
					io.done();
				}
				worker.request(re);
				
			}catch (Exception e) {
				log.error("Transaction Confirm Fail.",e);
			}
		}
		
	}
}
