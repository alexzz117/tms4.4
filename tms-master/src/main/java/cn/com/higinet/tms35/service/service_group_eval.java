package cn.com.higinet.tms35.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.server.core.AsyncService;
import cn.com.higinet.rapid.server.core.Request;
import cn.com.higinet.rapid.server.core.Response;
import cn.com.higinet.tms35.comm.StaticParameters;
import cn.com.higinet.tms35.core.concurrent.counter;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_serv_init;

@Service("groupevlService")
public class service_group_eval extends AsyncService
{
	@SuppressWarnings("unchecked")
	public void doService(Request req, Response res)
	{
		io_env ie = new io_env(req, res);
		List<Map<String, Object>> sub_req_list = (List<Map<String, Object>>) ie
				.getParameterOriMap().get("TransList");

		counter count = new counter();
		List<io_env_group> sub_trans = new ArrayList<io_env_group>(sub_req_list.size());
		for (Map<String, Object> m : sub_req_list)
		{
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
			Map<String, Object> main_req_list = (Map<String, Object>) ie.getParameterOriMap().get(
					"TransInfo");

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
	}

}
