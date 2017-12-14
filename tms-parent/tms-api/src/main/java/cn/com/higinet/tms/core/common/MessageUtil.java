/*
 * Copyright © 2012 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.core.common;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.util.DateUtil;


/**
 * @author zhangfg
 * @version 1.1.0,2012-08-08
 * @description 公共报文服务类
 */
public class MessageUtil {
	// private static Logger logger = LogManager.getLogger(MessageUtil.class);
	private static Logger logger = LoggerFactory.getLogger(MessageUtil.class);
	/**
	 * 发送报文
	 * @param ip		发送的目标服务器IP地址
	 * @param port		服务器监听端口
	 * @param b			报文头
	 * @param ws		报文体
	 * @param timeOut	超时时间
	 * @return
	 */
	public static String sendMessage(String ip,String port,byte[] b, byte[] ws,int timeOut){
		String reg = "";
		
		Socket sock = null;
		DataOutputStream dos = null;
		InputStream dis = null;
		
		try {
			sock = new Socket(ip, new Integer(port));
			sock.setSoTimeout(timeOut);
			dos =  new DataOutputStream(sock.getOutputStream());
			
			dos.write(b);
			dos.write(ws);
			dos.flush();
			dis = new DataInputStream(new BufferedInputStream(
					sock.getInputStream()));
			byte[] ret = new byte[2048];
			
			int len1 = dis.read(ret);
			if(len1<0){
				len1 = 10;
			}
			String resMsg = new String(ret, 0, len1, "gb2312");
			return resMsg;
		} catch (Exception e) {
			logger.info(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "MessageUtil sendMessage error!" + e);
		} finally {
			try {
				if (dis != null)
					dis.close();
				if (dos != null)
					dos.close();
				if (sock != null)
					sock.close();
			} catch (IOException e) {
				logger.info(DateUtil.dateConvert(new Date(), "yyyy-MM-dd HH:mm:ss") + "Close message channel error!" + e);
			}
		}
		return reg;
	}
	
}
