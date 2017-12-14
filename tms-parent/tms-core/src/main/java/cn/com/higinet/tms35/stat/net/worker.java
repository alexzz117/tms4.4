package cn.com.higinet.tms35.stat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import cn.com.higinet.tms35.comm.delay_tool;

public class worker implements Runnable
{
	static Logger log = Logger.getLogger(net_main.class);
	static Map<String, cmd> g_cmd_disp = new TreeMap<String, cmd>();
	static
	{
		g_cmd_disp.put("CLOSE", new cmd_close());
		g_cmd_disp.put("PING", new cmd_ping());
		g_cmd_disp.put("STAT", new cmd_stat());
		g_cmd_disp.put("QUERY", new cmd_stat_query());
		g_cmd_disp.put("SET", new cmd_setup());
		g_cmd_disp.put("SHUTDOWN", new cmd_shutdown());
		g_cmd_disp.put("INIT", new cmd_init());
		g_cmd_disp.put("FILTER", new cmd_filter());
	}
	Socket m_raw_socket;
	BufferedReader m_in;
	BufferedWriter m_out;
	private AtomicBoolean m_running, m_shuting;

	String name;

	public worker(Socket sock) throws IOException
	{
		m_running = new AtomicBoolean(true);
		m_shuting = new AtomicBoolean(false);
		m_raw_socket = sock;
		try
		{
			m_in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
			m_out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
		}
		catch (IOException e)
		{
			close();
			throw e;
		}
		name = m_raw_socket.toString();
		Thread thr = new Thread(this);
		thr.setName("socket io.");
		thr.start();
	}

	public String toString()
	{
		return name;
	}

	private void close()
	{
		try
		{
			if (m_in != null)
			{
				m_in.close();
			}
		}
		catch (IOException e)
		{
		}
		m_in = null;
		try
		{
			if (m_out != null)
				m_out.close();
		}
		catch (IOException e)
		{
		}
		m_out = null;
		try
		{
			if (m_raw_socket != null)
			{
				m_raw_socket.close();
				m_raw_socket = null;
			}
		}
		catch (IOException e)
		{
		}
	}

	public void run()
	{
		try
		{
			for (; m_running.get();)
			{
				int cmd_result = process_cmd();
				if (cmd_result == 0)
				{
					m_out.flush();
					continue;
				}

				if (cmd_result == -1)
					log.info("client closed:"+m_raw_socket);
				else
					log.info("other result:" + cmd_result);

				break;
			}
		}
		catch (Exception e)
		{
			if (!m_raw_socket.isClosed())
				log.error(null, e);
		}

		close();
		m_shuting.set(true);
		net_main.worker_closed(this);
	}

	private int process_cmd() throws IOException
	{
		String line = null;
		do
		{
			line = read_cmd();
			if (line == null)
				return -1;
		} while (line.length() == 0);

		java.util.StringTokenizer st = new StringTokenizer(line, " ");
		String cmd = st.nextToken().toUpperCase();
		if (cmd.length() > 2 && cmd.charAt(0) == 65533 && cmd.charAt(1) == 3)
			cmd = cmd.substring(2);
		cmd disp = g_cmd_disp.get(cmd);
		if (disp == null)
			throw new IOException("错误的命令:" + line);
		if (st.countTokens() > 0)
		{
			String[] param = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				param[i] = st.nextToken();
			return disp.on_cmd(param, m_in, m_out);
		}
		else
			return disp.on_cmd(null, m_in, m_out);
	}

	private String read_cmd() throws IOException
	{
		for (; m_running.get();)
		{
			try
			{
				return m_in.readLine();
			}
			catch (SocketTimeoutException e)
			{
				if (!m_running.get())
					return null;
			}
			catch (IOException e)
			{
				throw e;
			}
		}

		return null;
	}

	public void shutdown()
	{
		m_running.set(false);
	}

	public void wait_shutdown()
	{
		while (!m_shuting.get())
			delay_tool.delay(10);
	}
}
