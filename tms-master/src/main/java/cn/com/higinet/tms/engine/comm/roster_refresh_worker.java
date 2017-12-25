package cn.com.higinet.tms.engine.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.bean;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker_base;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms.manager.dao.SqlMap;

/**
 * 名单更新服务
 * 
 * @author lining
 */
public class roster_refresh_worker extends tms_worker_base<Map<String, Object>> {
	static Logger log = LoggerFactory.getLogger(roster_refresh_worker.class);
	final static boolean isOn = tmsapp.get_config("tms.roster_refresh.isOn", 0) == 1;
	final static int deque_size = tmsapp.get_config("tms.roster_refresh.dequesize", 8192);
	final static int batch_size = tmsapp.get_config("tms.roster_refresh.batchsize", 1024);
	final static int batch_time = tmsapp.get_config("tms.roster_refresh.batchtime", 100);
	final static int item_maxlen = tmsapp.get_config("tms.roster_refresh.item_maxlen", 256);
	final static int msg_maxlen = tmsapp.get_config("tms.roster_refresh.msg_maxlen", 1024 * 3);
	final static String risk_ipaddr = tmsapp.get_serv_ip();
	final static int risk_port = tmsapp.get_config("tms.server.port", 8000);

	static tms_worker<Map<String, Object>> inst;

	public static tms_worker<Map<String, Object>> worker() {
		if (inst != null)
			return inst;
		synchronized (roster_refresh_worker.class) {
			if (inst != null)
				return inst;
			return inst = new roster_refresh_worker("roster_refresh", deque_size);
		}
	}

	public roster_refresh_worker(String name, int requst_max_size) {
		super(name, requst_max_size);
	}

	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(1024);
	// Map<名单ID, List<名单值>>
	Map<String, List<Map<String, Object>>> msgMap = new HashMap<String, List<Map<String, Object>>>();
	Map<String, List<Map<String, Object>>> tmpMsgMap = new HashMap<String, List<Map<String, Object>>>();

	batch_stmt_jdbc stmt;
	List<Object[]> servList;
	List<String> msgList;

	@Override
	protected void pre_run() {
		if (isOn) {
			stmt = new batch_stmt_jdbc(new data_source(), ((SqlMap) bean.get("tmsSqlMap")).getSql("TMS.SERVER.LIST"), new int[] { Types.INTEGER });
		}
		servList = new ArrayList<Object[]>();
		msgList = new ArrayList<String>(32);
	}

