package cn.com.higinet.sf0718;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * 调用短信服务的客户端实现
 * 
 * @author wujinwang
 * */

public class NPP101Socket {

	/**
	 * 报文头长度
	 */
	private final int headLength = 32;

	/**
	 * 短信的IP地址,通过配置tms-service.xml配置
	 * */
	// public String ip = "10.118.244.180";
	private String ip = "127.0.0.1";
	// private String ip = "172.16.133.203";

	/**
	 * 短信的端口，通过配置tms-service.xml配置
	 * */
	private int port = 8000;

	/**
	 * 短信的连接超时，通过配置tms-service.xml配置
	 * */
	private int timeOut = 2000;

	public boolean sendMessage(Map<String, Object> map) {
		boolean b = false;
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		String msgBody = xml
				+ "<Message><SYNCFLAG>1</SYNCFLAG><TXNCODE>d7a86a7fe50449b2a21a1514c771baa1</TXNCODE><TXNTIME>2016-07-14 18:48:08</TXNTIME><TXNID>NPP101</TXNID><CHANCODE>CH01</CHANCODE><USERID>20108945041987</USERID><SESSIONID>16k28vlal8aum!1468493283375</SESSIONID><IPADDR>61.148.244.77</IPADDR>"
				+ "<DEVICEFINGER>1|2001:352204062284031|2002:460019121503926|2003:AC:36:13:CF:1B:10|2004:4facd2a2|2006:8fbb91df18fe6a17|2009:1|2010:true|2013:4.589389937671455|2014:720*1280|2015:SM-N7506V|2016:hlltezn|2018:android|2019:4.3|2020:zh|2021:com.sfpay.mobile|2022:V2.1.2|2023:322d4985062b16|2024:18|2025:1.20 GB|2026:1.83 GB|2027:95|2028:N7506VZNUAOL1|2029:3.4.0-2576597|2030:CN|2031:TimeZone   GMT+08:00Timezon id :: Asia/Shanghai|2032:192.168.1.107|2033:8.8.8.8|2034:46001|2035:460|2036:01|2037:0c:72:2c:cd:9b:ea|2039:L&E|2040:0.0|2042:0.0|2044:40.008784|2045:116.278501|2046:0.0|2051:12.51 GB|2052:ARMv7 Processor rev 3 (v7l) 0|2053:BOARD: MSM8928, BOOTLOADER: N7506VZNUAOL1|2054:hllte|2055:wifi|2057:89860115811016362405</DEVICEFINGER><DEVICETOKEN>88e04d259db54106b4b994cf9c0b9218</DEVICETOKEN><DISPATCH>20108945041987</DISPATCH><AUTHLEVEL>MIDD_AUTH</AUTHLEVEL><LOGINTYPE>02</LOGINTYPE><BUSYSRC>APP</BUSYSRC><LONGITUDE>116.299622</LONGITUDE><LATITUDE>39.981325</LATITUDE></Message>";
		// String msgBody = xml + "";
		int bodyLength = 0;
		try {
			bodyLength = msgBody.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String msgHead = this.getReqMsgHead(bodyLength);
		System.out.println("---msgHead--:" + msgHead);

		String msg = msgHead + msgBody;
		System.out.println("---msg--:" + msg);

		String response = callServer(ip, port, msg, timeOut);

		return b;
	}

	/**
	 * 组装报文头
	 * 
	 * @param actionCode
	 *            操作代码
	 * @param bodyLength
	 *            报文体长度
	 * @return
	 */
	public String getReqMsgHead(int bodyLength) {
		// 00000329TMS 0002XML
		StringBuffer sb = new StringBuffer();
		String actionCode = "0002";
		String len = String.valueOf(bodyLength);
		len = "00000000".substring(len.length()) + len;
		sb.append(len);// 报文体长度
		sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
		sb.append(actionCode);// 交易号
		sb.append("XML").append(" ");// 报文体类型
		sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		// sb.append("20180989981961                  ");
		return sb.toString();
	}

	/**
	 * 向TMS送报文
	 * 
	 * @param message
	 * @return
	 */
	private String callServer(String ip, int port, String message, int timeOut) {
		String responseBody = "";
		Socket socket = null;
		DataOutputStream dos = null;
		InputStream dis = null;

		try {
			// log.debug("SMS socket start.");
			socket = new Socket();
			// 连接超时和read超时应用一个数据项 P302
			socket.connect(new InetSocketAddress(ip, port), timeOut);
			if (socket.isConnected()) {
				socket.setSoTimeout(timeOut);
				socket.setTcpNoDelay(true);
				dos = new DataOutputStream(socket.getOutputStream());
				dos.write(message.getBytes());
				dos.flush();
				socket.shutdownOutput();

				dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

				// 取返回数据长度
				byte[] byteHead = new byte[headLength];
				int headLength = dis.read(byteHead);

				System.out.println("rhead:" + new String(byteHead, 0, 8, "utf-8"));
				// 判断报文头是否正确
				if (headLength > 0) {
					// 得到报文体长度
					int msgLen = Integer.parseInt(new String(byteHead, 0, 8, "utf-8"));

					byte[] msgBody = new byte[msgLen];
					int readedLen = 0, i = 0;
					while (readedLen < msgLen) {
						i = dis.read(msgBody, readedLen, msgLen - readedLen);
						readedLen += i;
						if (i < 0)
							break;
					}

					// resMsg = new String(byteHead, 0, headLength, "utf-8");
					responseBody = new String(msgBody, 0, msgLen, "utf-8");
				}
				System.out.println("responseBody:" + responseBody);
				return responseBody;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				System.out.println("Close connectServer error:" + e.getMessage());
			}
		}
		return responseBody;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public static void main(String[] args) {
		NPP101Socket ep0001Socket = new NPP101Socket();
		ep0001Socket.sendMessage(null);
	}

}
