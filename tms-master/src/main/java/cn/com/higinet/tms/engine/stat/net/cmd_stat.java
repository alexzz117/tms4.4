package cn.com.higinet.tms.engine.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cache.db_cache;
import cn.com.higinet.tms.engine.core.concurrent.counter;
import cn.com.higinet.tms.engine.stat.stat_row;
import cn.com.higinet.tms.engine.stat.serv.stat_serv;

public class cmd_stat implements cmd
{
	static Logger log = LoggerFactory.getLogger(cmd_stat.class);

	final private List<stat_row> read_stat(BufferedReader in, counter c)
			throws NumberFormatException, IOException
	{
		int stat_count = Integer.parseInt(in.readLine());
		List<stat_row> ret = new ArrayList<stat_row>(stat_count);
		for (int i = 0; i < stat_count; i++)
			ret.add(stat_row.from_net_request(in.readLine(), c));
		return ret;
	}

	public int on_cmd(String[] param, BufferedReader in, BufferedWriter out) throws IOException
	{
		String version = in.readLine();
		String syncFlag = in.readLine();
		boolean isSync = "SYNC".equals(syncFlag);
		db_cache.get(Long.parseLong(version));

		counter c = new counter();
		List<stat_row> list = read_stat(in, c);
		int count = c.get();
		if (log.isDebugEnabled())
			log.debug("统计数量:" + count);

		stat_serv.eval_inst().request(list);

		if (isSync)
		{
			c.wait_gt(0, 10);
			if (c.get() < 0)
			{
				out.write("-\n");
				log.error("统计写发生错误");
				out.flush();
				return 0;
			}
		}

		out.write("+\n0\n");

		out.flush();
		return 0;
	}
}
