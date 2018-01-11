package cn.com.higinet.tms.base.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 计时工具类
 * */
public class Clockz {

	private static Map<String, Long> timezs = new LinkedHashMap<String, Long>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry( Map.Entry<String, Long> eldest ) {
			return size() > 10000;
		}
	};

	public static Long start() {
		Long time = System.currentTimeMillis();
		timezs.put( Stringz.valueOf( Thread.currentThread().getId() ), time );
		return time;
	}

	public static Long stop() {
		String threadId = Stringz.valueOf( Thread.currentThread().getId() );
		return System.currentTimeMillis() - timezs.remove( threadId );
	}

	public static Long start( String key ) {
		Long time = System.currentTimeMillis();
		timezs.put( key, time );
		return time;
	}

	public static Long stop( String key ) {
		return System.currentTimeMillis() - timezs.remove( key );
	}
}
