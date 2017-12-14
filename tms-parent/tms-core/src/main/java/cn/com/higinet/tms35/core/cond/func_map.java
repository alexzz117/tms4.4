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

import cn.com.higinet.tms35.comm.array_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tm_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.cache.db_cache;
import cn.com.higinet.tms35.core.cache.db_roster;
import cn.com.higinet.tms35.core.cache.ip_cache;
import cn.com.higinet.tms35.core.cond.func_impl.func_last_impl;
import cn.com.higinet.tms35.core.cond.func_impl.func_set_impl;
import cn.com.higinet.tms35.core.dao.dao_base;
import cn.com.higinet.tms35.run.run_env;

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

	static map_function[] map = {
			new map_function(op.add_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 0
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return ((Long) p[n + 0]) + ((Long) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.long_, op.double_ }, op.double_, new func()
			{// 1
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return ((Long) p[n + 0]) + ((Double) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.long_, op.string_ }, op.string_, new func()
			{// 2
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) + ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.double_, op.long_ }, op.double_, new func()
			{// 3
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Double) p[n + 0]) + ((Long) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.double_, op.double_ }, op.double_, new func()
			{// 4
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Double) p[n + 0]) + ((Double) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.double_, op.string_ }, op.string_, new func()
			{// 5
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Double) p[n + 0]) + ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.string_, op.long_ }, op.string_, new func()
			{// 6
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((String) p[n + 0]) + ((Long) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.string_, op.double_ }, op.string_, new func()
			{// 7
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((String) p[n + 0]) + ((Double) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.string_, op.string_ }, op.string_, new func()
			{// 8
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((String) p[n + 0]) + ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.string_, op.datetime_ }, op.string_,
					new func()
					{// 9
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((String) p[n + 0])
									+ date_tool.format(new java.util.Date((Long) p[n + 1]));
						}
					}),
			new map_function(op.add_, new byte[] { op.string_, op.time_ }, op.string_, new func()
			{// 10
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((String) p[n + 0])
									+ date_tool.format(new java.util.Date((Long) p[n + 1]));
						}
					}),
			new map_function(op.add_, new byte[] { op.datetime_, op.string_ }, op.string_,
					new func()
					{// 11
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.format(new java.util.Date((Long) p[n + 0]))
									+ ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.datetime_, op.span_ }, op.datetime_,
					new func()
					{// 12
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) + ((Long) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.time_, op.string_ }, op.string_, new func()
			{// 13
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.format(new java.util.Date((Long) p[n + 0]))
									+ ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.time_, op.span_ }, op.time_, new func()
			{// 14
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return date_tool.tm_time((((Long) p[n + 0]) + ((Long) p[n + 1])));
						}
					}),
			new map_function(op.add_, new byte[] { op.span_, op.string_ }, op.string_, new func()
			{// 15
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return "[span:" + ((Long) p[n + 0]) + "]" + ((String) p[n + 1]);
						}
					}),
			new map_function(op.add_, new byte[] { op.span_, op.span_ }, op.span_, new func()
			{
				// 16
				public Object exec(Object[] p, int n)
				{
					if (has_nothing(p, n, 2))
						return NOTHING;
					return ((Long) p[n + 0]) + ((Long) p[n + 1]);
				}
			}),

			new map_function(op.neg_, new byte[] { op.long_ }, op.long_, new func()
			{// 17
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return -((Long) p[n + 0]);
						}
					}),
			new map_function(op.neg_, new byte[] { op.span_ }, op.span_, new func()
			{// 18
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return -((Long) p[n + 0]);
						}
					}),
			new map_function(op.neg_, new byte[] { op.double_ }, op.double_, new func()
			{// 19
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return -((Double) p[n + 0]);
						}
					}),
			new map_function(op.sub_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 20
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) - ((Long) p[n + 1]);
						}
					}),
			new map_function(op.sub_, new byte[] { op.long_, op.double_ }, op.double_, new func()
			{// 21
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							BigDecimal b1 = new BigDecimal(Long.toString((Long) p[n + 0]));
							BigDecimal b2 = new BigDecimal(Double.toString((Double) p[n + 1]));
							return b1.subtract(b2).doubleValue();
						}
					}),
			new map_function(op.sub_, new byte[] { op.double_, op.long_ }, op.double_, new func()
			{// 22
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							BigDecimal b1 = new BigDecimal(((Number) p[n + 0]).doubleValue());
							BigDecimal b2 = new BigDecimal(Long.toString((Long) p[n + 1]));
							return b1.subtract(b2).doubleValue();
						}
					}),
			new map_function(op.sub_, new byte[] { op.double_, op.double_ }, op.double_, new func()
			{// 23
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							BigDecimal b1 = new BigDecimal((((Number) p[n + 0])).doubleValue());
							BigDecimal b2 = new BigDecimal(((Number) p[n + 1]).doubleValue());
							return b1.subtract(b2).doubleValue();
						}
					}),
			new map_function(op.sub_, new byte[] { op.datetime_, op.span_ }, op.datetime_,
					new func()
					{// 24
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) - ((Long) p[n + 1]);
						}
					}),
			new map_function(op.sub_, new byte[] { op.datetime_, op.datetime_ }, op.span_,
					new func()
					{// 25
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return (((Number) p[n + 0]).longValue()) - ((Number) p[n + 1]).longValue();
						}
					}),
			new map_function(op.sub_, new byte[] { op.time_, op.span_ }, op.time_, new func()
			{// 26
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.tm_time((((Long) p[n + 0]) - ((Long) p[n + 1])));
						}
					}),
			new map_function(op.sub_, new byte[] { op.span_, op.span_ }, op.span_, new func()
			{// 27
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) - ((Long) p[n + 1]);
						}
					}),

			new map_function(op.mul_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 28
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) * ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.long_, op.span_ }, op.span_, new func()
			{// 29
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) * ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.span_, op.long_ }, op.span_, new func()
			{// 30
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) * ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.long_, op.double_ }, op.double_, new func()
			{// 31
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) * ((Double) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.span_, op.double_ }, op.span_, new func()
			{// 32
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Long) p[n + 0]) * ((Double) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.double_, op.long_ }, op.double_, new func()
			{// 33
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Double) p[n + 0]) * ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.double_, op.span_ }, op.span_, new func()
			{// 34
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Double) p[n + 0]) * ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mul_, new byte[] { op.double_, op.double_ }, op.double_, new func()
			{// 35
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							BigDecimal b1 = new BigDecimal(Double.toString((Double) p[n + 0]));
							BigDecimal b2 = new BigDecimal(Double.toString((Double) p[n + 1]));
							return b1.multiply(b2).doubleValue();
						}
					}),

			new map_function(op.div_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 36
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Long) p[n + 0]) / ((Long) p[n + 1]);
						}
					}),
			new map_function(op.div_, new byte[] { op.long_, op.double_ }, op.double_, new func()
			{// 37
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							BigDecimal b1 = new BigDecimal(Long.toString((Long) p[n + 0]));
							BigDecimal b2 = new BigDecimal(Double.toString((Double) p[n + 1]));
							return b1.divide(b2, 15, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
					}),
			new map_function(op.div_, new byte[] { op.double_, op.long_ }, op.double_, new func()
			{// 38
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							BigDecimal b1 = new BigDecimal(((Number) p[n + 0]).doubleValue());
							BigDecimal b2 = new BigDecimal(((Number) p[n + 1]).longValue());
							return b1.divide(b2, 15, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
					}),
			new map_function(op.div_, new byte[] { op.double_, op.double_ }, op.double_, new func()
			{// 39
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							BigDecimal b1 = new BigDecimal(Double.toString((Double) p[n + 0]));
							BigDecimal b2 = new BigDecimal(Double.toString((Double) p[n + 1]));
							return b1.divide(b2, 15, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
					}),
			new map_function(op.div_, new byte[] { op.span_, op.long_ }, op.span_, new func()
			{// 40
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Long) p[n + 0]) / ((Long) p[n + 1]);
						}
					}),
			new map_function(op.div_, new byte[] { op.span_, op.double_ }, op.span_, new func()
			{// 41
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							BigDecimal b1 = new BigDecimal(Long.toString((Long) p[n + 0]));
							BigDecimal b2 = new BigDecimal(Double.toString((Double) p[n + 1]));
							return b1.divide(b2, 15, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
					}),

			new map_function(op.mod_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 42
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Long) p[n + 0]) % ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mod_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 43
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Long) p[n + 0]) % ((Double) p[n + 1]).longValue();
						}
					}),
			new map_function(op.mod_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 44
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Double) p[n + 0]).longValue() % ((Long) p[n + 1]);
						}
					}),
			new map_function(op.mod_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 45
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							if (((Number) p[n + 1]).doubleValue() == 0)
								throw new tms_exception("被零除");
							return ((Double) p[n + 0]).longValue()
									% ((Double) p[n + 1]).longValue();
						}
					}),

			new map_function(op.eq_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 46
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() == 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 47
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return Math.abs(((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue()) < 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 48
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return Math.abs(((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue()) < 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 49
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue()) < 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.string_, op.string_ }, op.long_, new func()
			{// 50
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return p[n + 0].equals(p[n + 1]) ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 51
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() == 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 52
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) == 0 ? TRUE
									: FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 53
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() == 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.eq_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 54
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() == 0 ? TRUE : FALSE;
						}
					}),

			new map_function(op.gt_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 55
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 56
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 57
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 58
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 59
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 60
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 61
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue() > 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.gt_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 62
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) > 0 ? TRUE
									: FALSE;
						}
					}),

			new map_function(op.lt_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 63
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 64
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 65
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 66
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 67
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 68
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 69
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue() < 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.lt_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 70
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) < 0 ? TRUE
									: FALSE;
						}
					}),

			new map_function(op.ge_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 71
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 72
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 73
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 74
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 75
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 76
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 77
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue() >= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ge_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 78
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) >= 0 ? TRUE
									: FALSE;
						}
					}),

			new map_function(op.le_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 79
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 80
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 81
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 82
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 83
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 84
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 85
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue() <= 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.le_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 86
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) <= 0 ? TRUE
									: FALSE;
						}
					}),

			new map_function(op.ne_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 87
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() != 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.long_, op.double_ }, op.long_, new func()
			{// 88
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).doubleValue()) > 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.double_, op.long_ }, op.long_, new func()
			{// 89
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).longValue()) > 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.double_, op.double_ }, op.long_, new func()
			{// 90
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).doubleValue()
									- ((Number) p[n + 1]).doubleValue()) > 1e-4 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.string_, op.string_ }, op.long_, new func()
			{// 91
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return !p[n + 0].equals(p[n + 1]) ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.datetime_, op.datetime_ }, op.long_,
					new func()
					{// 92
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() != 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.datetime_, op.time_ }, op.long_, new func()
			{// 93
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							return date_tool.tm_time(((Number) p[n + 0]).longValue())
									- date_tool.tm_time(((Number) p[n + 1]).longValue()) != 0 ? TRUE
									: FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.time_, op.time_ }, op.long_, new func()
			{// 94
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() != 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.ne_, new byte[] { op.span_, op.span_ }, op.long_, new func()
			{// 95
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;
							return ((Number) p[n + 0]).longValue()
									- ((Number) p[n + 1]).longValue() != 0 ? TRUE : FALSE;
						}
					}),

			new map_function(op.func_, "abs", new byte[] { op.long_ }, op.long_, new func()
			{// 96
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).longValue());
						}
					}),
			new map_function(op.func_, "abs", new byte[] { op.span_ }, op.span_, new func()
			{// 97
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).longValue());
						}
					}),
			new map_function(op.func_, "abs", new byte[] { op.double_ }, op.double_, new func()
			{// 98
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return Math.abs(((Number) p[n + 0]).doubleValue());
						}
					}),
			new map_function(op.func_, "tm_year", new byte[] { op.datetime_ }, op.long_, new func()
			{// 99
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getYear() + 1900;
						}
					}),
			new map_function(op.func_, "tm_month", new byte[] { op.datetime_ }, op.long_,
					new func()
					{// 100
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getMonth() + 1;
						}
					}),
			new map_function(op.func_, "tm_day", new byte[] { op.datetime_ }, op.long_, new func()
			{// 101
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getDate();
						}
					}),
			new map_function(op.func_, "tm_wday", new byte[] { op.datetime_ }, op.long_, new func()
			{// 102
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getDay();
						}
					}),
			new map_function(op.func_, "tm_hour", new byte[] { op.datetime_ }, op.long_, new func()
			{// 103
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getHours();
						}
					}),
			new map_function(op.func_, "tm_minute", new byte[] { op.datetime_ }, op.long_,
					new func()
					{// 104
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getMinutes();
						}
					}),
			new map_function(op.func_, "tm_second", new byte[] { op.datetime_ }, op.long_,
					new func()
					{// 105
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return (long) new Date(((Number) p[n + 0]).longValue()).getSeconds();
						}
					}),
			new map_function(op.func_, "tm_date", new byte[] { op.datetime_ }, op.datetime_,
					new func()
					{// 106
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;

							return date_tool.tm_date(((Number) p[n + 0]).longValue());
						}
					}),
			new map_function(op.func_, "tm_time", new byte[] { op.datetime_ }, op.time_, new func()
			{// 107
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;
							return date_tool.tm_time(((Number) p[n + 0]).longValue());
						}
					}),
			new map_function(op.func_, "nvl", new byte[] { op.any_, op.any_ }, op.any_, new func()
			{// 108
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n])) {
								if (is_nothing(p[n + 1])) {
									return NOTHING;
								} else {
									return p[n + 1];
								}
							}
							return p[n];
						}
					}),
			new map_function(op.func_, "set_impl", new byte[] { op.any_, op.any_}, op.long_, new func_set_impl()),
			new map_function(op.and_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 109
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]) && is_nothing(p[n + 1]))
								return NOTHING;
							if (is_nothing(p[n]))
								return (Long) p[n + 1] != 0 ? TRUE : FALSE;
							if (is_nothing(p[n + 1]))
								return (Long) p[n] != 0 ? TRUE : FALSE;

							return ((Long) p[n + 0]) * ((Long) p[n + 1]) != 0 ? TRUE : FALSE;
						}
					}),
			new map_function(op.or_, new byte[] { op.long_, op.long_ }, op.long_, new func()
			{// 110
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]) && is_nothing(p[n + 1]))
								return NOTHING;
							if (is_nothing(p[n]))
								return (Long) p[n + 1] != 0 ? TRUE : FALSE;
							if (is_nothing(p[n + 1]))
								return (Long) p[n] != 0 ? TRUE : FALSE;

							return ((Long) p[n + 0]) == 1 || ((Long) p[n + 1]) == 1 ? TRUE : FALSE;
						}
					}),
			new map_function(op.func_, "ip2isp", new byte[] { op.string_ }, op.string_, new func()
			{// 111

						public Object exec(Object[] p, int n)
						{
							return "";
						}

					}),
			new map_function(op.func_, "ip2city", new byte[] { op.string_ }, op.string_, new func()
			{// 112

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;

							return ip_cache.Instance().get_city(ip_cache.LOCATE_TYPE_IP, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "ip2region", new byte[] { op.string_ }, op.string_, new func()
			{// 113

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_region(ip_cache.LOCATE_TYPE_IP, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "ip2country", new byte[] { op.string_ }, op.string_, new func()
			{// 114

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_country(ip_cache.LOCATE_TYPE_IP, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "card2city", new byte[] { op.string_ }, op.string_, new func()
			{// 115

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_city(ip_cache.LOCATE_TYPE_CARD, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "card2region", new byte[] { op.string_ }, op.string_, new func()
			{// 116

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_region(ip_cache.LOCATE_TYPE_CARD, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "mobile2city", new byte[] { op.string_ }, op.string_, new func()
			{// 117

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_city(ip_cache.LOCATE_TYPE_MOBILE, str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "mobile2region", new byte[] { op.string_ }, op.string_, new func()
			{// 118

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							return ip_cache.Instance().get_region(ip_cache.LOCATE_TYPE_MOBILE, str_tool.to_str(p[n]));
						}
					}),
					

			/*new map_function(op.func_, "user2mobile", new byte[] { op.string_ }, op.string_, new func()
			{//
					public Object exec(Object[] p, int n)
					{
						if (is_nothing(p[n]))
							return NOTHING;

						String username = null;
						run_env re = (run_env) p[n - 1];
						String userId =str_tool.to_str(p[n]);
						if (null == userId) {
							log.debug("用户转手机号传入的用户标识USERID为空!");
							return null;
						}
						try {
							db_user duser = re.get_dao_combin().stmt_user.read(userId);
							username = null != duser ? duser.get("username") : null;
						} catch (SQLException e) {
							log.error("",e);
							e.printStackTrace();
							return null;
						}
						
						return username;
					}
				}),
				new map_function(op.func_, "card2code", new byte[] { op.string_ }, op.string_, new func()
				{//
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;

							String bankCode = null;
							run_env re = (run_env) p[n - 1];
							String accountId =str_tool.to_str(p[n]);
						
							if (null == accountId) {
								log.debug("卡号或银行账号转银行代码传入的卡号为空!");
								return null;
							}
							try {
								db_ref_row accrow = re.get_dao_combin().stmt_account.read(accountId);
								
								bankCode = null != accrow ? accrow.get("BANK_CODE") : null;
							} catch (SQLException e) {
								log.error("",e);
								e.printStackTrace();
								return null;
							}
							
							return bankCode;
						}
					}),		*/
			new map_function(op.in_, new byte[] { op.long_, op.table_ }, op.long_, new func()
			{// 119
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return FALSE;

							run_env re = (run_env) p[n - 1];
							db_roster.cache drc = re == null ? db_cache.get().roster() : re
									.get_txn().dc().roster();
							return drc.val_in((Number) p[n + 1], p[n], re.get_txn_time()) ? TRUE
									: FALSE;
						}
					}),
			new map_function(op.in_, new byte[] { op.string_, op.table_ }, op.long_, new func()
			{// 120

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return FALSE;

							run_env re = (run_env) p[n - 1];
							db_roster.cache drc = re == null ? db_cache.get().roster() : re
									.get_txn().dc().roster();
							return drc.val_in((Number) p[n + 1], p[n], re.get_txn_time()) ? TRUE
									: FALSE;
						}
					}),
			new map_function(op.notin_, new byte[] { op.long_, op.table_ }, op.long_, new func()
			{// 121

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;

							run_env re = (run_env) p[n - 1];
							db_roster.cache drc = re == null ? db_cache.get().roster() : re
									.get_txn().dc().roster();
							return drc.val_in((Number) p[n + 1], p[n], re.get_txn_time()) ? FALSE
									: TRUE;
						}
					}),
			new map_function(op.notin_, new byte[] { op.string_, op.table_ }, op.long_, new func()
			{// 122

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;

							run_env re = (run_env) p[n - 1];
							db_roster.cache drc = re == null ? db_cache.get().roster() : re
									.get_txn().dc().roster();
							return drc.val_in((Number) p[n + 1], p[n], re.get_txn_time()) ? FALSE
									: TRUE;
						}
					}),
			new map_function(op.func_, "last_impl", new byte[] { op.any_ }, op.any_,
					new func_last_impl()),//123
			new map_function(op.func_, "is_null", new byte[] { op.any_ }, op.long_, new func()
			{// 124

						public Object exec(Object[] p, int n)
						{
							return (p[n] == null || String.valueOf(p[n]).length() == 0) ? TRUE : FALSE;
						}
					}),
			new map_function(op.func_, "is_nothing", new byte[] { op.any_ }, op.long_, new func()
			{// 125

						public Object exec(Object[] p, int n)
						{
							return is_nothing(p[n]) ? TRUE : FALSE;
						}
					}),
			new map_function(op.func_, "is_empty", new byte[] { op.string_ }, op.long_, new func()
			{// 126
						public Object exec(Object[] p, int n)
						{
							if (p[n] == null)
								return TRUE;

							if (p[n] instanceof String && ((String) p[n]).length() == 0)
								return TRUE;

							return FALSE;
						}
					}),
			new map_function(op.func_, "not", new byte[] { op.any_ }, op.long_, new func()
			{// 127
						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return TRUE;

							if (p[n] instanceof String && ((String) p[n]).length() == 0)
								return TRUE;

							if (p[n] instanceof Number && ((Number) p[n]).doubleValue() == 0)
								return TRUE;

							return FALSE;
						}
					}),
			new map_function(op.func_, "truncate", new byte[] { op.string_, op.long_ }, op.string_, new func()
			{// 128

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;

							String s = str_tool.to_str(p[n]);
							int s1 = ((Number) p[n + 1]).intValue();
							
							if(s1>=s.length())
								return s;

							return s.substring(0, s1);
						}
					}),
			new map_function(op.func_, "trim", new byte[] { op.string_ }, op.string_, new func()
			{// 129

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							String s = str_tool.to_str(p[n]);
							return s.trim();
						}
					}),
			new map_function(op.func_, "len", new byte[] { op.string_ }, op.long_, new func()
			{// 130

						public Object exec(Object[] p, int n)
						{
							if (is_nothing(p[n]))
								return NOTHING;
							String s = str_tool.to_str(p[n]);
							return s.length();
						}
					}),
			new map_function(op.func_, "substr", new byte[] { op.string_, op.long_, op.long_ },
					op.string_, new func()
					{// 131
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 3))
								return NOTHING;

							String s = str_tool.to_str(p[n]);
							int s1 = ((Number) p[n + 1]).intValue();
							int s2 = ((Number) p[n + 2]).intValue();
							
							if(s1>=s.length())
								return NOTHING;
							
							if(s2>=s.length())
								s2=s.length();
							return s.substring(s1, s2);
						}
					}),
			new map_function(op.func_, "now", new byte[] {}, op.datetime_, new func()
			{// 132
						public Object exec(Object[] p, int n)
						{
							return new Long(tm_tool.lctm_ms());
						}
					}),

			new map_function(op.func_, "upper", new byte[] { op.string_ }, op.string_, new func()
			{// 133
						public Object exec(Object[] p, int n)
						{
							return str_tool.upper_case(str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "lower", new byte[] { op.string_ }, op.string_, new func()
			{// 134
						public Object exec(Object[] p, int n)
						{
							return str_tool.lower_case(str_tool.to_str(p[n]));
						}
					}),
			new map_function(op.func_, "index", new byte[] { op.string_, op.string_ }, op.long_,
					new func()
					{// 135
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 2))
								return NOTHING;

							String s1 = str_tool.to_str(p[n]);
							String s2 = str_tool.to_str(p[n + 1]);

							return new Long(s1.indexOf(s2));
						}
					}),
			new map_function(op.func_, "to_string", new byte[] { op.any_ }, op.string_, new func()
			{// 136
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;

							return str_tool.to_str(p[n]);
						}
					}),
			new map_function(op.func_, "to_long", new byte[] { op.any_ }, op.long_, new func()
			{// 137
						public Object exec(Object[] p, int n)
						{
							if (has_nothing(p, n, 1))
								return NOTHING;

							String s = str_tool.to_str(p[n]);
							return (long) Double.parseDouble(s);
						}
					}),
			new map_function(op.func_, "to_double", new byte[] { op.any_ }, op.double_, new func()
			{// 138
						public Object exec(Object[] p, int n)
						{
							Object o = p[n];
							if (is_nothing(o))
								return NOTHING;
							if (o instanceof Number)
								return ((Number) o).doubleValue();
							String s = str_tool.to_str(p[n]);
							return Double.parseDouble(s);
						}
					}),
			new map_function(op.func_, "to_datetime", new byte[] { op.any_ }, op.datetime_, new func()
			{// 139
				public Object exec(Object[] p, int n)
				{
					if (has_nothing(p, n, 1))
						return NOTHING;

					String s = str_tool.to_str(p[n]);
					return (long) Double.parseDouble(s);
				}
			}),
			new map_function(op.func_, "to_time", new byte[] { op.any_ }, op.time_, new func()
			{// 140
				public Object exec(Object[] p, int n)
				{
					if (has_nothing(p, n, 1))
						return NOTHING;

					String s = str_tool.to_str(p[n]);
					return (long) Double.parseDouble(s);
				}
			}),
			new map_function(op.func_, "ifelse", new byte[] { op.long_, op.any_, op.any_ }, op.any_, new func()
			{// 141
				public Object exec(Object[] p, int n)
				{
					Object c = p[n];
					if (is_nothing(c))
						return NOTHING;
					if ((Long) c == 1)
						return p[n + 1];
					else
						return p[n + 2];
				}
			}),
			new map_function(op.func_, "card2age", new byte[] { op.string_ }, op.long_, new func()
			{// 142

				public Object exec(Object[] p, int n)
				{
					if (is_nothing(p[n]))
						return NOTHING;
					String s = str_tool.to_str(p[n]);
					int leh = s.length();
					String dates = "";
					if (leh != 18 && leh != 15) {
						return NOTHING;
					}
					if (leh == 18) {
						dates = s.substring(6, 10) + "-" + s.substring(10, 12) + "-" + s.substring(12, 14);
					}
					else {
						dates = "19" + s.substring(6, 8) + "-" + s.substring(8, 10) + "-" + s.substring(10, 12);
					}
					
					if(date_tool.parse(dates) == null) {
						return NOTHING;
					}
					
					return (int) ((new Date().getTime()-date_tool.parse(dates).getTime())/1000/60/60/24/365);
				}
			}),
			new map_function(op.func_, "card2birthday", new byte[] { op.string_ }, op.datetime_, new func()
			{// 143
				public Object exec(Object[] p, int n)
				{
					if (is_nothing(p[n]))
						return NOTHING;
					String s = str_tool.to_str(p[n]);
					int leh = s.length();
					String dates = "";
					if (leh != 18 && leh != 15) {
						return NOTHING;
					}
					if (leh == 18) {
						dates = s.substring(6, 10) + "-" + s.substring(10, 12) + "-" + s.substring(12, 14);
					}
					else {
						dates = "19" + s.substring(6, 8) + "-" + s.substring(8, 10) + "-" + s.substring(10, 12);
					}
					
					return date_tool.parse(dates).getTime();
				}
			}),
			new map_function(op.func_, "pow", new byte[] {op.string_, op.long_}, op.double_, new func()
			{
				// 次方
				@Override
				public Object exec(Object[] p, int n) {
					if (has_nothing(p, n, 2)) {
						return new Double(0.d);
					}

					double d = Double.parseDouble(str_tool.to_str(p[n]));
					long d1 = ((Number) p[n + 1]).longValue();
					return Math.pow(d, d1);
				}
			}),
			new map_function(op.func_, "pow", new byte[] {op.double_, op.long_}, op.double_, new func()
			{
				// 次方
				@Override
				public Object exec(Object[] p, int n) {
					if (has_nothing(p, n, 2)) {
						return new Double(0.d);
					}

					double d = ((Number) p[n]).doubleValue();
					long d1 = ((Number) p[n + 1]).longValue();
					return Math.pow(d, d1);
				}
			}),
			new map_function(op.func_, "pow", new byte[] {op.long_, op.long_}, op.double_, new func()
			{
				// 次方
				@Override
				public Object exec(Object[] p, int n) {
					if (has_nothing(p, n, 2)) {
						return new Double(0.d);
					}

					double d = ((Number) p[n]).doubleValue();
					long d1 = ((Number) p[n + 1]).longValue();
					return Math.pow(d, d1);
				}
			}),
			new map_function(op.func_, "sqrt", new byte[] {op.double_}, op.double_, new func()
			{
				// 平方根
				@Override
				public Object exec(Object[] p, int n) {
					if (is_nothing(p[n])) {
						return new Double(0.d);
					}

					double d = ((Number) p[n]).doubleValue();
					return Math.sqrt(d);
				}
			}),
			new map_function(op.func_, "sqrt", new byte[] {op.long_}, op.double_, new func()
			{
				// 平方根
				@Override
				public Object exec(Object[] p, int n) {
					if (is_nothing(p[n])) {
						return new Double(0.d);
					}

					long d = ((Number) p[n]).longValue();
					return Math.sqrt(d);
				}
			}),
			new map_function(op.func_, "not_equal_sum", new byte[] { op.any_, op.any_, op.any_ ,op.any_}, op.long_, new func()
			{// 144
				public Object exec(Object[] p, int n)
				{
					Set<Object> data_s = new HashSet<Object>();
					for (int i = 0; i < 4; i++) {
						if(str_tool.is_empty(str_tool.to_str(p[n+i]))||is_nothing(p[n+i])) {
							continue;
						}
						data_s.add(p[n+i]);
					}
					
					return data_s.isEmpty() ? 0 : data_s.size();
				}
			}),
			new map_function(op.func_, "to_device", new byte[] { op.string_, op.long_ }, op.string_, load_class("func_device")),
			new map_function(op.func_, "add_roster", new byte[] { op.any_, op.table_ }, op.long_, load_class("func_roster")),
			new map_function(op.func_, "send_confirm", new byte[] { op.string_ }, op.long_, load_class("func_send_confirm"))
	// new type_map(notin_, new byte[] { OP.long_,id_ }, OP.long_,(byte)73),
	// new type_map(notin_, new byte[] { OP.string_,id_ }, OP.long_, (byte)74)

	};

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

		SqlRowSet rsf = bean.get(dao_base.class).query(
				"select FUNC_ID, FUNC_CODE," + "FUNC_TYPE,FUNC_CLASS" + " from TMS_COM_FUNC"
						+ " where FUNC_IS_ENABLE!=0 and FUNC_CLASS is not null" + " order by FUNC_ID");
		SqlRowSet rsp = bean.get(dao_base.class).query(
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