package cn.com.higinet.tms35.manage.sign.common;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms35.manage.aop.cache.CacheSynService;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.StaticParameters;
import cn.com.higinet.tms35.manage.common.StaticParameters.ActionCode;
import cn.com.higinet.tms35.manage.common.util.MapUtil;
import cn.com.higinet.tms35.manage.exception.TmsMgrServiceException;

public class TmsMessageUtil {
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final String SEND_SUCCESS_MARK = "SUCCESS__MARK";// 成功标记字段名
	public static final String SEND_ERROR_INFO = "ERROR__INFO";// 错误信息字段名
	public static final int TIME_OUT = 10000;

	/**
	 * 向list的服务列表发送报文message
	 * @param message	发送的报文内容
	 * @param list		发送的服务列表
	 * @return			发送错误的服务列表
	 */
	public static List<Map<String, Object>> callServer(String message, List<Map<String, Object>> list) {
		if (list == null || list.isEmpty()) {
			throw new TmsMgrServiceException("参数list为空, 无可用的服务");
		}

		List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>(list.size());// 发送出错的列表
		for (int i = 0, len = list.size(); i < len; i++) {
			Map<String, Object> map = list.get(i);
			boolean isSuccess = MapUtil.getBoolean(map, SEND_SUCCESS_MARK);
			if (isSuccess) {
				continue;
			}

			String ipAddr = MapUtil.getString(map, DBConstant.TMS_RUN_SERVER_IPADDR);
			int port = MapUtil.getInteger(map, DBConstant.TMS_RUN_SERVER_PORT);

			SocketClient client = new SocketClient(ipAddr, port);
			try {
				clock c = new clock();
				client.connect(TIME_OUT);
				int left = c.left(TIME_OUT);
				if (left <= 0) {
					// socket连接时间过长,但是成功了,清空链接所占时长
					c.reset();
					left = TIME_OUT;
				}
				client.setSoTimeout(left);
				client.write(message);
				client.flush();
				String ret = client.read(TIME_OUT, c);
				if (ret == null) {
					result(errorList, map, "读取返回结果超时");
				}
				String backCode = ret.substring((StaticParameters.HEAD_LEN - 8), StaticParameters.HEAD_LEN);
				result(errorList, map, (StaticParameters.SYSTEM_SUCCESS.equals(backCode) ? null : new Object[] { "返回错误码:", backCode }));
			} catch (Exception e) {
				result(errorList, map, new Object[] { "异常报错:", e });
			} finally {
				client.close();
			}
		}

		return errorList;
	}

	private static void result(List<Map<String, Object>> errorList, Map<String, Object> map, Object... errorInfo) {
		if (errorInfo == null) {
			map.put(SEND_SUCCESS_MARK, true);
		} else {
			errorList.add(map);
			map.put(SEND_SUCCESS_MARK, false);
			StringBuilder sb = new StringBuilder(128);
			for (int i = 0, len = errorInfo.length; i < len; i++) {
				sb.append(errorInfo[i]);
			}
			map.put(SEND_ERROR_INFO, sb.toString());
		}
	}

	public static String composeMessage(ActionCode actionCode, String message) {
		String body = composeBody(message);
		int bodyLen = body.getBytes(UTF8).length;
		String head = composeHead(actionCode, bodyLen);
		return (new StringBuilder(bodyLen + 32).append(head).append(body).toString());
	}

	/**
	 * 组装风险评估报文体
	 * 
	 * @param o
	 * @return
	 */
	public static String composeBody(String message) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(CacheSynService.getXmlHead(null));
		sb.append(message);
		sb.append("</Message>");
		return sb.toString();
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
	public static String composeHead(ActionCode actionCode, int bodyLength) {
		StringBuilder sb = new StringBuilder(32);
		if (actionCode != null) {
			String len = String.valueOf(bodyLength);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
			sb.append(actionCode.getCode());// 交易号
			sb.append(StaticParameters.MESSAGE_TYPE).append(" ");// 报文体类型
			sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		}
		return sb.toString();
	}
}