package cn.com.higinet.tms35;


import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.higinet.tms35.comm.clock;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.cache.cache_init;
import cn.com.higinet.tms35.core.concurrent.tm_count;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;
import cn.com.higinet.tms35.run.serv.risk_commit;
import cn.com.higinet.tms35.run.serv.risk_serv_eval;
import cn.com.higinet.tms35.run.serv.risk_serv_init;
import cn.com.higinet.tms35.service.io_env_test;
import cn.com.higinet.tms35.stat.client.x_socket_pool;
import cn.com.higinet.tms35.stat.serv.stat_serv;

public class evalTest
{
	Logger log = LoggerFactory.getLogger(this.getClass());

	synchronized public static void init()
	{
		try
		{
			Properties p = new Properties();
			InputStream in = ClassLoader.getSystemResourceAsStream("server.properties");
			p.load(in);
			in.close();
			tmsapp.set_config(p);
			String configFilePath = p.getProperty("spring.configfile");
			new ClassPathXmlApplicationContext(configFilePath.split(","));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	static void startup()
	{
		x_socket_pool.Instance().start();
		stat_serv.eval_inst().start();
		stat_serv.query_inst().start();
		risk_commit.commit_pool().start();
		risk_serv_eval.eval_pool().start();
		risk_serv_init.init_pool().start();
	}

	static void shutdown()
	{
		risk_serv_init.init_pool().shutdown(false);
		risk_serv_eval.eval_pool().shutdown(false);
		risk_commit.commit_pool().shutdown(false);
		stat_serv.eval_inst().shutdown(false);
		stat_serv.query_inst().shutdown(false);
		x_socket_pool.Instance().close();
	}

	public static void puser(int i, String sessionPrefix) throws SQLException
	{
		int uid=i%1000000;
		
		TreeMap<String, Object> m = new TreeMap<String, Object>();
		m.put("USERID", "user" + uid/2);
		m.put("SESSIONID", sessionPrefix + i);
		m.put("TXNCODE", sessionPrefix + i);
		m.put("CHANCODE", "CH01");

		m.put("TXNID", "TXN00"+(uid%2+1));
		m.put("DEVICEID", "dev-"+uid%250000);
		m.put("IPADDR", "58.1.1."+(uid%250+1));
		m.put("TXNTIME", date_tool.format(new Date()));
		m.put("balance", "1000");
		m.put("currencyType", "人民币");
		m.put("seAcc", "se-"+uid+uid%5);
		m.put("recAcc", "re-"+uid+uid%5);

		if (i % 2 == 0)
		{
			m.put("payAmount", "100");
		}
		else
		{
			m.put("payAmount", "10000");
		}

		m.put("TXNSTATUS", uid%100==0?"0":"1");
		risk_serv_init.init_pool().request(run_env.identification(new io_env_test(m), false));
//		risk_eval_main.confirm_pool().request(run_env.identification(new io_env_test(m), true));
//		m.remove("TXNSTATUS");
	}

	public static void main(String[] args)
	{
		clock ta = new clock();
		init();
		ta.pin("init spring");
		cache_init.init(new data_source());
		ta.pin("init cache");
		ta.reset();
		startup();
		ta.pin("init threads");

		clock t = new clock();
		
		String session_prefix="sess-";
		
		if (args.length > 0)
			session_prefix=args[0];

		int xx = 2;
		if (args.length > 1)
			xx = Integer.parseInt(args[1]);
		int beg = 0;
		if (args.length > 2)
			beg = Integer.parseInt(args[2])<<1;
		int end = beg + 2000000;
		if (args.length > 3)
			end = (Integer.parseInt(args[3])<<1) + beg;

		final int ROW_COUNT = 1<<20;
		java.util.List<Integer> list = new ArrayList<Integer>(ROW_COUNT);
		t.reset();
		tm_count tc=new tm_count(100,100*1000);
		clock cc=new clock();
		for (int x = 0; x < xx; x++)
		{
			for (int i = beg; i < end; i += ROW_COUNT)
			{
				list.clear();
				for (int j = 0; j < ROW_COUNT && i + j < end; j++)
					list.add(i + j);
				Collections.shuffle(list);
				for (int j = 0, len = list.size(); j < len; j++)
				{
					try
					{
						puser(list.get(j),session_prefix);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}
					tc.add_now(tm_tool.lctm_ms(), 1);
					if(cc.count_ms()>1000)
					{
						tc.remove(20000);
						cc.pin("POS=" + (j + i) + "/" + (end) + " TPS=" + tc.get_tps());
					}
				}
			}
			t.pin("........update start.........");
		}
		t.pin("trans.ok.");
		shutdown();
		ta.pin("game over:");
	}
}
