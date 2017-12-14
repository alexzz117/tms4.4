package cn.com.higinet.tms.core.survivor;

/**
 * 用于保存数据的事件上下文，支持部分操作，避免method上过多入参
 * 
 * @author WX
 *
 */
public class EventContextHolder {
	private static final ThreadLocal<EventContext> local = new ThreadLocal<EventContext>();

	public static void setContext(EventContext e) {
		local.set(e);
	}

	public static void removeContext() {
		local.remove();
	}

	public static EventContext currentContext() {
		return local.get();
	}

}
