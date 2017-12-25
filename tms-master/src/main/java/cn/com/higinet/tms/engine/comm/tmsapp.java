package cn.com.higinet.tms.engine.comm;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.dao.dao_serv_live;
import cn.com.higinet.tms.engine.core.dao.dao_serv_live.db_server;

public final class tmsapp
{
	static Logger log = LoggerFactory.getLogger(tmsapp.class);

	private static Properties config;
	private static byte[] serv_ip_byte;
	private static String serv_ip;
	private static List<String> serv_ips;

	final static long nanoMs()
	{
		return System.nanoTime() / 1000000;
	}
	
	static
	{
		init();
	}
	
	private static void init()
	{
		try
		{
			Inet4Address myip = (Inet4Address) Inet4Address.getLocalHost();
			//if (myip.isLoopbackAddress()) throw new Error("无法获取本机IP地址，请正确配置/etc/hosts文件");
			serv_ip = myip.getHostAddress();
			serv_ip_byte = myip.getAddress();
			serv_ips = new ArrayList<String>(8);
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress ip = null;
			while (allNetInterfaces.hasMoreElements())
			{
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				if (netInterface.isVirtual())
					continue;
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements())
				{
					ip = (InetAddress) addresses.nextElement();
					if (ip.isLinkLocalAddress())
						continue;
					if (ip.isLoopbackAddress())
						continue;

					if (ip != null && ip instanceof Inet4Address)
						serv_ips.add(ip.getHostAddress());
				}
			}
		}
		catch (UnknownHostException e) {
			log.error("", e);
			throw new Error("无法获取本机IP地址，请正确配置/etc/hosts文件");
		}
		catch (SocketException e) {
			log.error("", e);
			throw new Error("获取本机所有网络接口时，发生I/O错误");
		}
	}

	final static public void live_notify(boolean is_stat)
	{
		int ping_delay = tmsapp.get_config("server.live.timeout", 30);
		ping_delay *= 1000;

		dao_serv_live dsl = new dao_serv_live(new db_server(tmsapp.get_serv_ip(), //
				is_stat ? tmsapp.get_config("tms.stat.port", 4000) //
						: tmsapp.get_config("tms.server.port", 8000),//
				is_stat ? 2 : 1, //
				ping_delay));

		ping_delay *= 0.9;
		long last_ping = nanoMs();
		boolean is_first = true;//add lining 20150413 保证系统启动后马上注册服务
		for (;;)
		{
			while (!is_first && nanoMs() - last_ping < ping_delay)
				delay_tool.delay(1000);
			
			last_ping = nanoMs();
			if (is_first)
				is_first = false;
			dsl.ping();
		}
	}

	final static public void start_live_deamon(final boolean is_stat)
	{
		Thread thr = new Thread(new Runnable()
		{
			public void run()
			{
				tmsapp.live_notify(is_stat);
			}
		});

		thr.setDaemon(true);
		thr.setName("process live");
		thr.start();
	}

	final static public void set_config(Properties config)
	{
		tmsapp.config = config;
	}

	final static public String get_config(String key, String defaultValue)
	{
		return get_config(key, defaultValue, true);
	}

	final static public String get_config(String key, String defaultValue, boolean print_console)
	{
		String ret = config == null ? defaultValue : config.getProperty(key, defaultValue);

		if (print_console)
			log.info(key + "=" + ret);

		return ret;
	}

	final static public int get_config(String key, int defaultValue)
	{
		return get_config(key, defaultValue, true);
	}

	final static public int get_config(String key, int defaultValue, boolean print_console)
	{
		String v = get_config(key, "" + defaultValue, print_console);
		return Integer.parseInt(v);
	}

	public static String get_serv_id(boolean is_stat)
	{
		byte[] ip = get_serv_ip_byte();
		int port = is_stat ? tmsapp.get_config("tms.stat.port", 4000) //
				: tmsapp.get_config("tms.server.port", 8000);//

		return String.format("%02X%02X%02X%02X%02X%02X", ip[0], ip[1], ip[2], ip[3], port >>> 8,
				port & 0xff);
	}

	public static String get_serv_id2(boolean is_stat)
	{
		byte[] ip = get_serv_ip_byte();
		int port = is_stat ? tmsapp.get_config("tms.stat.port", 4000) //
				: tmsapp.get_config("tms.server.port", 8000);//

		return String.format("%d.%d.%d.%d:%d", 0xFF&ip[0], 0xFF&ip[1], 0xFF&ip[2], 0xFF&ip[3], 0xFFFF&port);
	}

	public static List<String> get_ip() throws SocketException
	{
		return serv_ips;
	}

	public static String get_serv_ip()
	{
		return serv_ip;
	}

	public static byte[] get_serv_ip_byte()
	{
		return serv_ip_byte;
	}

	/**
	 * @param args
	 * @throws SocketException
	 */
	public static void main(String[] args) throws SocketException
	{
		System.out.print(get_serv_id(false));
		long start = nanoMs() / 1000;

		for (int i = 0; i < 1000; i++)
		{
			System.out.println("tick:" + (nanoMs() / 1000 - start));
			delay_tool.delay(1000);
		}
	}

	public static int get_serv_count()
	{
		return 1;
	}

	public static boolean is_cluster()
	{
		return get_config("tms.cluster", 0) != 0;
	}

	public static void set_config(String string, int i)
	{
		config.put(string, Integer.toString(i));
	}

	public static void set_config(String string, String s)
	{
		config.put(string, s);
	}
}
