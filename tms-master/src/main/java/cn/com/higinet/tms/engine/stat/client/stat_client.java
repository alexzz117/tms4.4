package cn.com.higinet.tms.engine.stat.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.hash;
import cn.com.higinet.tms.engine.core.cache.db_cache;
import cn.com.higinet.tms.engine.stat.stat_row;

public class stat_client
{
	static Logger log = LoggerFactory.getLogger(stat_client.class);

	final static public int hash_server(String param, int serv_count)
	{
		return (hash.clac(param, 1048919)) % 2089 % serv_count;
	}

	final static private List<List<stat_row>> dispatch(List<List<stat_row>> request,
			socket_pool sp, int mask)
	{
		int serv_count = sp.count(mask);
		List<List<stat_row>> ret = new ArrayList<List<stat_row>>(serv_count);
		for (int i = 0; i < serv_count; i++)
			ret.add(new LinkedList<stat_row>());

		for (List<stat_row> req_list : request)
		{
			if (req_list == null)
				continue;
			for (stat_row req : req_list)
			{
				int index = hash_server(req.m_param, serv_count);
				if (index == -1)
					return null;
				ret.get(index).add(req);
			}
		}

		return ret;
	}

	static private final String make_stat_reqstring(boolean is_query, List<stat_row> request)
	{
		StringBuffer sb = new StringBuffer(request.size() << 6);
		sb.append(is_query ? "QUERY\n" : "STAT\n");

		sb.append(db_cache.version()).append('\n');
		sb.append(request.size()).append('\n');
		for (stat_row row : request)
			row.to_net_request(sb);

		return sb.toString();
	}

	public static int send_request(boolean is_query, List<stat_row> request, List<Integer> time)
	{
		final socket_pool sp = socket_pool.Instance();
		List<List<stat_row>> disp_list = new ArrayList<List<stat_row>>();
		disp_list.add(request);
		int serv_count = -1;
		int mask = 0;
		clock c = new clock();
		for (;;)
		{
			for (; serv_count == -1;)
			{
				mask = sp.mask();
				serv_count = sp.count(mask);
				if (serv_count == 0)
					return 1;
			}

			if (time != null)
				time.add((int) c.count_ms());
			disp_list = dispatch(disp_list, sp, mask);
			if (disp_list == null)
				return 1;

			String[] req_string = new String[disp_list.size()];
			for (int i = 0; i < serv_count; i++)
			{
				List<stat_row> serv_list = disp_list.get(i);
				if (serv_list == null || serv_list.isEmpty())
					continue;

				req_string[i] = make_stat_reqstring(is_query, serv_list);
			}

			if (time != null)
				time.add((int) c.count_ms());
			int[] err_id = sp.send(mask, req_string, c, time);
//			int[] err_id=new int[req_string.length];
			Arrays.fill(err_id, 0);
			
			if (time != null)
				time.add((int) c.count_ms());
			for (int i = 0; i < err_id.length; i++)
			{
				if (err_id[i] != 0)
				{
					if (err_id[i] > 0)
						log.error("服务器[" + i + "]发生错误：" + sp.error_info(err_id[i]));
					serv_count = -1;
					continue;
				}

				if (is_query)
				{
					parse_result(disp_list.get(i), req_string[i]);
					disp_list.set(i, null);
				}
			}

			if (time != null)
				time.add((int) c.count_ms());
			if (serv_count == -1)
				continue;
			break;
		}

		return 0;
	}

	private static void parse_result(List<stat_row> list, String s)
	{
		if (list == null || s == null)
			return;
//		for (int i = 0, len = list.size(); i < len; i++)
//		{
//			list.get(i).dec_query_batch();
//			
//		}
//		
//		if(true)
//			return;

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
}
