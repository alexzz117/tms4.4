package cn.com.higinet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import cn.com.higinet.tms.core.common.StaticParameter;
import cn.com.higinet.tms.core.model.RiskResult;

public abstract class Rule {

	/**
	 * api参数
	 */
	public static boolean useFlag = true;// 启用标识，开启/关闭API或应用探针 true开 false关
	public static boolean syncFlag = true;// 同步标志 true同步 false异步
	public static int timeout = 300000;// 超时时间(毫秒)
	// ，如果为0，则以tmsServer.properties中的timeout为准

	public static String sessionId = "";// 会话ID
	public static String channelType = "CH00";

	public abstract void execute() throws Exception;

	protected static void printResult(RiskResult result) {
		if (result == null)
			return;
		System.out.println("返回码：" + result.getBackCode() + ", 错误信息："
				+ result.getErrorInfo());
		if (StaticParameter.SYSTEM_SUCCESS.equals(result.getBackCode()))
			System.out.println("txnId:" + result.getTxnId() + ", txnName:"
					+ result.getTxnName() + ", txnCode:"
					+ result.getTransCode() + ", \nalertId:"
					+ result.getRiskId() + ", disposal:" + result.getDisposal()
					+ ", hitRuleNum:" + result.getHitRuleNum()
					+ ", trigRuleNum:" + result.getTrigRuleNum()
					+ ", \nprocessInfo:{" + result.getProcessInfo() + "}"
					+ ", \nswitchInfo:{" + result.getSwitchInfo() + "}"
					+ ", \nactionInfo:{" + result.getActionInfo() + "}"
					+ ", \nhitRules:{" + result.getHitRules() + "}");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	private static String composeHead(String actionCode, int bodyLength) {
		StringBuffer sb = new StringBuffer();
		if (actionCode != null && !"".equals(actionCode)) {
			String len = String.valueOf(bodyLength);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ")
					.append(" ");// 服务号
			sb.append(actionCode);// 交易号
			sb.append(StaticParameter.MESSAGE_TYPE).append(" ");// 报文体类型
			sb.append(" ").append(" ").append(" ").append(" ").append(" ")
					.append(" ").append(" ").append(" ");// 返回码
		}
		return sb.toString();
	}

	public static String sendMessage(String ip, String port, byte[] b,
			byte[] ws, int timeOut) throws SocketTimeoutException,
			NumberFormatException, UnknownHostException, IOException {

		Socket sock = null;
		DataOutputStream dos = null;
		InputStream dis = null;

		sock = new Socket(ip, new Integer(port));

		sock.setSoTimeout(timeOut);

		dos = new DataOutputStream(sock.getOutputStream());
		String len = String.valueOf(ws.length);
		len = "00000000".substring(len.length()) + len;
		System.arraycopy(len.getBytes(), 0, b, 0, 8);

		dos.write(b);
		dos.write(ws);
		dos.flush();

		dis = new DataInputStream(
				new BufferedInputStream(sock.getInputStream()));
		byte[] ret = new byte[2048];
		int len1 = dis.read(ret);
		if (len1 < 0)
			len1 = 10;

		String resMsg = new String(ret, 0, len1, "UTF-8");
		try {
			if (dis != null)
				dis.close();
			if (dos != null)
				dos.close();
			if (sock != null)
				sock.close();
		} catch (IOException e) {
		}
		return resMsg;
	}
}
