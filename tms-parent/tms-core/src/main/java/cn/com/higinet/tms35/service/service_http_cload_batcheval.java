package cn.com.higinet.tms35.service;

import java.util.ArrayList;
import java.util.List;
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
import cn.com.higinet.tms35.core.concurrent.counter;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("httpClodBatchEvlService")
public class service_http_cload_batcheval extends AbstractHttpService
{
	static Logger log = LoggerFactory.getLogger(service_http_cload_batcheval.class);
	private static final boolean isDebug = log.isDebugEnabled();
	
	public void doService(Request request, Response res)
	{
		
		// 校验请求数据
		String resultJson = validate(request);
		
		if(resultJson == null) {
			try {
				// 获取交易数据JSON串
				String json = parseJsonString(request);
				if(isDebug) {
					log.debug("批量风险评估数据："+json);
				}
				
				TreeMap trans = JacksonMapper.getInstance().readValue(json, TreeMap.class);
				
				io_env_http ie = new io_env_http(request,res,trans,false,true);

				Map<String, Object> main_req_list = (Map<String, Object>) ie.getParameterOriMap().get(
						StaticParameters.Batch.BATCH);

				// 补充主交易数据
				addTxnFeature(main_req_list,request);
				
				Map<String,Map<String, Object>> sub_req_list = (Map<String,Map<String, Object>>) ie.getParameterOriMap().get(StaticParameters.Transaction.TRANSACTIONS);
				
				counter count = new counter();
				List<io_env_group> sub_trans = new ArrayList<io_env_group>(sub_req_list.size());
				for (int i = 0 ; i<sub_req_list.size();i++)
				{
					Map m = MapUtil.getMap(sub_req_list,StaticParameters.Transaction.TRANSACTION+i);
					String devicetoken = MapUtil.getString(main_req_list, StaticParameters.Transaction.DEVICETOKEN);
					if(devicetoken!=null && devicetoken.length() > 0) {
						m.put(StaticParameters.Transaction.DEVICETOKEN, devicetoken);
					}
					String keystrokesimi = MapUtil.getString(main_req_list, StaticParameters.Transaction.KEYSTROKESIMI);
					if(keystrokesimi!=null && keystrokesimi.length() > 0) {
						m.put(StaticParameters.Transaction.KEYSTROKESIMI, keystrokesimi);
					}
					String ipaddress = MapUtil.getString(main_req_list, StaticParameters.Transaction.IPADDRESS);
					if(ipaddress!=null && ipaddress.length() > 0) {
						m.put(StaticParameters.Transaction.IPADDRESS, ipaddress);
					}
					
					io_env_group signal_env = new io_env_group(m, res, count);
					sub_trans.add(signal_env);
					run_env re = run_env.identification(signal_env, false);
					if (re == null)
					{
						count.set_error(-1);
						break;
					}

					risk_serv_init.init_pool().request(re);
				}

				int ret = count.wait_gt_0();
				io_env_group ret_ie = null;
				for (io_env_group sie : sub_trans)
				{
					if (ret_ie == null)
					{
						ret_ie = sie;
						continue;
					}

					if (!StaticParameters.SYSTEM_SUCCESS.equals(ret_ie.return_code))
					{
						ret_ie = sie;
						break;
					}

					if (ret_ie.getResData("disposal").compareTo(sie.getResData("disposal")) < 0)
					{
						ret_ie = sie;
						continue;
					}
				}

				if (ret == 0)
				{
					count = new counter();
					io_env_group main_env;
					{
						main_env = new io_env_group(main_req_list, res, count);
						run_env re = run_env.identification(main_env, false);
						if (re == null)
						{
							count.set_error(-1);
						}
						else
							risk_serv_init.init_pool().request(re);
					}

					ret = count.wait_gt_0();
					if (ret < 0)
					{
						ret_ie = main_env;
					}
					else
					{
						if (!StaticParameters.SYSTEM_SUCCESS.equals(ret_ie.return_code))
							ret_ie = main_env;
						else if (ret_ie.getResData("disposal").compareTo(main_env.getResData("disposal")) < 0)
						{
							ret_ie = main_env;
						}
					}
				}

				// 返回信息
				ie.setReturnCode(ret_ie.return_code);
				ie.setData("disposal", ret_ie.getResData("disposal"));
				ie.setData("score", ret_ie.getResData("score"));

				ie.done();
				
			}catch (Exception e) {
				log.error("Batch Risk Fail.",e);
			}
		}
		
	}
}
