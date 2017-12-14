package cn.com.higinet.tms.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.core.common.func_if;
import cn.com.higinet.tms.core.util.DateUtil;
import cn.com.higinet.tms.core.util.JsonUtil;

/**
 * 运行周期
 * 
 * @author lining
 *
 */
public class OperatCycle implements Serializable {
	// static Logger logger = LogManager.getLogger(OperatCycle.class);
	private static Logger logger = LoggerFactory.getLogger(OperatCycle.class);

	static AtomicLong _counter_ = new AtomicLong(0);
	static int _index_ = 0;
	static int _socket_index_ = 0;
	public static final int INDEX_CALL_API_INTERFACE = _index_++;
	public static final int INDEX_DATA_COMPLETE = _index_++;
	public static final int INDEX_COMPOSE_MESSAGE_START = _index_++;
	public static final int INDEX_COMPOSE_MESSAGE_END = _index_++;
	public static final int INDEX_RETURN_RISKRESULT_TIME = _index_++;

	public static final int INDEX_SOCKET_DATA_GET_SOCKET_BEGORE = _socket_index_++;
	public static final int INDEX_SOCKET_DATA_GET_SOCKET_TIME = _socket_index_++;
	public static final int INDEX_SOCKET_DATA_SEND_MESSAGE_BEFORE = _socket_index_++;
	public static final int INDEX_SOCKET_DATA_SEND_MESSAGE_FINISH = _socket_index_++;
	public static final int INDEX_SOCKET_DATA_READ_MESSAGE_FINISH = _socket_index_++;
	public static final int INDEX_SOCKET_DATA_SOCKET_CLOSE = _socket_index_++;

	static final String[] m_code = new String[] { "CALL_API_INTERFACE", "DATA_COMPLETE", "COMPOSE_MESSAGE_START", "COMPOSE_MESSAGE_END", "RETURN_RISKRESULT_TIME" };

	static final String[] msg_code = new String[] { "GET_SOCKET_BEGORE", "GET_SOCKET_TIME", "SEND_MESSAGE_BEFORE", "SEND_MESSAGE_FINISH", "READ_MESSAGE_FINISH", "SOCKET_CLOSE" };

	static final String[] m_desc = new String[] { "调用API接口", "数据补齐", "开始拼装报文", "拼装报文结束", "返回风险评估结果" };

	static final String[] msg_desc = new String[] { "获取Socket连接前", "获取Socket连接完成", "发送报文之前", "发送报文完成，开始读取返回报文", "读取返回报文完成", "Socket通讯结束" };

	public static void main(String[] args) {
		for (int i = 0, len = m_code.length; i < len; i++) {
			System.out.println(m_code[i] + ": " + m_desc[i]);
		}
	}

	private long createTime = 0;
	private boolean isConfirm = false;
	private boolean isSync = false;
	private Object riskTrades = null;
	private RiskResult riskResult;
	private String sendInfo = null;// 发送报文内容
	private String reciveInfo = null;// 返回报文内容
	Integer[] m_data = new Integer[m_code.length];
	private List<String> sendServerInfoList = new ArrayList<String>();// 发送所需
	private List<Integer[]> socketDatas = new ArrayList<Integer[]>();// socket通讯信息

	protected OperatCycle() {
		createTime = System.currentTimeMillis();
	}

