package cn.com.higinet.tms35.manage.sign.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.com.higinet.tms35.manage.stat.service.impl.StatServiceImpl;

/**
 * Socket客户端工具类
 * 
 * @author lining
 */
public class SocketClient {
	private static Log logger = LogFactory.getLog(StatServiceImpl.class);
	
	private String ip;
	private int port;
	private Socket socket;
	private InputStream m_in;
	private OutputStream m_out;
	
	public SocketClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public SocketClient(String ip, int port, int connTimeout) throws Exception {
		try {
			this.ip = ip;
			this.port = port;
			this.socket = new Socket();
			this.socket.setReuseAddress(true);
			this.socket.connect(new InetSocketAddress(ip, port), connTimeout);
			this.m_in = this.socket.getInputStream();
			this.m_out = this.socket.getOutputStream();
		} catch (Exception e) {
			destroy();
			throw e;
		}
	}
	
	public void connect(int connTimeout) throws Exception {
		try {
			this.socket = new Socket();
			this.socket.setReuseAddress(true);
			this.socket.connect(new InetSocketAddress(this.ip, this.port), connTimeout);
			this.m_in = this.socket.getInputStream();
			this.m_out = this.socket.getOutputStream();
		} catch (Exception e) {
			destroy();
			throw e;
		}
	}

	public String read(int timeout, clock c) throws IOException {
		if (this.m_in == null)
			return null;

		byte[] buff = new byte[16 << 10];
		int len = read(this.m_in, buff, 0, 8, c, timeout);
		if (len != 8) {
			logger.error("[" + this.toString() + "]socket读取报文头数据错误,无法获取报文头前8个字节.获取内容：" + new String(buff, 0, len, "UTF-8"));
			return null;
		}
		int buff_len = 32 + Integer.parseInt(new String(buff, 0, 8), 10);
		len = read(this.m_in, buff, 8, buff_len - 8, c, timeout);
		if (len != buff_len - 8) {
			logger.error("[" + this.toString() + "]socket无法正确读取报文数据,当前报文头长度:" + buff_len + ",返回内容:" + new String(buff, 0, len, "UTF-8"));
			return null;
		}
		return new String(buff, 0, buff_len, "UTF-8");
	}

	private int read(InputStream stmi, byte[] buff, int pos, int len, clock c, int timeout) throws IOException {
		int left = c.left(timeout);
		if (left <= 0) {
			return 0;
		}

		this.setSoTimeout(left);
		int ret = 0;
		for (; ret < len;) {
			int b = stmi.read(buff, pos + ret, len - ret);
			if (b < 0)
				return ret;

			if ((left = c.left(timeout)) <= 0)
				return ret;

			this.setSoTimeout(left);
			ret += b;
		}
		return ret;
	}

	public void write(char c) throws IOException {
		if (this.m_out == null)
			return;
		this.m_out.write(c);
	}

	public void write(String s) throws IOException {
		if (this.m_out == null)
			return;
		this.m_out.write(s.getBytes("UTF-8"));
	}

	public void flush() throws IOException {
		if (this.m_out == null)
			return;
		this.m_out.flush();
	}

	private final void destroy() {
		try {
			if (this.socket != null)
				this.socket.close();
		} catch (Exception e) {
			this.socket = null;
		}
		try {
			if (this.m_in != null)
				this.m_in.close();
		} catch (Exception e) {
			this.m_in = null;
		}
		try {
			if (this.m_out != null)
				this.m_out.close();
		} catch (Exception e) {
			this.m_out = null;
		}
	}

	public final void close() {
		this.destroy();
	}

	public int getSoTimeout() {
		try {
			return this.socket.getSoTimeout();
		} catch (SocketException e) {
			return -1;
		}
	}

	public void setSoTimeout(int tmout) {
		try {
			this.socket.setSoTimeout(tmout);
		} catch (SocketException e) {
		}
	}

	public boolean isClosed() {
		return this.socket == null || this.socket.isClosed();
	}

	public String toString() {
		return new StringBuilder().append("ip: ").append(this.ip).append(", port: ").append(this.port).toString();
	}

	public String getIp() {
		return this.ip;
	}
}