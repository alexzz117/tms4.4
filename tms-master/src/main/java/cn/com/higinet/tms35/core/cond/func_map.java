package cn.com.higinet.tms35.core.cond;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import cn.com.higinet.tms.manager.common.ApplicationContextUtil;
import cn.com.higinet.tms35.comm.array_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.dao.dao_base;

@SuppressWarnings("deprecation")
public class func_map
{
	static Logger log = LoggerFactory.getLogger(func_map.class);

	static final Long TRUE = new Long(1);
	static final Long FALSE = new Long(0);

	static class Nothing
	{
		public String toString()
		{
			return "Nothing";
		}
	}

	public static final Nothing NOTHING = new Nothing();

	static public boolean is_true(Object o)
	{
		if (is_nothing(o))
			return false;

		return !FALSE.equals(o);
	}

	static class map_function implements Comparable<Object>
	{
		public map_function(byte op, byte[] param, byte ret, func func)
		{
			this.op = op;
			this.name = "";
			this.param_type = param;
			this.type = ret;
			this.fun_impl = func;
		}

		public map_function(byte op, String name, byte[] param, byte ret, func func)
		{
			this.op = op;
			this.name = name;
			this.param_type = param;
			this.type = ret;
			this.fun_impl = func;
		}

		map_function(byte op, byte[] param)
		{
			this(op, "", param);
		}

		map_function(byte op, String name, byte[] param)
		{
			this.op = op;
			this.name = name;
			this.param_type = param;
		}

		String name;
		byte[] param_type;
		short fun_index;
		byte op;
		byte type;
		func fun_impl;

		int param_count()
		{
			return param_type == null ? 0 : param_type.length;
		}

		public int compareTo(Object o)
		{
			map_function t = (map_function) o;
			if (op != t.op)
				return op - t.op;
			int c = name.compareTo(t.name);
			if (c != 0)
				return c;

			for (int i = 0, size = Math.min(param_count(), t.param_count()); i < size; i++)
			{
				if (param_type[i] != t.param_type[i])
					return param_type[i] - t.param_type[i];
			}

			return param_count() - t.param_count();
		}
	}

	static map_function get(byte _op, String name, byte[] type)
	{
		name = str_tool.upper_case(name);
		int ind = Arrays.binarySearch(map, new map_function(_op, name == null ? "" : name, type));
		if (ind >= 0)
			return map[ind];

		for (int i = type.length - 1; i >= 0; i--)
		{
			byte[] tmp = type.clone();
			tmp[i] = op.any_;
			ind = Arrays.binarySearch(map, new map_function(_op, name == null ? "" : name, tmp));
			if (ind >= 0)
				return map[ind];
			for (int n = i - 1; n >= 0; n--)
			{
				tmp[n] = op.any_;
				ind = Arrays.binarySearch(map, new map_function(_op, name == null ? "" : name, tmp));
				if (ind >= 0)
					return map[ind];
			}
		}

		return null;
	}

	public static boolean is_nothing(Object p)
	{
		return p == null || p == NOTHING;
	}

	public static boolean has_nothing(Object[] p, int start, int count)
	{
		for (int i = 0; i < count; i++)
		{
			if (is_nothing(p[start + i]))
				return true;
		}
		return false;
	}

	static map_function[] map = {};

	static func load_class(String clsName)
	{
		try
		{
			Class<?> c = java.lang.Class
					.forName("cn.com.higinet.tms35.core.cond.func_impl." + clsName);
			return (func) c.newInstance();
		}
		catch (Exception e)
		{
			log.error(null, e);
		}

		return null;
	}

	static void init_diy_func()
	{
		List<map_function> m = new ArrayList<map_function>(200);
		for (int i = 0, len = map.length; i < len; i++)
		{
			map_function mf = map[i];
			mf.name = str_tool.upper_case(mf.name);
			m.add(map[i]);
		}

		SqlRowSet rsf = ApplicationContextUtil.getBean(dao_base.class).query(
				"select FUNC_ID, FUNC_CODE," + "FUNC_TYPE,FUNC_CLASS" + " from TMS_COM_FUNC"
						+ " where FUNC_IS_ENABLE!=0 and FUNC_CLASS is not null" + " order by FUNC_ID");
		SqlRowSet rsp = ApplicationContextUtil.getBean(dao_base.class).query(
				"select fp.FUNC_ID, fp.PARAM_ID, fp.PARAM_TYPE" + " from TMS_COM_FUNCPARAM fp inner join TMS_COM_FUNC f"
						+ " on fp.FUNC_ID =f.FUNC_ID where f.FUNC_CLASS is not null order by FUNC_ID,PARAM_ORDERBY");

		byte[] param = new byte[20];
		int param_p = 0;
		while (rsf.next())
		{
			String cls_name = rsf.getString("FUNC_CLASS");
			if (str_tool.is_empty(cls_name))
				continue;

			int func_id = rsf.getInt("FUNC_ID");
			param_p = 0;
			for (; rsp.next();)
			{
				if (rsp.getInt("FUNC_ID") != func_id)
				{
					rsp.previous();
					break;
				}

				param[param_p++] = op.name2type(str_tool.upper_case(rsp.getString("PARAM_TYPE")));
			}

			map_function mf = new map_function(
					op.func_, //
					str_tool.upper_case(rsf.getString("FUNC_CODE")), //
					array_tool.copyOf(param, param_p), //
					op.name2type(str_tool.upper_case(rsf.getString("FUNC_TYPE"))),
					load_class(cls_name));
			m.add(mf);
		}

		java.util.Collections.sort(m);
		func_map.map = m.toArray(new map_function[] {});
		for (int i = 0; i < map.length; i++)
			map[i].fun_index = (short) i;
	}

	static public func get(int index)
	{
		if (index >= map.length)
			return null;
		return map[index].fun_impl;
	}

	static public func_map.map_function get_map(int index)
	{
		if (index < 0 || index >= map.length)
			return null;
		return map[index];
	}

	static
	{
		init_diy_func();
	}
}