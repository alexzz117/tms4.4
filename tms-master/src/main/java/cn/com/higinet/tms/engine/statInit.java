package cn.com.higinet.tms.engine;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.cache_init;
import cn.com.higinet.tms.engine.core.cond.date_tool;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.run.run_env;
import cn.com.higinet.tms.engine.run.serv.risk_commit;
import cn.com.higinet.tms.engine.run.serv.risk_serv_eval;
import cn.com.higinet.tms.engine.run.serv.risk_serv_init;
import cn.com.higinet.tms.engine.service.io_env_test;
import cn.com.higinet.tms.engine.stat.client.x_socket_pool;
import cn.com.higinet.tms.engine.stat.serv.stat_serv;

public class statInit
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

	static final public int RSHash(final String s, final int prime)
	{
		int h = 0;
		for (int i = 0, len = s.length(); i < len; i++)
		{
			h *= prime;
			h ^= 0xff & s.charAt(i);
		}

		return h;
	}

	static final public int RSHash0(final String s)
	{
		final int prime = 16777337;
		return 0x7FFFFFFF & RSHash(s, prime);
	}

	static final public int RSHash1(final String s)
	{
		final int prime = 16777337;
		return RSHash(s, prime);
	}

	static int brand()
	{
		return r.nextInt();
	}

	static int get_random(String what, String user, int per_user)
	{
		return RSHash1(user + what)
				^ arr[(0x7FFFFFFF & brand()) % (arr.length <= per_user ? arr.length - 1 : per_user)];
	}

	static int get_random0(String what, String user, int per_user, int tt_count)
	{
		return 0x7FFFFFFF & (get_random(what, user, per_user) % tt_count);
	}

	static String get_dev(String user, int per_user)
	{
		return "dev-" + get_random0("dev", user, per_user, 100000000);
	}

	static String get_re_acc(String user, int per_user)
	{
		return "re-" + get_random0("accre", user, per_user, 100000000);
	}

	static String get_se_acc(String user, int per_user)
	{
		return "se-" + user.substring(4) + "-" + r.nextInt(per_user);
	}

	static String get_ip(String user, int per_user)
	{
		int i = get_random("ip", user, per_user);

		if ((i & 0xFF000000) == 0)
			i |= 0x1000000;

		if ((i >>> 24) == 127)
			i += 0x1000000;

		return (0xff & (i >>> 24)) + "." + (0xff & (i >>> 16)) + "." + (0xff & (i >>> 8)) + "."
				+ (0xff & (i >>> 0));
	}

	static int[] arr = { 1073741831, 1073741993, 1073742073, 1073742169, 1073742233, 1073742343,
			1073742361, 1073742391, 1073742583, 1073742713 };
	static Random r = new Random();
	static int ip_per_user = 5;
	static int dev_per_user = 5;
	static int acc_per_user = 5;

	public static void puser(int i, String sessionPrefix) throws SQLException
	{
		TreeMap<String, Object> m = new TreeMap<String, Object>();
		String user = "user" + i / 2;

		m.put("TXNID", i % 2 == 0 ? "P01" : "P0201");
		m.put("DEVICEID", get_dev(user, dev_per_user));
		m.put("IPADDR", get_ip(user, ip_per_user));
		m.put("TXNTIME", date_tool.format(new Date()));

		m.put("balance", "1000");
		m.put("currencyType", "人民币");

		m.put("seAcc", get_se_acc(user, acc_per_user));
		m.put("payAccountName", "张福明");
		// m.put("payOpenNodeName", "宣武支行杨柳树分理处");

		m.put("recAcc", get_re_acc(user, acc_per_user));
		m.put("recAccountName", "李阜新");

		m.put("payAmount", "" + 1000 * (i % 20 + 1));

		m.put("USERID", user);
		m.put("SESSIONID", sessionPrefix + i / 2);
		m.put("TXNCODE", sessionPrefix + i);
		m.put("TXNSTATUS", i % 100 == 20 ? "0" : "1");

		// risk_eval_main.eval_pool().request(run_env.identification(new
		// io_env_test(m), false));
		// 风险确认
		risk_serv_init.init_pool().request(run_env.identification(new io_env_test(m), true));
		// m.remove("TXNSTATUS");
	}

	static AtomicLong m_tps = new AtomicLong(0);
	static AtomicLong m_pos = new AtomicLong(0);

	public static class thread implements Runnable
	{
		public String session_prefix;
		public int xx;
		public int beg;
		public int end;

		public void run()
		{
			final int ROW_COUNT = 1000000;
			java.util.List<Integer> list = new ArrayList<Integer>(ROW_COUNT);
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
							m_pos.set(i + j);
							puser(list.get(j), session_prefix);
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}

						m_tps.incrementAndGet();
					}
				}
			}

			System.out.println("THE END!");
		}
	}

	public static void main(String[] args) throws InterruptedException
	{
		clock ta = new clock();
		java.util.TreeSet<String> ip = new TreeSet<String>();
		java.util.TreeSet<String> dev = new TreeSet<String>();
		java.util.TreeSet<String> re = new TreeSet<String>();
		java.util.TreeSet<String> se = new TreeSet<String>();
		String user = "user9";
		for (int i = 0; i < 100; i++)
		{
			ip.add(get_ip(user, 5));
			dev.add(get_dev(user, 5));
			re.add(get_re_acc(user, 5));
			se.add(get_se_acc(user, 5));
		}

		System.out.print(ip + "\n" + dev + "\n" + re + "\n" + se);

		init();
		ta.pin("init spring");
		cache_init.init(new data_source());
		ta.pin("init cache");
		ta.reset();
		startup();
		ta.pin("init threads");

		clock t = new clock();

		thread th = new thread();

		th.session_prefix = "s2kw-";

		if (args.length > 0)
			th.session_prefix = args[0];

		// int xx = 1;
		th.xx = 2;
		if (args.length > 1)
			th.xx = Integer.parseInt(args[1]);
		th.beg = 0;
		if (args.length > 2)
			th.beg = Integer.parseInt(args[2]);
		// int end = 1000;
		th.end = th.beg + 2000000;
		if (args.length > 3)
		{
			th.end = (Integer.parseInt(args[3]));
		}

		new Thread(th).start();

		long tps;
		for (;;)
		{
			Thread.sleep(1000);
			//40000000
			t.pin("POS=" + m_pos.get() + "/" + (th.end) + ",TPS=" + (tps = m_tps.getAndSet(0)));

			if (tps != 0)
			{
				continue;
			}

//			if (++zero_count > 60 * 10)
//				break;
		}

//		shutdown();
	}
}
