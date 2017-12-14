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

@Service("httpClodBatchConfirmService")
public class service_http_cload_batchconfirm extends AbstractHttpService
{
	static Logger log = LoggerFactory.getLogger(service_http_cload_batchconfirm.class);
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
					log.debug("批量风险确认数据："+json);
				}
				
				//批量风险确认
				TreeMap trans = JacksonMapper.getInstance().readValue(json, TreeMap.class);
				io_env_http ie = new io_env_http(request,res,trans,true,true);
				Map<String,Map<String, Object>> sub_req_list = (Map<String,Map<String, Object>>) ie
						.getParameterOriMap().get(StaticParameters.Transaction.TRANSACTIONS);

				counter count = new counter();
				List<io_env_group> sub_trans = new ArrayList<io_env_group>(sub_req_list.size());
				for (int i = 0 ; i<sub_req_list.size();i++)
				{
					Map m = MapUtil.getMap(sub_req_list,StaticParameters.Transaction.TRANSACTION+i);
					io_env_group signal_env = new io_env_group(m, res, count);
					sub_trans.add(signal_env);
					run_env re = run_env.identification(signal_env, true);
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
				}

				if (ret == 0)
				{
					Map<String, Object> main_req_list = (Map<String, Object>) ie.getParameterOriMap().get(
							StaticParameters.Batch.BATCH);

					count = new counter();
					io_env_group main_env;
					{
						main_env = new io_env_group(main_req_list, res, count);
						run_env re = run_env.identification(main_env, true);
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
					}
				}

				// 返回信息
				ie.setReturnCode(ret_ie.return_code);
				ie.done();
			}catch (Exception e) {
				log.error("Batch Confirm Fail.",e);
			}
		}
	}
}
