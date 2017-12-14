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
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("httpClodEvlService")
public class service_http_cload_eval extends AbstractHttpService
{
	static Logger log = LoggerFactory.getLogger(service_http_cload_eval.class);
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
					log.debug("风险评估数据："+json);
				}
				
				Map transactionObj = JacksonMapper.getInstance().readValue(json, TreeMap.class);
	            
				Map<String, Object> transaction  = MapUtil.getMap(transactionObj,StaticParameters.Transaction.TRANSACTION);
				
				// 补充交易数据
				addTxnFeature(transaction,request);
				
				//风险评估
				io_env_http io = new io_env_http(request,response,transaction,false);
				run_env re = run_env.identification(io, false);
				if (re == null)
					return;
				tms_worker<run_env> worker = risk_serv_init.init_pool();
				worker.request(re);

	        }catch (Exception e) {
	        	log.error("Transaction Risk Fail.",e);
	        }
		}
	}
}
