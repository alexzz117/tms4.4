package cn.com.higinet.tms.core.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.com.higinet.tms.core.common.func_if;

public class JsonUtil {
	/**
	 * 将对象转换为JSON格式
	 * @param obj	需要转换的对象
	 * @return
	 */
	public static String formObject(Object obj) {
		return formObject(obj, new func_if<Object>() {
			public boolean _if(Object o) {
				return true;
			}
		});
	}

	/**
	 * 将对象转换为JSON格式
	 * @param obj	需要转换的对象
	 * @param func	判断条件，满足条件才会拼接到json中
	 * @return
	 */
	public static String formObject(Object obj, func_if<Object> func) {
		if (obj == null || !func._if(obj))
			return null;
		Class<?> classes = obj.getClass();
		String name = classes.getName();
		StringBuilder sb = new StringBuilder(1024);
		if (obj instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) obj;
			Iterator<?> it = map.entrySet().iterator();
			sb.append('{');
			while (it.hasNext()) {
				Entry<?, ?> en = (Entry<?, ?>) it.next();
				Object key = en.getKey();
				Object value = en.getValue();
				if (func._if(value)) {
					sb.append(toJsonElement(key)).append(':').append(toJsonElement(value)).append(',');
				}
			}
			if (sb.length() > 1) {
				sb.setCharAt((sb.length() - 1), '}');
			} else {
				sb.append('}');
			}
		} else if (obj instanceof Collection) {
			Collection<?> c = (Collection<?>) obj;
			Iterator<?> it = c.iterator();
			sb.append('[');
			while (it.hasNext()) {
				Object o = it.next();
				if (func._if(o)) {
					sb.append(toJsonElement(o)).append(',');
				}
			}
			if (sb.length() > 1) {
				sb.setCharAt((sb.length() - 1), ']');
			} else {
				sb.append(']');
			}
		} else if (obj instanceof Object[]) {
			Object[] os = (Object[]) obj;
			sb.append('[');
			for (int i = 0, len = os.length; i < len; i++) {
				Object o = os[i];
				if (func._if(o)) {
					sb.append(toJsonElement(o)).append(',');
				}
			}
			if (sb.length() > 1) {
				sb.setCharAt((sb.length() - 1), ']');
			} else {
				sb.append(']');
			}
		} else if (name.startsWith("cn.com.higinet.tms")) {
			// 内部java bean
			Field[] fds = classes.getDeclaredFields();
			sb.append('{');
			for (int i = 0, len = fds.length; i < len; i++) {
				try {
					Field fd = fds[i];
					if (!fd.isAccessible()) {
						fd.setAccessible(true);
					}
					String fname = fd.getName();
					Object value = fd.get(obj);
					if (func._if(value)) {
						sb.append(toJsonElement(fname)).append(':').append(toJsonElement(value)).append(',');
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (sb.length() > 1) {
				sb.setCharAt((sb.length() - 1), '}');
			} else {
				sb.append('}');
			}
		} else if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
			sb.append(toJsonElement(obj));
		} else {
			System.err.println("unsupported type: " + obj);
		}
		return sb.toString();
	}
	
	private static String toJsonElement(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof String) {
			return "\"" + obj + "\"";
		} else if (obj instanceof Number || obj instanceof Boolean) {
			return "" + obj;
		} else {
			return formObject(obj);
		}
	}
}