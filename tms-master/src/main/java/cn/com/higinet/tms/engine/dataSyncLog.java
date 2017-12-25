package cn.com.higinet.tms.engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.com.higinet.tms.engine.comm.JacksonMapper;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.comm.tmsapp;

/**
 * 数据同步日志打印
 * @author liining
 */
public class dataSyncLog implements Runnable {
	/**
	 * 是否开启打印数据同步日志
	 */
	public static final boolean isSyncLog = tmsapp.get_config("sync.log.isOn", 0) == 1;
	public static final String batch_name_split = "-";
	
	private static Log log = LogFactory.getLog(dataSyncLog.class);
	
	public void execute() {
	    if (isSyncLog)
	        new Thread(this).start();
	}

    @Override
    public void run() {
        print_log(get_batch_code("printlog-0", System.currentTimeMillis()), "test");
    }
	
	/**
	 * 获取批次号
	 * @param batch_name	批次名
	 * @param batch_number	批号-自增长序列
	 * @return
	 */
	public static String get_batch_code(String batch_name, long batch_number) {
		return (batch_name + batch_name_split + batch_number);
	}
	
	/**
	 * 批量打印开始
	 * @param batch_code	批次号
	 */
	public static void batch_print_begin(String batch_code) {
		print_log(batch_code, "begin");
	}
	
	/**
	 * 批量打印结束
	 * @param batch_code	批次号
	 * @param count			操作数量
	 * @param ac			结束动作
	 */
	public static void batch_print_end(String batch_code, long count, Action ac) {
		print_log(batch_code, (ac.getText() + " " + count + " end"));
	}
	
	
	
	/**
	 * 批量打印SQL语句日志
	 * @param batch_code	批次号
	 * @param sql			SQL语句
	 * @param param_type	SQL参数类型集合
	 * @param param_value	SQL参数值集合
	 */
	public static void batch_print_log(String batch_code, String sql, int[] param_type, Object[] param_value) {
		print_log(batch_code, sql, param_type, param_value);
	}
	
	/**
	 * 批量打印SQL语句日志
	 * @param batch_code	批次号
	 * @param sql			SQL语句
	 * @param param_value	SQL参数值集合
	 */
	public static void batch_print_log(String batch_code, String sql, Object param_value) {
		print_log(batch_code, sql, new int[] {}, param_value);
	}
	
	/**
	 * 打印SQL语句日志
	 * @param number		编号-自增长序列
	 * @param sql			SQL语句
	 * @param param_type	SQL参数类型集合
	 * @param param_value	SQL参数值集合
	 */
	public static void print_log(long number, String sql, int[] param_type, Object[] param_value) {
		print_log(String.valueOf(number), sql, param_type, param_value);
	}
	
	/**
	 * 打印SQL语句日志
	 * @param number		编号-自增长序列
	 * @param sql			SQL语句
	 * @param param_value	SQL参数值集合
	 */
	public static void print_log(long number, String sql, Object param_value) {
		print_log(String.valueOf(number), sql, new int[] {}, param_value);
	}
	
	/**
	 * 打印SQL语句日志
	 * @param head			日志标记头
	 * @param sql			SQL语句
	 * @param param_type	SQL参数类型集合
	 * @param param_value	SQL参数值集合
	 */
	private static void print_log(String head, String sql, int[] param_type, Object param_value) {
		if (isSyncLog) {
			try {
				sql = sql.replaceAll("\\n", "");
				Map<String, Object> logMap = new HashMap<String, Object>(2);
				logMap.put("sql", sql);
				if (param_value instanceof Map) {
					logMap.put("param", param_value);
				} else if (param_value instanceof Object[]) {
				    Map<String, Object> paramMap = new HashMap<String, Object>(2);
				    paramMap.put("type", param_type);
				    paramMap.put("value", param_value);
				    logMap.put("param", paramMap);
				}
				String body = JacksonMapper.getInstance().writeValueAsString(logMap);
				print_log(head, body);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 打印日志
	 * @param head	日志标记头
	 * @param body	日志内容体
	 */
	private static void print_log(String head, String body) {
		if (isSyncLog) {
			if (str_tool.is_empty(head) || str_tool.is_empty(body)) {
				throw new tms_exception(String.format(
						"数据同步日志的标记头[head]和内容体[body]都不能为空{head:%s, body:%s}", head, body));
			} else {
				log.info("[" + head + "]- " + body);
			}
		}
	}
	
    public static String toString(Object[] a, String split) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0;; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.append(']').toString();
            b.append(split).append(" ");
        }
    }

	public static enum Action {
		COMMIT("c", "commit"), ROLLBACK("r", "rollback");
		private final String value;
		private final String text;
		private static Map<String, Action> constants = new HashMap<String, Action>();
		static {
			for (Action c : Action.values()) {
				constants.put(c.value, c);
			}
		}

		private Action(String value, String text) {
			this.value = value;
			this.text = text;
		}

		public String getValue() {
			return this.value;
		}

		public String getText() {
			return this.text;
		}

		public static Action fromValue(String value) {
			Action constant = constants.get(value);
			return constant;
		}
	}
}