	public static String helpString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("\n#").append("CREATETIME").append("\t=").append("创建时间");
		sb.append("\n#").append("IS_CONFIRM").append("\t=").append("是否风险确认");
		sb.append("\n#").append("IS_SYNC").append("\t=").append("是否同步");
		for (int i = 0, len = m_code.length; i < len; i++) {
			sb.append("\n#")//
					.append(m_code[i])//
					.append("\t=")//
					.append(m_desc[i]);
		}
		for (int i = 0, len = msg_code.length; i < len; i++) {
			sb.append("\n#")//
					.append(msg_code[i])//
					.append("\t=")//
					.append(msg_desc[i]);
		}
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		if (_counter_.getAndIncrement() % 1000 == 0) {
			logger.info(helpString());
		}
		sb.append("\nCREATETIME:").append(DateUtil.fotmatDate(new Date(this.createTime), "yyyy-MM-dd HH:mm:ss.SSS")).append(',');
		sb.append("IS_CONFIRM:").append(this.isConfirm).append(',');
		sb.append("IS_SYNC:").append(this.isSync).append(',');
		for (int i = 0, len = m_data.length; i < len; i++) {
			Integer data = m_data[i];
			if (data == null)
				continue;
			sb.append(m_code[i]).append(':').append(data).append(',');
		}
		if (!socketDatas.isEmpty()) {
			sb.append("SOCKETS:[");
			for (int i = 0, len = socketDatas.size(); i < len; i++) {
				Integer[] datas = socketDatas.get(i);
				String serverInfo = (i < sendServerInfoList.size() ? sendServerInfoList.get(i) : null);
				if (datas != null) {
					if (i > 0) {
						sb.append(",");
					}
					sb.append("{");
					if (serverInfo != null) {
						sb.append("SERVER:").append(serverInfo).append(",");
					}
					for (int j = 0, jlen = datas.length; j < jlen; j++) {
						Integer data = datas[j];
						if (data == null)
							continue;
						sb.append(msg_code[j]).append(':').append(data).append(',');
					}
					sb.setCharAt(sb.length() - 1, '}');
				}
			}
			sb.append("]");
		}
		if (this.riskTrades != null) {
			sb.append('\n');
			if (this.riskTrades instanceof Transaction) {
				sb.append("Transaction:");
			} else if (this.riskTrades instanceof Batch) {
				sb.append("Batch:");
			} else {
				sb.append("unknown:");
			}
			sb.append(JsonUtil.formObject(this.riskTrades, new func_if<Object>() {
				public boolean _if(Object o) {
					if (o instanceof OperatCycle) {
						return false;
					}
					return true;
				}
			}));
		}
		sb.append("\nSEND_MESSAGE:").append(this.sendInfo);
		sb.append("\nRECIVE_MESSAGE:").append(this.reciveInfo);
		sb.append("\nRiskResult:").append(JsonUtil.formObject(riskResult));
		return sb.toString();
	}

	public void setConfirm(boolean isConfirm) {
		this.isConfirm = isConfirm;
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public void setRiskTrades(Object riskTrades) {
		this.riskTrades = riskTrades;
	}

	public void setRiskResult(RiskResult riskResult) {
		this.riskResult = riskResult;
		this.setPinTime(INDEX_RETURN_RISKRESULT_TIME);
	}

	public void setSendInfo(String sendInfo) {
		this.sendInfo = sendInfo;
	}

	public void setReciveInfo(String reciveInfo) {
		this.reciveInfo = reciveInfo;
	}

	public void setServerInfo(String serverInfo) {
		this.sendServerInfoList.add(serverInfo);
	}

	public void newSocketData() {
		this.socketDatas.add(new Integer[msg_code.length]);
	}

	public void setScoketData(int index) {
		int s = socketDatas.size();
		if (s > 0) {
			Integer[] datas = socketDatas.get(s - 1);
			if (index >= datas.length)
				return;
			datas[index] = (int) (System.currentTimeMillis() - createTime);
		}
	}

	public final void setPinTime(int index) {
		if (index >= m_data.length)
			return;
		m_data[index] = (int) (System.currentTimeMillis() - createTime);
	}

	public int getData(int index) {
		if (index >= m_data.length)
			return -1;
		return m_data[index];
	}

	public long getCreateTime() {
		return createTime;
	}

	public static void setOperatCycle(OperatCycle operatCycle, int index) {
		if (operatCycle != null) {
			operatCycle.setPinTime(index);
		}
	}
}