	@Override
	protected void run_once() {
		list.clear();
		msgMap.clear();
		tmpMsgMap.clear();
		this.drainTo(list, batch_size, batch_time);
		if (!isOn || list.isEmpty())
			return;
		for (Map<String, Object> map : list) {
			String rosterId = str_tool.to_str(map.get("rosterId"));
			List<Map<String, Object>> values = msgMap.get(rosterId);
			if (values == null) {
				values = new ArrayList<Map<String, Object>>();
				msgMap.put(rosterId, values);
			}
			map.remove("rosterId");
			values.add(map);
		}
		try {
			stmt.query_fetch_all(new Object[] { "1" }, new row_fetch() {
				@Override
				public boolean fetch(ResultSet rs) throws SQLException {
					while (rs.next()) {
						String ipAddr = rs.getString("IPADDR");
						int port = rs.getInt("PORT");
						if (!risk_ipaddr.equals(ipAddr) || risk_port != port) {
							servList.add(new Object[] { ipAddr, port });
						}
					}
					return true;
				}
			});
		} catch (SQLException e) {
			log.error("查询TMS_RUN_SERVER表, 获取风险评估服务列表出错.", e);
		}
		try {
			if (servList.isEmpty()) {
				log.info("没有可通知的风险评估服务.");
			} else {
				int length = 0;
				if (list.size() * item_maxlen <= msg_maxlen) {
					msgList.add(composeMessage(msgMap));
				} else {
					Iterator<Entry<String, List<Map<String, Object>>>> it = msgMap.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, List<Map<String, Object>>> en = it.next();
						String key = en.getKey();
						List<Map<String, Object>> list = (List<Map<String, Object>>) en.getValue();
						composeMsgMap(length, tmpMsgMap, key, list, msgList);
					}
					if (!tmpMsgMap.isEmpty()) {
						msgList.add(composeMessage(tmpMsgMap));
					}
				}
				for (Object[] serv : servList) {
					String ipaddr = (String) serv[0];
					int port = (Integer) serv[1];
					for (String msg : msgList) {
						try {
							if (str_tool.is_empty(msg))
								continue;
							sendMessage(ipaddr, port, msg, 3000);
						} catch (Exception e) {
							log.error(String.format("发送通知报文[ip: %s, port: %d, msg: %s]失败.", ipaddr, port, msg), e);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			servList.clear();
			msgList.clear();
		}
	}
	
	public static int composeMsgMap(int len, Map<String, List<Map<String, Object>>>
		tmpMsgMap, String key, List<Map<String, Object>> mapList, List<String> msgList) {
		if (mapList == null || mapList.isEmpty())
			return len;
		int size = ((msg_maxlen - len) / item_maxlen);// 此报文还需要的map个数
		if (mapList.size() > size) {
			// 超过最大报文长度
			List<Map<String, Object>> _list_ = new ArrayList<Map<String, Object>>();
			if (size > 0) {
				_list_.addAll(mapList.subList(0, size));
				tmpMsgMap.put(key, _list_);
			}
			msgList.add(composeMessage(tmpMsgMap));
			len = 0;
			tmpMsgMap.clear();
			_list_.clear();
			_list_.addAll(mapList.subList(size, mapList.size()));
			len = composeMsgMap(len, tmpMsgMap, key, _list_, msgList);
		} else {
			// 未超过最大报文长度
			len += (mapList.size() * item_maxlen);
			tmpMsgMap.put(key, mapList);
		}
		return len;
	}

	public static String composeMessage(Map<String, List<Map<String, Object>>> bodyMap) {
		try {
			String body = composeBody(bodyMap);
			String head = composeHead("0005", body.getBytes("UTF-8").length);
			return head + body;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String composeHead(String actionCode, int bodyLength) {
		StringBuffer sb = new StringBuffer();
		if (actionCode != null && !"".equals(actionCode)) {
			String len = String.valueOf(bodyLength);
			len = "00000000".substring(len.length()) + len;
			sb.append(len);// 报文体长度
			sb.append("TMS").append(" ").append(" ").append(" ").append(" ").append(" ");// 服务号
			sb.append(actionCode);// 交易号
			sb.append("XML").append(" ");// 报文体类型
			sb.append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ").append(" ");// 返回码
		}
		return sb.toString();
	}

	public static String composeBody(Map<String, List<Map<String, Object>>> bodyMap) {
		StringBuilder sb = new StringBuilder();
		sb.append(getXmlHead());
		sb.append("<tableName>TMS_MGR_ROSTERVALUE</tableName>");
		sb.append("<rosters type='list'>");
		Iterator<Entry<String, List<Map<String, Object>>>> it = bodyMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Map<String, Object>>> en = it.next();
			List<Map<String, Object>> list = en.getValue();
			sb.append("<roster type='map'>");
			sb.append(appendXmlMessage("rosterId", en.getKey()));
			sb.append("<values type='list'>");
			for (Map<String, Object> map : list) {
				sb.append("<value type='map'>");
				sb.append(getXmlExtMap(map));
				sb.append("</value>");
			}
			sb.append("</values>");
			sb.append("</roster>");
		}
		sb.append("</rosters>");
		sb.append("</Message>");
		return sb.toString();
	}

	private static String getXmlHead() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<Message>");
		return sb.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String getXmlExtMap(Map<String, Object> map) {
		StringBuffer sb = new StringBuffer("");
		if (map != null && !map.isEmpty()) {
			Set keySet = map.keySet();
			Iterator keyIt = keySet.iterator();
			while (keyIt.hasNext()) {
				Object key = keyIt.next();
				Object value = map.get(key);
				if (value == null)
					continue;
				if (value instanceof Map) {
					Map<String, Object> valueMap = (Map<String, Object>) value;
					if (valueMap != null && !valueMap.isEmpty()) {
						sb.append("<").append(String.valueOf(key)).append(" type='map'>");
						sb.append(getXmlExtMap(valueMap));
						sb.append("</").append(key).append(">");
					}
				} else if (value instanceof Collection) {
					String valstr = Arrays.toString(((Collection) value).toArray());
					sb.append(appendXmlMessage(String.valueOf(key), valstr.substring(1, valstr.length() - 1)));
				} else {
					sb.append(appendXmlMessage(String.valueOf(key), value));
				}
			}
		}
		return sb.toString();
	}

	public static String appendXmlMessage(String key, Object value) {
		StringBuffer bf = new StringBuffer("");
		if (value != null) {
			bf.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		}
		return bf.toString();
	}

	public static String sendMessage(String ip, int port, String msg, int timeOut) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		int ret = sendRequest(sb, ip, port, msg, timeOut);
		if (ret == 0)
			return sb.toString();
		return null;
	}

	public static int sendRequest(StringBuffer sb, String ip, int port, String msg, int timeOut) throws IOException {
		Socket socket = new Socket();
		try {
			socket.setReuseAddress(true);
			socket.connect(new InetSocketAddress(ip, port), timeOut);
			socket.setSoTimeout(timeOut);
			OutputStream os = socket.getOutputStream();
			if (os != null) {
				os.write(msg.getBytes("UTF-8"));
				os.flush();
			}
			String ret = readResponse(socket.getInputStream(), timeOut);
			sb.append(ret);
			return 0;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
		}
	}

	static String readResponse(InputStream is, int timeout) throws IOException {
		if (is == null)
			return null;

		byte[] buff = new byte[16 << 10];
		long abs_time_out = tm_tool.lctm_ms() + timeout;

		int len = read(is, buff, 0, 8, abs_time_out);
		if (len != 8)
			return null;

		int buff_len = 32 + Integer.parseInt(new String(buff, 0, 8), 10);

		len = read(is, buff, 8, buff_len - 8, abs_time_out);
		if (len != buff_len - 8)
			return null;

		return new String(buff, 0, buff_len, "UTF-8");
	}

	private static int read(InputStream stmi, byte[] buff, int pos, int len, long abs_tmout) throws IOException {
		int ret = 0;
		for (; tm_tool.lctm_ms() < abs_tmout && ret < len;) {
			int b = stmi.read(buff, pos + ret, len - ret);
			if (b < 0)
				return ret;

			ret += b;
		}
		return ret;
	}

	@Override
	protected void post_run() {
		if (stmt != null)
			stmt.close();
	}
}