package cn.com.higinet.tms.engine.stat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.base.util.Base64Util;
import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.delay_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;

public final class x_socket_pool implements Runnable
{
	static volatile x_socket_pool inst;
	static Logger log = LoggerFactory.getLogger(x_socket_pool.class);
	static db_mutex lock = new db_mutex();
	static final String local_serv_id = tmsapp.get_serv_id2(false);
	static final int conn_timeout_ms = 1000;
	static final int ping_timeout_ms = 500;

	public static x_socket_pool Instance()
	{
		if (inst != null)
			return inst;

		synchronized (x_socket_pool.class)
		{
			if (inst == null)
			{
				inst = new x_socket_pool();
				return inst;
			}
		}
		return inst;
	}

	public void load_cluster_config()
	{
		String t = "tms_run_stat";
		String db = tmsapp.get_config("tms.jdbc.url", null);
		String drv = tmsapp.get_config("tms.jdbc.driverClassName", null);
		String u = tmsapp.get_config("tms.jdbc.username", null);
		String p = Base64Util.base64Decode(tmsapp.get_config("tms.jdbc.password", null));

		for (int i = 0; i < 32; i++)
		{
			String server = tmsapp.get_config("tms.stat.server." + i, null, true);
			if (server == null)
				break;

			++m_count;
			this.m_config[i] = new serv_config(//
					i, //
					tmsapp.get_config("tms.stat.server." + i + ".db", db), //
					tmsapp.get_config("tms.stat.server." + i + ".db.driver", drv), //
					tmsapp.get_config("tms.stat.server." + i + ".db.tabname", t), //
					tmsapp.get_config("tms.stat.server." + i + ".db.username", u), //
					tmsapp.get_config("tms.stat.server." + i + ".db.password", p) //
			);
		}

		log.info("集群统计服务器数量（CLUSTER）：" + m_count);
	}

	public void start()
	{
		if (!tmsapp.is_cluster())
			return;

		load_cluster_config();

		for (int i = 0; i < 64; i++)
		{
			String server = tmsapp.get_config("tms.stat.server." + i, null, false);
			if (server == null)
				continue;

			int split_index = server.indexOf(':');
			if (split_index < 0 || split_index >= server.length())
			{
				String e = "配置字符串错误:tms.stat.server-" + i + "=" + server;
				log.error(e);
				throw new Error(e);
			}

			add_server(i, server.substring(0, split_index), Integer.parseInt(server
					.substring(split_index + 1)));
		}

		if (!lock.lock())
			throw new Error("初始化统计服务器池时发现死锁！");

		if (lock.locked)
		{
			try
			{
				this.m_version = -lock.status;
				this.m_db_server.load();
				if (!m_db_server.match(m_serv))
				{
					m_db_server.from_local(m_serv);
					m_db_server.save(local_serv_id);

					this.m_version++;
				}
				else
				{
					this.from_db_server();
				}

				for (int i = 0; i < 32; i++)
				{
					if (m_serv[i] != null)
					{
						init_server(i);
					}
				}

				log.warn(log_info_cluster());
			}
			finally
			{
				lock.unlock(this.m_version);
			}
		}

		Thread thr = new Thread(this);
		thr.setName("stat client socket pool");
		thr.setDaemon(true);
		thr.start();
	}

	String log_info_cluster()
	{
		java.io.ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		java.io.PrintStream ps = new PrintStream(bos);

		ps.println("pool version=" + this.m_version);
		ps.println("server count(cluster)=" + this.count());
		for (int i = 0; i < 32; i++)
		{
			if(m_serv[i]==null)
				continue;
			ps.println("server" + i + "=" + m_serv[i].toString());
		}

		ps.flush();
		ps.close();

		return bos.toString();
	}

	int find_server(String ip_port)
	{
		for (int i = 0; i < 64; i++)
		{
			if (m_serv[i] == null)
				continue;
			if (ip_port.equals(m_serv[i].toString()))
				return i;
		}

		return -1;
	}

	boolean from_db_server()
	{
		boolean ret = false;
		String[] d = m_db_server.db_server;
		server[] s = new server[64];
		long mask = 0;
		for (int i = 0; i < 64; i++)
		{
			if (d[i] == null)
				continue;
			int id = find_server(d[i]);
			ret = (id != i) || ret;

			if (id == -1)
			{
				int split_index = d[i].indexOf(':');
				s[id] = new server(d[i].substring(0, split_index), Integer.parseInt(d[i]
						.substring(split_index) + 1));
			}
			else
			{
				s[i] = m_serv[id];
				if (is_online(id))
					mask |= 1L << i;
			}
		}

		m_mask_online = mask;
		m_serv = s;

		return ret;
	}

