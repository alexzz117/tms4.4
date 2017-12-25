package cn.com.higinet.tms.engine.comm;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * socket客户端
 * @author yanghui
 * @date 2011-7-26
 * 
 * @version 2.0
 */
public class SocketClient {
	private Socket sock = null;
	private DataOutputStream dos = null;
	private InputStream dis = null;
	private String ip;
	private int port;
	
	public SocketClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * 发送缓存刷新消息到缓存刷新服务，产生异常后抛出，通知上层
	 * @param b
	 * @param ws
	 * @throws Exception
	 */
	public void send(byte[] b, byte[] ws) throws Exception{
		try {
			//发送信息
			sock = new Socket(this.ip, Integer.valueOf(this.port));
			sock.setSoTimeout(3000);
			dos = new DataOutputStream(sock.getOutputStream());
			dos.write(b);
			dos.write(ws);
			dos.flush();
			
			//返回信息
			dis = new DataInputStream(new BufferedInputStream(
					sock.getInputStream()));
			byte[] ret = new byte[2048];
			int len1 = dis.read(ret);
			if(len1 < 0){
				len1 = 10;
			}
			String retMess = new String(ret, 0, len1, "UTF-8");
			//System.out.println("retStr="+retStr);
			String retCode = retMess.substring(24, 32);
			String bodyStr = retMess.substring(32);

			String errorInfo = "";
			if(bodyStr != null && bodyStr.length() > 0) {
				Document doc = DocumentHelper.parseText(bodyStr);
				Element root = doc.getRootElement(); 
				List<Element> iter = root.elements();
				for (Element element : iter) {
					String e_name = element.getName();
					if(e_name.equals("ERROR")) {
						errorInfo = element.getText();
					}
				}
			}
			
			//System.out.println("retStr.trim()="+retStr.trim());
			if (!"000000".equals(retCode.trim())){
				/*if(errorInfo.length() == 0) {
					errorInfo = MapUtil.getString(StaticParameters.ERROR, retCode.trim());
				}*/
				throw new Exception(errorInfo);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (sock != null)
					sock.close();
				if (dis != null)
					dis.close();
			} catch (IOException e) {
				throw e;
			}
		}
	}
	
	/**
	 * 发送
	 * @param b
	 * @param ws
	 * @throws Exception
	 */
	public String sendMsg(byte[] b, byte[] ws) throws Exception{
		try {
			//发送信息
			sock = new Socket(this.ip, Integer.valueOf(this.port));
			sock.setSoTimeout(3000);
			dos = new DataOutputStream(sock.getOutputStream());
			dos.write(b);
			dos.write(ws);
			dos.flush();
			
			//返回信息
			BufferedReader dis = new BufferedReader(new InputStreamReader(
					sock.getInputStream(),"UTF-8"));
			
			String retMess = "";
			String str = "";
		    while ((str = dis.readLine()) != null) {
		    	retMess +=str;
		    }
			/*byte[] ret = new byte[2048];
			int len1 = dis.read(ret);
			if(len1 < 0){
				len1 = 10;
			}
			String retMess = new String(ret, 0, len1, "UTF-8");*/
			return retMess;
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (sock != null)
					sock.close();
				if (dis != null)
					dis.close();
			} catch (IOException e) {
				throw e;
			}
		}
	}
}
