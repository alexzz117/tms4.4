package cn.com.higinet.tms.engine.stat.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.hash;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.db_cache;
import cn.com.higinet.tms.engine.stat.stat_row;

public class x_stat_client
{
	static Logger log = LoggerFactory.getLogger(x_stat_client.class);
	static final boolean isAsync = tmsapp.get_config("tms.stat.eval.isAsync", 1) == 1;

	final static public int hash_server(String param, int serv_count)
	{
		final int h = (hash.clac(param, 1048919)) % 2089;
		return h % serv_count;
	}

	final static private List<List<stat_row>> dispatch(List<stat_row> request, int serv_count)
	{
		final List<List<stat_row>> ret = new ArrayList<List<stat_row>>(serv_count);
		for (int i = 0; i < serv_count; i++)
			ret.add(new ArrayList<stat_row>(request.size() * 2 / serv_count));

		for (stat_row req : request)
		{
			final int index = hash_server(req.m_param, serv_count);
			ret.get(index).add(req);
		}

		return ret;
	}

	static private final String make_stat_reqstring(boolean is_query, List<stat_row> request)
	{
		StringBuffer sb = new StringBuffer(request.size() << 6);
		sb.append(is_query ? "QUERY\n" : "STAT\n");
		sb.append(db_cache.version()).append('\n');
		if (!is_query)
		{
			sb.append(isAsync ? "ASYNC" : "SYNC").append('\n');
		}
		sb.append(request.size()).append('\n');
		for (stat_row row : request)
			row.to_net_request(sb);

		return sb.toString();
	}

	public static int send_request(boolean is_query, List<stat_row> request, List<Integer> time)
	{
		final x_socket_pool sp = x_socket_pool.Instance();
		int serv_count = -1;
		long version = 0;
		for (; serv_count == -1;)
		{
			version = sp.version();
			serv_count = sp.count();
			if (serv_count == 0)
				return 1;
		}

		final List<List<stat_row>> disp_list = dispatch(request, serv_count);
		String[] req_string = new String[disp_list.size()];
		for (int i = 0; i < serv_count; i++)
		{
			List<stat_row> serv_list = disp_list.get(i);
			if (serv_list == null || serv_list.isEmpty())
				continue;
			req_string[i] = make_stat_reqstring(is_query, serv_list);

		}
		clock c = new clock();
		for (;;)
		{
			version = sp.version();
			int[] err_id = sp.send(version, req_string, c, time);

			for (int i = 0; i < err_id.length; i++)
			{
				if (err_id[i] != 0)
				{
					if (err_id[i] > 0)
						log.error("服务器[" + i + "]发生错误：" + sp.error_info(err_id[i]));
					version = 0;
					continue;
				}

				if (is_query)
				{
					parse_query_result(disp_list.get(i), req_string[i]);
				}
				else
				{
					parse_eval_result(disp_list.get(i), req_string[i]);
				}
				disp_list.set(i, null);
				req_string[i]=null;
			}

			if (version == 0)
				continue;
			break;
		}

		return 0;
	}
	
	private static void parse_query_result(List<stat_row> list, String s)
	{
		if (list == null || s == null)
			return;

		int from = 0;
		int p = s.indexOf('\n');
		stat_row row = null;
		for (int i = 0, len = list.size(); i < len; i++)
		{
			row = list.get(i);
			if (p == -1)
			{
				row.set_value(s.substring(from));
			}
			else
			{
				row.set_value(s.substring(from, p));
			}
			row.dec_batch();
			from = p + 1;
			p = s.indexOf('\n', from);
		}
	}
	
	private static void parse_eval_result(List<stat_row> list, String s)
	{
		if (list == null || s == null)
			return;

		for (int i = 0, len = list.size(); i < len; i++)
		{
			stat_row row = list.get(i);
			row.dec_batch();
		}
	}
}