	private boolean init_server(int id)
	{
		socket sock = m_serv[id].get(conn_timeout_ms, false);
		if (sock == null)
			return false;
		try
		{
			sock.write("INIT\n");
			sock.write(count());
			sock.write('\n');
			sock.write(id);
			sock.write('\n');
			sock.write(m_config[id].db);
			sock.write('\n');
			sock.write(m_config[id].drv);
			sock.write('\n');
			sock.write(m_config[id].tab_name);
			sock.write('\n');
			sock.write(m_config[id].user);
			sock.write('\n');
			sock.write(m_config[id].passwd);
			sock.write('\n');
			sock.flush();
			String ret = sock.readLine();
			if (ret != null && ret.charAt(0) == '+')
				return true;
		}
		catch (IOException e)
		{
			log.error("Init stat server error.", e);
			return false;
		}
		finally
		{
			sock.close(false);
		}
		return false;
	}

	public void shutdown()
	{
		close();
	}

	static class serv_config
	{
		public serv_config(int id, String db_link, String drv, String tab_name, String u, String p)
		{
			this.db = db_link;
			this.drv = drv;
			this.tab_name = tab_name;
			this.user = u;
			this.passwd = p;
			this.hashid = id;
		}

		String drv, db, tab_name, user, passwd;// 数据库连接属性
		int hashid; // 统计横向分割时的hashid,统计服务器使用此hashid初始化影子缓存
	}

	final int check_priod = 1000 * 10;

	final serv_config[] m_config;	//32个在线服务器的配置
	server[] m_serv;				//所有64个服务器
	final x_server_db m_db_server;	//一致性存储
	volatile long m_mask_online;	//所有可用的服务器，包括备份服务器
	volatile long m_version;
	int m_count;					//在线服务器的数量

	private x_socket_pool()
	{
		m_db_server = new x_server_db();
		m_config = new serv_config[32];
		m_serv = new server[64];
		m_mask_online = 0;
	}

	public int add_server(int id, String ip, int port)
	{
		server s = new server(ip, port);
		m_serv[id] = s;

		if (s.ping() < 0)
		{
			log.info("初始化统计服务器:" + s + "失败");
			return -1;
		}

		m_mask_online |= 1L << id;//标记该服务器在线
		log.info("初始化统计服务器:" + s + "成功");
		return 0;
	}

	private final static String[] err_info = new String[] { //
	"无错误", "服务器连接状态变化", //
			"当前服务器不可用", "IO错误",//
			"服务器过早关闭连接", "服务器返回错误信息" };

	public String error_info(int err_id)
	{
		if (err_id >= err_info.length || err_id < 0)
			return "错误码超出定义范围，或者连接过程发生错误";
		return err_info[err_id];
	}

	final public int count()
	{
		return m_count;
	}

	final long version()
	{
		return m_version;
	}

	private int get(long version, String[] buff, socket[] sock, int[] error)
	{
		int epos = -1;
		for (int i = 0; i < buff.length; i++)
		{
			if (buff[i] == null)
				continue;

			if (version != version())
			{
				error[i] = 1;
				epos = i;
				break;
			}

			if (null == (sock[i] = m_serv[i].get(conn_timeout_ms, true)))
			{
				error[i] = 2;
				epos = i;
				break;
			}
		}

		if (epos != -1)
		{
			for (int i = 0; i < epos; i++)
			{
				if (sock[i] != null)
					sock[i].close(false);
			}

			return error[epos];
		}

		return 0;
	}

	public int[] send(long mask, String[] buff, clock c, List<Integer> time)
	{
		socket[] sock = new socket[buff.length];
		int[] error = new int[buff.length];
		Arrays.fill(error, -1);
		int ret = get(mask, buff, sock, error);
		if (ret != 0)
			return error;

		Arrays.fill(error, 0);
		if (time != null)
			time.add((int) c.count_ms());
		for (int i = 0; i < buff.length; i++)
		{
			if (buff[i] == null)
				continue;
			try
			{
				sock[i].write(buff[i]);
				sock[i].flush();
			}
			catch (IOException e)
			{
				error[i] = 3;
			}
		}

		if (time != null)
			time.add((int) c.count_ms());
		StringBuffer sb = new StringBuffer(1024);
		for (int i = 0; i < buff.length; i++)
		{
			if (buff[i] == null || error[i] != 0)
				continue;
			sb.setLength(0);

			try
			{
				String s = sock[i].readLine();
				if (s == null)
				{
					error[i] = 4;
					continue;
				}

				if (s.charAt(0) != '+')
				{
					log.warn("{}",ret);
					error[i] = 5;
					continue;
				}

				s = sock[i].readLine();
				if (s == null)
				{
					error[i] = 4;
					continue;
				}

				int count = Integer.parseInt(s);
				for (int row = 0; row < count; row++)
				{
					s = sock[i].readLine();
					if (s == null)
					{
						error[i] = 4;
						break;
					}

					sb.append(s).append('\n');
				}
				buff[i] = sb.toString();
			}
			catch (IOException e)
			{
				error[i] = 3;
			}
		}

		if (time != null)
			time.add((int) c.count_ms());
		for (int i = 0; i < sock.length; i++)
		{
			if (sock[i] == null)
				continue;
			sock[i].close(error[i] != 0);
		}

		return error;
	}

