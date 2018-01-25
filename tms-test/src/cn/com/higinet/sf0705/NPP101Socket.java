package cn.com.higinet.sf0705;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

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
	//public String ip = "10.118.244.172";
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
		String uuid = UUID.randomUUID().toString();

		String msgBody = xml
				+ "<Message><TXNSTATUS>1</TXNSTATUS><RS_ID>1</RS_ID><TXNID>rate0001</TXNID><USERID>20149919931963</USERID><LEVEL_SCORE>-100,1|-99.83,2|50,3|70,4</LEVEL_SCORE></Message>";
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
