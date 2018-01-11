package cn.com.higinet.tms.base.util;

import java.util.HashMap;
import java.util.Map;

public class Timez {

	private long _time = System.currentTimeMillis();

	private static Map<String, Timez> timezs = new HashMap<String, Timez>();

	public static Timez getInstance() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String className = stack[stack.length - 1].getClassName();
		Timez timez = timezs.get( className );
		if( timez == null ) {
			timez = new Timez();
			timezs.put( className, timez );
		}
		return timez;
	}

	public static long start() {
		Timez.getInstance()._time = System.currentTimeMillis();
		return Timez.getInstance()._time;
	}

	public static long stop() {
		long now = System.currentTimeMillis();
		long end = now - Timez.getInstance()._time;
		Timez.getInstance()._time = now;
		return end;
	}

}