	public void close()
	{
		for (int i = 0; i < 64; i++)
		{
			if (m_serv[i] == null)
				continue;
			m_serv[i].close();
		}
		m_mask_online = 0;
	}

	// 挑选备份服务器替代s1
	private void replace_server(int s1)
	{
		int s2 = 32;
		for (; s2 < 64; s2++)
		{
			if ((m_mask_online & (1L << s2)) != 0 && m_serv[s2].ping() >= 0)
				break;
		}

		if (s2 == 64)
			throw new Error("没有找到可以替换的服务器，系统将停止！");

		log.warn("替换不可用的统计服务器：" + m_serv[s1] + "----->" + m_serv[s2]);

		server tmp = m_serv[s1];
		m_serv[s1] = m_serv[s2];
		m_serv[s2] = tmp;

		this.m_mask_online ^= 1L << s2;
		init_server(s1);
		m_serv[s2].close();
	}

	synchronized private boolean valid_pool()
	{
		if (!lock.lock())
			return false;

		this.m_version = -lock.status;
		try
		{
			this.m_db_server.load();
			if (!m_db_server.match(m_serv))
			{
				log.error(lock.local_serv_id + ":与数据库现有的服务器定义不匹配。");
			}
			for (int i = 0; i < 32; i++)
			{
				if (m_serv[i] == null)
					continue;
				if (m_serv[i].ping() >= 0)
				{
					continue;
				}

				replace_server(i);
				m_version++;
			}
			this.m_db_server.from_local(m_serv);
			this.m_db_server.save(local_serv_id);

			if (this.from_db_server())
			{
				log.warn("根据服务器存储的统计服务信息调整本地服务：");
				log.warn(log_info_cluster());
			}
		}
		finally
		{
			lock.unlock(m_version);
		}

		return true;
	}

	synchronized private void check_cluster()
	{
		for (int i = 0; i < 32; i++)
		{
			if (m_serv[i] == null)
				continue;

			if (m_serv[i].ping() < 0)
			{
				valid_pool();
				break;
			}
		}
	}

	private boolean is_online(int i)
	{
		return 0 != (m_mask_online & (1L << i));
	}

	private boolean is_offline(int i)
	{
		return !is_online(i);
	}

	synchronized private void check_backup()
	{
		for (int i = 32; i < 64; i++)
		{
			if (m_serv[i] == null)
				continue;

			if (m_serv[i].ping() < 0)
			{
				if (is_online(i))
					log.warn("服务器:" + m_serv[i].toString() + "变为不可连接。");

				m_mask_online &= ~1L ^ (1L << i);
			}
			else
			{
				if (is_offline(i))
					log.warn("服务器:" + m_serv[i].toString() + "变为可连接。");

				m_mask_online |= 1L << i;
			}
		}
	}

	public void run()
	{
		long last_check_time = 0;
		for (;;)
		{
			while (tm_tool.lctm_ms() < last_check_time + check_priod)
				delay_tool.delay(100);
			last_check_time = tm_tool.lctm_ms();

			try
			{
				check_cluster();
				check_backup();
			}
			catch (Exception e)
			{
			}
		}
	}

	public static final class socket
	{
		private Socket m_raw_socket;
		private BufferedReader m_in;
		private BufferedWriter m_out;
		private server m_below_server;
		private long m_last_use_time;

		private socket(Socket mRawSocket, server below_server) throws IOException
		{
			try
			{
				m_below_server = below_server;
				m_raw_socket = mRawSocket;
				m_raw_socket.setTcpNoDelay(true);
				// m_raw_socket.setSendBufferSize(40960);
				// m_raw_socket.setReceiveBufferSize(40960);
				m_in = new BufferedReader(new InputStreamReader(m_raw_socket.getInputStream(),
						"UTF-8"));
				m_out = new BufferedWriter(new OutputStreamWriter(m_raw_socket.getOutputStream(),
						"UTF-8"));
				m_last_use_time = tm_tool.lctm_ms();
			}
			catch (IOException e)
			{
				destroy();
				throw e;
			}
		}

