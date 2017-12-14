package cn.com.higinet.tms.core.common;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * 临时加入的打印发送报文日志
 * 
 * @author wx
 *
 */
public class LogHolder {
	private static final ThreadLocal<Map<String, Object>> holder = new ThreadLocal<Map<String, Object>>();
	private static final SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static final String TXN_CODE = "txn_code"; // 交易流水
	public static final String TXN_ID = "txn_id";// 交易标识
	public static final String BEFORE_SEND = "before_send";// 发送报文之前耗时
	public static final String START_TIME = "start_time"; // 执行消息发送开始时间
	public static final String BEFORE_CONN = "before_conn"; // 开始发送消息到建立socket连接之前这段时间
	public static final String BEFORE_WRITE = "before_write";// 发送报文之前耗时
	public static final String BEFORE_READ = "before_read";// 读耗费时间
	public static final String ITER_COUNT = "iter_count";// 循环次数
	public static final String ITER_COST = "iter_cost";// 循环耗时
	public static final String ITER_TIME = "iter_time";// 循环开始时间
	public static final String ITER_SERVER = "iter_server";// 循环开始时间
	public static final String ITER_END_TIME = "iter_end_time";// 循环结束时间
	public static final String ERROR = "error";// 异常

	public static final String ERR_CONN = "connect_err";
	public static final String ERR_WRITE = "write_err";
	public static final String ERR_READ = "read_err";

	private static Map<String, Object> getMap() {
		Map<String, Object> m = holder.get();
		if (m == null) {
			m = new HashMap<String, Object>();
			holder.set(m);
		}
		return m;
	}

	public static long beginTime() {
		Object beginTime = get(START_TIME);
		if (beginTime == null) {
			long current = System.currentTimeMillis();
			put(START_TIME, current);
			return current;
		}
		return Long.valueOf(beginTime.toString());
	}

	public static void put(String key, Object value) {
		getMap().put(key, value);
	}

	public static Object get(String key) {
		return getMap().get(key);
	}

	public static void reset() {
		getMap().clear();
		getMap().put(LogHolder.START_TIME, System.currentTimeMillis());
	}

	public static void clear() {
		getMap().clear();
	}

	public static int currentIterCount() {
		Object rc = getMap().get(ITER_COUNT);
		if (rc == null) {
			getMap().put(ITER_COUNT, -1);
			return -1;
		}
		return Integer.valueOf(rc.toString());
	}

	public static void increaseIterCount() {
		getMap().put(ITER_COUNT, currentIterCount() + 1);
	}

	public static void recordIterateTime(long begin) {
		put(ITER_END_TIME + currentIterCount(), System.currentTimeMillis());
		put(ITER_COST + currentIterCount(), System.currentTimeMillis() - begin);
	}

	private static String cal(Object o1, Object o2) {
		if (o2 == null) {
			return "none";
		}
		return String.valueOf((Long.valueOf(o2.toString()) - Long.valueOf(o1.toString())));
	}

	private static boolean concatErr(StringBuilder sb, String phase, int i) {
		Object err = get(phase + i);
		if (err == null) {
			return false;
		}
		sb.append(",").append(err.toString()).append(",iter_end_time[").append(i).append("]:").append(cal(beginTime(), get(ITER_END_TIME + i))).append(",iter_cost[").append(i).append("]:").append(get(ITER_COST + i)).append("】");
		return true;
	}

	public static void flush(Logger logger) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("*****发送报文*****begin_time:").append(dateF.format(get(START_TIME))).append("|tran_code:").append(get(TXN_CODE)).append("|call_method:").append(get(TXN_ID));
			sb.append("|before_send_time:").append(cal(beginTime(), get(BEFORE_SEND)));
			sb.append("|iter_count:").append(currentIterCount());
			int ic = Integer.valueOf(get(ITER_COUNT).toString());
			for (int i = 0; i <= ic; i++) {
				sb.append("【 iter_time[").append(i).append("]:").append(cal(beginTime(), get(ITER_TIME + i))).append(",server:").append(get(ITER_SERVER + i)).append(",before_conn_time[").append(i).append("]:").append(cal(beginTime(), get(BEFORE_CONN + i)));
				if (concatErr(sb, ERR_CONN, i)) {
					continue;
				}
				sb.append(",before_write_time[").append(i).append("]:").append(cal(beginTime(), get(BEFORE_WRITE + i)));
				if (concatErr(sb, ERR_WRITE, i)) {
					continue;
				}
				sb.append(",before_read_time[").append(i).append("]:").append(cal(beginTime(), get(BEFORE_READ + i)));
				if (concatErr(sb, ERR_READ, i)) {
					continue;
				}
				sb.append(",iter_end_time[").append(i).append("]:").append(cal(beginTime(), get(ITER_END_TIME + i))).append(",iter_cost [").append(i).append("]:").append(get(ITER_COST + i)).append("】");
			}
			sb.append("|total_cost:").append(System.currentTimeMillis() - Long.valueOf(get(START_TIME).toString()));
			logger.debug(sb.toString());
		}
		clear();
	}
}
