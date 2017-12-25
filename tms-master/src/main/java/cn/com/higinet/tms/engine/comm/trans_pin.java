package cn.com.higinet.tms.engine.comm;

import java.util.Date;

import cn.com.higinet.tms.engine.core.cond.date_tool;

public class trans_pin implements TransPinLog {
	static int _counter_ = 0;

	public static int INDEX_IO_THREADID = _counter_++;

	public static int INDEX_RISKINIT_THREADID = _counter_++;

	public static int INDEX_RISKEVAL_THREADID = _counter_++;

	public static int INDEX_IO_ENV_INIT_END = _counter_++;

	public static int INDEX_TRANS_IDENTIFY_END = _counter_++;

	public static int INDEX_TRANS_INTO_INITQUEUE_BG = _counter_++;

	public static int INDEX_TRANS_INTO_INITQUEUE_END = _counter_++;

	public static int INDEX_TRANS_OUT_INITQUEUE = _counter_++;

	public static int INDEX_TRANS_DATAINIT_BG = _counter_++;

	public static int INDEX_TRANS_LOAD_STAT_BG = _counter_++;

	public static int INDEX_TRANS_LOAD_TRANFFIC_END = _counter_++;

	public static int INDEX_TRANS_LOAD_SESSION_END = _counter_++;

	public static int INDEX_TRANS_LOAD_USER_END = _counter_++;

	public static int INDEX_TRANS_DATAINIT_END = _counter_++;

	public static int INDEX_TRANS_INTO_EVALQUEUE_BG = _counter_++;

	public static int INDEX_TRANS_INTO_EVALQUEUE_END = _counter_++;

	public static int INDEX_TRANS_OUT_EVALQUEUE = _counter_++;

	public static int INDEX_TRANS_RISKEVAL_BG = _counter_++;

	public static int INDEX_TRANS_STRATEGY_BG = _counter_++;

	public static int INDEX_TRANS_RULE_TIMEOUT = _counter_++;

	public static int INDEX_TRANS_MODEL_BG = _counter_++;

	public static int INDEX_TRANS_MODEL_END = _counter_++;

	public static int INDEX_TRANS_STRATEGY_END = _counter_++;

	public static int INDEX_TRANS_USER_DEVICE_BG = _counter_++;

	public static int INDEX_TRANS_USER_DEVICE_END = _counter_++;

	public static int INDEX_TRANS_TO_COMMIT = _counter_++;

	public static int INDEX_TRANS_RISKEVAL_END = _counter_++;

	public static int INDEX_RETURN_RESULT_BG = _counter_++;

	public static int INDEX_RETURN_RESULT_END = _counter_++;

	static final String[] m_code = new String[] { "IO_THREADID", "RISKINIT_THREADID", "RISKEVAL_THREADID", "IO_ENV_INIT_END", "TRANS_IDENTIFY_END", "INTO_INITQUEUE_BG", "INTO_INITQUEUE_END", "TRANS_OUT_INITQUEUE", "TRANS_DATAINIT_BG", "TRANS_LOAD_STAT_BG", "TRANS_LOAD_TRANFFIC_END", "TRANS_LOAD_SESSION_END", "TRANS_LOAD_USER_END", "TRANS_DATAINIT_END", "TRANS_INTO_EVALQUEUE_BG",
			"TRANS_INTO_EVALQUEUE_END", "TRANS_OUT_EVALQUEUE", "TRANS_RISKEVAL_BG", "TRANS_STRATEGY_BG", "TRANS_RULE_TIMEOUT", "TRANS_MODEL_BG", "TRANS_MODEL_END", "TRANS_STRATEGY_END", "TRANS_USER_DEVICE_BG", "TRANS_USER_DEVICE_END", "TRANS_TO_COMMIT", "TRANS_RISKEVAL_END", "RETURN_RESULT_BG", "RETURN_RESULT_END" };