		String readLine() throws IOException
		{
			if (m_in == null)
				return null;
			return m_in.readLine();
		}

		public void write(char c) throws IOException
		{
			if (m_out == null)
				return;
			m_out.write(c);
		}

		void write(String s) throws IOException
		{
			if (m_out == null)
				return;
			m_out.write(s);
		}

		void write(int s) throws IOException
		{
			if (m_out == null)
				return;
			m_out.write(Integer.toString(s));
		}

		void flush() throws IOException
		{
			if (m_out == null)
				return;
			m_out.flush();
		}

		public int ping()
		{
			if (is_closed())
				return -1;

			String ret;
			try
			{
				write("PING\n");
				flush();
				ret = readLine();
			}
			catch (IOException e)
			{
				this.destroy();
				return -1;
			}

			close(false);

			//if (ret != null && ret.charAt(0) == '+')
			if (null!=ret && ret.trim().length()>0 && ret.charAt(0) == '+')//added by wujw 2015/10/03
				return (int) (Long.parseLong(ret.substring(1)) / 1000);

			return -1;
		}

		private void destroy()
		{
			try
			{
				this.m_raw_socket.close();
			}
			catch (IOException e)
			{
			}
			try
			{
				this.m_in.close();
			}
			catch (IOException e)
			{
			}

			try
			{
				this.m_out.close();
			}
			catch (IOException e)
			{
			}
		}

		public void close(boolean has_error)
		{
			if (has_error)
			{
				this.destroy();
				x_socket_pool.Instance().valid_pool();
			}
			m_below_server.retrieve(this);
		}

		public int getSoTimeout()
		{
			try
			{
				return m_raw_socket.getSoTimeout();
			}
			catch (SocketException e)
			{
				return -1;
			}
		}

		public void setSoTimeout(int tmout)
		{
			try
			{
				m_raw_socket.setSoTimeout(tmout);
			}
			catch (SocketException e)
			{
			}
		}

		public boolean is_closed()
		{
			return m_raw_socket == null || m_raw_socket.isClosed();
		}

		public boolean last_use_timeout(int time_out)
		{
			return System.currentTimeMillis() - m_last_use_time > time_out;
		}

		public void reset_last_use_time()
		{
			m_last_use_time = System.currentTimeMillis();
		}

	}

	public static final class server
	{
		final String ip;
		final int port;
		final List<socket> sock_cache, sock_use;

		public server(String ip, int port)
		{
			this.ip = ip;
			this.port = port;
			sock_cache = new ArrayList<socket>(32);
			sock_use = new ArrayList<socket>(32);
		}

		socket newSocket(int timeout, boolean print_excetion)
		{
			try
			{
				Socket raw_sock = new Socket();
				raw_sock.connect(new InetSocketAddress(ip, port), timeout);
				return new socket(raw_sock, this);
			}
			catch (UnknownHostException e)
			{
				if (print_excetion)
					log.error(null, e);
			}
			catch (IOException e)
			{
				if (print_excetion)
					log.error(null, e);
			}
			return null;
		}

		public socket get(int timeout, boolean print_exception)
		{
			socket sock = null;
			synchronized (this)
			{
				while (sock_cache.size() > 0)
				{
					sock = sock_cache.remove(sock_cache.size() - 1);
					if (sock == null)
						continue;
					if (sock.is_closed())
					{
						sock.destroy();
						sock = null;
						continue;
					}
					break;
				}
			}

			if (sock == null)
				sock = newSocket(timeout, print_exception);

			if (sock != null)
			{
				synchronized (this)
				{
					sock_use.add(sock);
				}
			}

			return sock;
		}

		synchronized public void retrieve(socket sock)
		{
			if (!sock.is_closed())
				sock_cache.add(sock);

			sock_use.remove(sock);
		}

		synchronized public void close()
		{
			for (socket s : sock_cache)
				s.destroy();
			sock_cache.clear();

			for (socket s : sock_use)
				s.destroy();
			sock_use.clear();
		}

		public int ping()
		{
			socket sock = this.get(ping_timeout_ms, false);
			if (sock == null)
				return -1;

			return sock.ping();
		}

		@Override
		public String toString()
		{
			return this.ip + ":" + this.port;
		}
	}
}
