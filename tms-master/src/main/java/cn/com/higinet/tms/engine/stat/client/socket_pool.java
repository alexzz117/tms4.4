package cn.com.higinet.tms.engine.stat.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.delay_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.comm.tmsapp;

public final class socket_pool implements Runnable
{
	static volatile socket_pool inst;
	static Logger log = LoggerFactory.getLogger(socket_pool.class);

	public static socket_pool Instance()
	{
		if (inst != null)
			return inst;

		synchronized (socket_pool.class)
		{
			if (inst == null)
				return inst = new socket_pool();
		}
		return inst;
	}

	public void start()
	{
		if (!tmsapp.is_cluster())
			return;

		for (int i = 0; i < MAX_SERV_COUNT; i++)
		{
			String server = tmsapp.get_config("tms.stat.server." + i, null);
			if (server != null)
			{
				int split_index = server.indexOf(':');
				if (split_index < 0 || split_index >= server.length())
				{
					log.error(":配置字符串错误:tms.stat.server-" + i + "=" + server);
					continue;
				}

				add_server(server.substring(0, split_index), Integer.parseInt(server
						.substring(split_index + 1)));
			}
		}

		Thread thr = new Thread(this);
		thr.setName("stat client socket pool");
		thr.setDaemon(true);
		thr.start();
	}

	public void shutdown()
	{
		close();
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

			if (ret != null && ret.charAt(0) == '+')
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
				socket_pool.Instance().check_online(m_below_server);
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
		final int port, id;
		final List<socket> sock_cache, sock_use;

		public server(int id, String ip, int port)
		{
			this.id = id;
			this.ip = ip;
			this.port = port;
			sock_cache = new ArrayList<socket>(32);
			sock_use = new ArrayList<socket>(32);
		}

		socket newSocket(boolean print_excetion)
		{
			try
			{
				Socket raw_sock = new Socket();
				raw_sock.connect(new InetSocketAddress(ip, port), 1000);
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

		public socket get(boolean print_exception)
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
				sock = newSocket(print_exception);

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
			socket sock = this.get(false);
			if (sock == null)
				return -1;

			return sock.ping();
		}

		@Override
		public String toString()
		{
			return "[" + this.ip + ":" + this.port + "]";
		}

		public int id()
		{
			return id;
		}
	}

	static final int MAX_SERV_COUNT = 32;
	static final int check_priod = 1000 * 10;
	final server[] m_serv;
	volatile int m_mask;

	private socket_pool()
	{
		m_mask = 0;
		m_serv = new server[MAX_SERV_COUNT];
	}

	public int add_server(String ip, int port)
	{
		int c = this.count();
		server s = new server(c, ip, port);
		m_serv[c++] = s;
		set_count(c);
		if (s.ping() < 0)
		{
			log.info("初始化统计服务器:" + s + "失败");
			return -1;
		}

		m_mask |= 1 << (c - 1);

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

	final public int mask()
	{
		return m_mask;
	}

	final public int count(int mask)
	{
		return mask >>> 24;
	}

	final public int count()
	{
		return count(m_mask);
	}

	final public void set_count(int c)
	{
		m_mask = (m_mask & 0x0FFFFFF) | (c << 24);
	}

	final int version(int mask)
	{
		return (mask >> 16) & 0xff;
	}

	final int version()
	{
		return version(m_mask);
	}

	private int get(int mask, String[] buff, socket[] sock, int[] error)
	{
		int epos = -1;
		for (int i = 0; i < buff.length; i++)
		{
			if (buff[i] == null)
				continue;

			if (version(mask) != version())
			{
				error[i] = 1;
				epos = i;
				break;
			}

			if (null == (sock[i] = m_serv[i].get(true)))
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

	// * 这段代码的逻辑：
	// 1、获取socket句柄，如果有一个出错，或者是连接池已经发生变化，将直接返回；
	// 这一部分的错误，可能会是某个服务器直接不可用，直接导致所有的分发是没有意义的；
	// 如果成功，则可能会有部分请求成功发送，此时我们已经拿到了服务器的句柄，所以这部分是有意义的；
	// 2、顺序发送请求数据，如果发生失败，会标记错误，然后向下一个服务器发送；
	// 3、顺序取回返回数据，并解析数据，在buff[]中返回
	// 4、关闭所有的socket句柄

	// * 请求端需要检查每个请求的返回值，如果有错误，需要重新发送错误的部分，返回结果数组中，非0值代表了错误代码

	public int[] send(int mask, String[] buff, clock c, List<Integer> time)
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
					log.warn(ret+"");
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
		int c = count();
		for (int i = 0; i < c; i++)
		{
			if (m_serv[i] == null)
				continue;
			m_serv[i].close();
		}
		m_mask = 0;
	}

	private void check_online(server s)
	{
		if ((m_mask & (1 << s.id)) == 0)
			return;

		synchronized (s)
		{
			if ((m_mask & (1 << s.id)) == 0)
				return;

			if (s.ping() >= 0)
				return;

			log.warn("检测到统计服务器:" + s.ip + ":" + s.port + "不接收新连接.");
			inc_version();
			m_mask &= ~(1 << s.id);
			s.close();
		}
	}

	private void check_online()
	{
		int c = count();
		for (int i = 0; i < c; i++)
		{
			if ((m_mask & (1 << i)) == 0)
				continue;

			check_online(m_serv[i]);
		}
	}

	private void check_offline()
	{
		int c = count();
		for (int i = 0; i < c; i++)
		{
			if ((m_mask & (1 << i)) != 0)
				continue;

			server s = m_serv[i];
			if (s.ping() < 0)
				continue;
			log.warn("检测到统计服务器:" + s + "恢复.");

			inc_version();
			m_mask |= 1 << s.id;
		}
	}

	private void inc_version()
	{
		int v = version();
		if (++v >= 100)
			v = 0;

		m_mask = (m_mask & 0xFF00FFFF) | (v << 16);

		log.info("检测到服务器变化，调整连接池版本为：" + v);
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
				check_online();
				check_offline();
			}
			catch (Exception e)
			{
			}
		}
	}
}
