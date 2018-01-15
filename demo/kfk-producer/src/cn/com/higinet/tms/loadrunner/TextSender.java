package cn.com.higinet.tms.loadrunner;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

import cn.com.higinet.tms.core.model.RiskResult;
import cn.com.higinet.tms.core.service.impl.TransApiMessageImpl;

/**
 * 调用短信服务的客户端实现
 * 
 * @author wujinwang
 * */

public class TextSender {

	/**
	 * 报文头长度
	 */
	private final int headLength = 32;

	/**
	 * 短信的IP地址,通过配置tms-service.xml配置
	 * */
	//public String ip = "10.118.244.180";
	private String ip = "127.0.0.1";

	/**
	 * 短信的端口，通过配置tms-service.xml配置
	 * */
	private int port = 8000;

	/**
	 * 短信的连接超时，通过配置tms-service.xml配置
	 * */
	private int timeOut = 2000;

	public RiskResult sendMessage(String actionCode, String userId, String textBody) {
		boolean b = false;
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		String msgBody = xml + textBody;
		//			+ "<Message><SYNCFLAG>1</SYNCFLAG><TXNCODE>3695f67f09a64b78b0980e2b6a48a3d2</TXNCODE><TXNTIME>2017-07-03 16:07:45</TXNTIME><TXNID>NPP101</TXNID><CHANCODE>CH01</CHANCODE><USERID>20174936241831</USERID><SESSIONID>vzyh77ghn745!1499069265119</SESSIONID><IPADDR>113.195.78.37</IPADDR><DEVICEFINGER><![CDATA[11||20012001:862644034042707:862644034042707|2002:460028707080952|2002:460028707080952|2003:a4:44:d1:81:4c:31|2004:91QEBP75224E|2003:a4:44:d1:81:4c:31|2004:91QEBP75224E|2006:|2a15681c6bb2cb882006:2a15681c6bb2cb88|2009:0|2009:0|2010:true|2010:true||20132013::4.5893899376714554.589389937671455|2014:1080*1920|2014:1080*1920|2015:m3 note|2015:m3 note|2016:魅蓝 note 3|2016:魅蓝 note 3|2018:android|2018:android|2019:5.1|2019:5.1|2020:zh|2020:zh|2021:com.sfpay.mobile|2021:com.sfpay.mobile|2022:V4.0.4|2022:V4.0.4|2023:|322d4985062b162023:322d4985062b16|2024:22|2024:22|2025:|2025:1.26 GB1.26 GB|2026:1.65 GB|2026:1.65 GB|2027:71|2027:71|2028:MOLY.LR11.W1539.MD.MP.V9.P55, 2016/10/13 13:52|2028:MOLY.LR11.W1539.MD.MP.V9.P55, 2016/10/13 13:52|2029:3.10.72+|2029:3.10.72+|2030:CN|2030:CN|2031:TimeZone GMT+08:00Timezon id :: Asia/Shanghai|2031:TimeZone GMT+08:00Timezon id :: Asia/Shanghai|2032:192.168.1.3|2032:192.168.1.3|2033:|220.248.192.122033:220.248.192.12|2034:46002|2034:46002|2035:460|2035:460|2036:02|2037:08:10:79:9a:2c:70|2036:02|2037:08:10:79:9a:2c:70|2039:leike|2039:leike|2051:10.61 GB|2051:10.61 GB|2052:AArch64 Processor rev 2 (aarch64) 0|2052:AArch64 Processor rev 2 (aarch64) 0|2053:BOARD: m3note, BOOTLOADER: unknown|2053:BOARD: m3note, BOOTLOADER: unknown|2054:m3note|2054:m3note|2055:wifi|2055:wifi|2057:89860017141507166306]]></DEVICEFINGER><DEVICETOKEN>5ef0740b91ec49dbb094bef2bd874b20</DEVICETOKEN><DISPATCH>20174936241831</DISPATCH><AUTHLEVEL>MIDD_AUTH</AUTHLEVEL><LOGINTYPE>02</LOGINTYPE><BUSYSRC>APP</BUSYSRC><LONGITUDE>114.956143</LONGITUDE><LATITUDE>25.856457</LATITUDE></Message>";

		int bodyLength = 0;
		try {
			bodyLength = msgBody.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String msgHead = this.getReqMsgHead(actionCode, userId, bodyLength + 32);
		System.out.println("---msgHead--:" + msgHead);

		String msg = msgHead + msgBody;
		System.out.println("---msg--:" + msg);

		return new TransApiMessageImpl().getRiskResult(callServer(ip, port, msg, timeOut));

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
	public String getReqMsgHead(String actionCode, String userId, int bodyLength) {
		// 00000329TMS 0002XML
		StringBuffer sb = new StringBuffer();
		String len = String.valueOf(bodyLength);
		len = "00000000".substring(len.length()) + len;
		sb.append(len);// 报文体长度
		sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
		sb.append(actionCode);// 交易号
		sb.append("XML").append(" ");// 报文体类型
		sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		sb.append(userId);
		sb.append("                  ");
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
}
