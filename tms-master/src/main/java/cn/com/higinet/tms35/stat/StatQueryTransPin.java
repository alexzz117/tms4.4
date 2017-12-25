package cn.com.higinet.tms35.stat;

import java.text.SimpleDateFormat;

import cn.com.higinet.tms35.comm.TransPinLog;

// TODO: Auto-generated Javadoc
/**
 * transpin类，主要用于记录查询统计过程中的过程日志，便于发现执行过程中的性能瓶颈或者耗时点.
 */
public class StatQueryTransPin implements TransPinLog {

	/**  基础技术器，仅用于动态生成打印日志阶段的序号，对应 m_code 的数组index. */
	private static int BASE_INDEX = 0;

	/**  准备统计查询. */
	public static final int INDEX_PREPARE_STAT_QUERY = BASE_INDEX++;

	/** 准备统计查询结束. */
	public static final int INDEX_PREPARE_STAT_QUERY_END = BASE_INDEX++;

	/** 统计查询队列大小. */
	public static final int INDEX_STAT_QUEUE_SIZE = BASE_INDEX++;

	/** 开始查询数据库. */
	public static final int INDEX_STAT_DB_QUERY_BEGIN = BASE_INDEX++;

	/** 数据库查询队列大小. */
	public static final int INDEX_DB_QUERY_SIZE = BASE_INDEX++;

	/** 查询数据库结束. */
	public static final int INDEX_STAT_DB_QUERY_END = BASE_INDEX++;

	/** 本批次统计查询结束. */
	public static final int INDEX_STAT_QUERY_END = BASE_INDEX++;

	/** The m_base_time. */
	private long m_base_time = 0;

	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/** The m_data. */
	private int[] m_data = new int[m_code.length];

	/**  日志打印的标签，展示在日志里面的阶段英文名称. */
	private static final String[] m_code = new String[] { "PREPARE_STAT_QUERY", "PREPARE_STAT_QUERY_END", "STAT_QUEUE_SIZE", "STAT_DB_QUERY_BEGIN", "DB_QUERY_SIZE", "STAT_DB_QUERY_END", "STAT_QUERY_END" };

	/**  日志打印的标签，展示在日志里面的阶段中文描述. */
	private static final String[] m_desc = new String[] { "准备统计查询", "准备统计查询结束", "统计查询队列大小", "开始查询数据库", "数据库查询队列大小", "查询数据库结束", "本批次统计查询结束" };

	/** 用于记录每个阶段的私有信息. */
	private StringBuilder[] nodeMsg = new StringBuilder[m_code.length];

	/**
	 * Instantiates a new trans pin.
	 */
	public StatQueryTransPin() {
		m_base_time = System.currentTimeMillis();
	}

	/**
	 * Help_string.
	 *
	 * @return the string
	 */
	public String helpString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("\n#").append("RECTIME").append("\t=").append("TransPin基础时间");
		for (int i = 0, len = m_code.length; i < len; i++) {
			sb.append("\n#")//
					.append(m_code[i])//
					.append("\t=")//
					.append(m_desc[i]);
		}
		return sb.toString();
	}

	/**
	 * Gets the _data.
	 *
	 * @param index the index
	 * @return the _data
	 */
	public int get_data(int index) {
		if (index >= m_data.length)
			return -1;
		return m_data[index];
	}

	/**
	 * 添加阶段的单独日志.
	 *
	 * @param index the index
	 * @param msg the msg
	 */
	public void addTips(int index, String msg) {
		StringBuilder builder = nodeMsg[index];
		if (builder == null) {
			builder = new StringBuilder();
			nodeMsg[index] = builder;
			builder.append(msg);
		} else {
			builder.append("|").append(msg);
		}
	}

	/**
	 * Title.
	 *
	 * @return the string
	 */
	public static String title() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("BASE_TIME").append(' ');
		for (int i = 0; i < m_code.length; i++) {
			sb.append(m_code[i]).append(' ');
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(256);
		sb.append("RECTIME:").append(formatter.format(m_base_time)).append(",");
		for (int i = 0, len = m_data.length, end = len - 1; i < len; i++) {
			int data = m_data[i];
			if (data == -1)
				continue;
			sb.append(m_code[i]).append(':').append(m_data[i]);
			if (nodeMsg[i] != null) {
				sb.append(",").append(nodeMsg[i].toString());
			}
			if (i != end)
				sb.append(',');
		}
		return sb.toString();
	}

	/**
	 * Pin_time.
	 *
	 * @param index the index
	 */
	public final void pin_time(int index) {
		if (index >= m_data.length)
			return;
		m_data[index] = (int) (System.currentTimeMillis() - m_base_time);
	}

	public final void addData(int index, int data) {
		if (index >= m_data.length)
			return;
		m_data[index] = data;
	}

	/**
	 * Gets the _base_time.
	 *
	 * @return the _base_time
	 */
	public long get_base_time() {
		return m_base_time;
	}

	public static void main(String[] args) {
		StatQueryTransPin transPin = new StatQueryTransPin();
		transPin.pin_time(StatQueryTransPin.INDEX_PREPARE_STAT_QUERY);
		System.out.println(transPin.helpString());
		System.out.println(transPin.toString());

	}
}