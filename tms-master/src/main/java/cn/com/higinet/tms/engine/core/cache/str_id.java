package cn.com.higinet.tms.engine.core.cache;

import java.util.Comparator;

public final class str_id {
	public String s;
	public int id;

	public str_id(String s, int id) {
		this.s = s;
		this.id = id;
	}

	public String toString() {
		return "[" + s + "," + id + "]";
	}

	public static Comparator<str_id> comp_str = new Comparator<str_id>() {
		public int compare(str_id o1, str_id o2) {
			if (o1.s == null)
				return o2.s == null ? 0 : -1;

			if (o2.s == null)
				return 1;

			return o1.s.compareTo(o2.s);
		}
	};
};