	static final String[] m_desc = new String[] { "IO线程ID", "初始化线程ID", "风险评估线程ID", "收到IO请求数据, 创建io_env, 完成时刻", "交易识别, 完成时刻", "交易进入初始化线程队列, 开始时刻", "交易进入初始化线程队列, 完成时刻", "交易出初始化线程队列时刻", "交易数据初始化(执行init方法), 开始时刻", "交易字段准备完成时刻/请求异步读取统计, 开始时刻", "读取交易流水数据, 完成完成", "读取SESSION数据, 完成完成", "读取用户数据, 完成时刻", "交易数据初始化(请求异步读取统计), 完成/失败时刻", "交易进入风险评估线程队列, 开始时刻", "交易进入风险评估线程队列, 完成时刻", "交易出风险评估线程队列时刻",
			"交易风险评估(执行run方法), 开始时刻", "执行交易策略, 开始时刻", "执行规则超时时间", "执行交易风险模型, 开始时刻", "执行交易风险模型, 完成/失败时刻", "执行交易策略, 完成时刻", "执行用户设备信息更新, 开始时刻", "执行用户设备信息更新, 完成时刻", "交易数据移交存储提交线程时刻", "交易风险评估(执行run方法), 完成时刻", "返回IO请求结果, 开始时刻", "返回IO请求结果, 完成时刻" };

	long m_base_time = 0;

	Object[] m_data = new Object[m_code.length];

	String txnCode = null;

	String userId = null;

	String loginName = null;

	String txnId = null;

	String result = null;

	boolean is_confirm = false;

	boolean is_async = false;

	protected trans_pin() {
		m_base_time = System.currentTimeMillis();
	}
	
	public String helpString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\n#").append("RECTIME").append("\t=").append("接收时间");
		sb.append("\n#").append("TXNCODE").append("\t=").append("交易流水号");
		sb.append("\n#").append("USERID").append("\t=").append("用户标识");
		sb.append("\n#").append("LOGINNAME").append("\t=").append("登录名");
		sb.append("\n#").append("TXNID").append("\t=").append("交易标识");
		sb.append("\n#").append("IS_CONFIRM").append("\t=").append("是否风险确认");
		sb.append("\n#").append("IS_ASYNC").append("\t=").append("是否异步");
		for (int i = 0, len = m_code.length; i < len; i++) {
			sb.append("\n#")//
					.append(m_code[i])//
					.append("\t=")//
					.append(m_desc[i]);
		}
		sb.append("\n#").append("RESULT").append("\t=").append("返回结果");
		return sb.toString();
	}

	public static String title() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("RECTIME").append(' ');
		sb.append("TXNCODE").append(' ').append("LOGINNAME").append(' ');
		sb.append("USERID").append(' ').append("TXNID").append(' ');
		sb.append("IS_CONFIRM").append(' ').append("IS_ASYNC").append(' ');
		for (int i = 0; i < m_code.length; i++) {
			sb.append(m_code[i]).append(' ');
		}
		sb.append("RESULT");
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("RECTIME:").append(date_tool.format(new Date(this.m_base_time))).append(',');
		sb.append("TXNCODE:").append(this.txnCode).append(',').append("LOGINNAME:").append(this.loginName).append(",");
		sb.append("USERID:").append(userId).append(',').append("TXNID:").append(this.txnId).append(',');
		sb.append("IS_CONFIRM:").append(this.is_confirm).append(',').append("IS_ASYNC:").append(this.is_async).append(',');
		for (int i = 0, len = m_data.length; i < len; i++) {
			Object data = m_data[i];
			if (data == null)
				continue;
			sb.append(m_code[i]).append(':').append(data).append(',');
		}
		sb.append("RESULT:").append(this.result);
		return sb.toString();
	}

	public void setTxnCode(String txnCode) {
		this.txnCode = txnCode;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public void isConfirm(boolean is_confirm) {
		this.is_confirm = is_confirm;
	}

	public void isAsync(boolean is_async) {
		this.is_async = is_async;
	}

	public void setResult(String code, String error) {
		this.result = "[" + code + "]" + (error == null ? "" : error);
	}

	public final void pin_time(int index) {
		setData(index, (int) (System.currentTimeMillis() - m_base_time));
	}

	public final void setData(int index, Object data) {
		if (index >= m_data.length)
			return;
		m_data[index] = data;
	}

	public final void pin_thread(int index) {
		if (index >= m_data.length)
			return;
		String name = Thread.currentThread().getName();
		int i = 0;
		for (i = name.length() - 1; i >= 0; i--) {
			if ('0' <= name.charAt(i) && name.charAt(i) <= '9')
				continue;
			break;
		}
		m_data[index] = Integer.valueOf(name.substring(i + 1));
	}

	public long get_base_time() {
		return m_base_time;
	}
}