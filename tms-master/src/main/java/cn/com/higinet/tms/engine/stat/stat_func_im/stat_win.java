package cn.com.higinet.tms.engine.stat.stat_func_im;

import cn.com.higinet.tms.engine.stat.stat_number_encode;
import cn.com.higinet.tms.engine.stat.stat_win_base;
import cn.com.higinet.tms.engine.stat.stat_win_fac;

class stat_win_S extends stat_win_base
{
	public static final stat_win_S _fac = new stat_win_S();

	String sV;

	stat_win_S()
	{
	}

	stat_win_S(int t, String c)
	{
		super.win_time = t;
		sV = c;
	}

	@Override
	public Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time)
	{
		stat_win_S ret = new stat_win_S(stat_number_encode.decode_int(items[0]) + base_time,
				items.length > 1 ? items[1] : "");
		ret.fac = buff;
		return ret;
	}

	@Override
	public void appendTo(StringBuffer sb, int baseTime)
	{
		sb.append(stat_number_encode.encode(win_time - baseTime)).append(':').append(sV).append('|');
	}

	static final stat_win_S[] _array = new stat_win_S[0];

	@Override
	public Object[] Array()
	{
		return _array;
	}
}

class stat_win_L extends stat_win_base
{
	public static final stat_win_L _fac = new stat_win_L();
	static final stat_win_L[] _array = new stat_win_L[0];

	@Override
	public Object[] Array()
	{
		return _array;
	}

	int lV;

	stat_win_L()
	{
	}

	stat_win_L(int t, int c)
	{
		super.win_time = t;
		lV = c;
	}

	@Override
	public void appendTo(StringBuffer sb, int baseTime)
	{
		sb.append(stat_number_encode.encode(win_time - baseTime)).append(':')//
				.append(stat_number_encode.encode(lV)).append('|');
	}

	@Override
	public Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time)
	{
		stat_win_L ret = new stat_win_L(stat_number_encode.decode_int(items[0]) + base_time,
				stat_number_encode.decode_int(items[1]));
		ret.fac = buff;
		return ret;
	}
}

class stat_win_D extends stat_win_base
{
	public static final stat_win_D _fac = new stat_win_D();
	static final stat_win_D[] _array = new stat_win_D[0];

	@Override
	public Object[] Array()
	{
		return _array;
	}

	stat_win_D()
	{
	}

	stat_win_D(int t, double c)
	{
		super.win_time = t;
		dV = c;
	}

	double dV;

	@Override
	public void appendTo(StringBuffer sb, int baseTime)
	{
		sb.append(stat_number_encode.encode(win_time - baseTime)).append(':').append(dV).append('|');
	}

	@Override
	public Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time)
	{
		stat_win_D ret = new stat_win_D(stat_number_encode.decode_int(items[0]) + base_time,
				Double.parseDouble(items[1]));
		ret.fac = buff;
		return ret;
	}
}

class stat_win_LD extends stat_win_base
{
	public static final stat_win_LD _fac = new stat_win_LD();
	static final stat_win_LD[] _array = new stat_win_LD[0];

	@Override
	public Object[] Array()
	{
		return _array;
	}

	stat_win_LD()
	{
	}

	stat_win_LD(int t, int c, double v)
	{
		super.win_time = t;
		lV = c;
		dV = v;
	}

	int lV;
	double dV;

	@Override
	public void appendTo(StringBuffer sb, int baseTime)
	{
		sb.append(stat_number_encode.encode(win_time - baseTime))//
				.append(':')//
				.append(stat_number_encode.encode(lV)).append(':').append(dV)//
				.append('|');
	}

	@Override
	public Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time)
	{
		stat_win_LD ret = new stat_win_LD(stat_number_encode.decode_int(items[0]) + base_time, //
				stat_number_encode.decode_int(items[1]), Double.parseDouble(items[2]));
		ret.fac = buff;
		return ret;
	}
}

class stat_win_LS extends stat_win_base
{
	public static final stat_win_LS _fac = new stat_win_LS();
	static final stat_win_LS[] _array = new stat_win_LS[0];

	@Override
	public Object[] Array()
	{
		return _array;
	}

	stat_win_LS()
	{
	}

	stat_win_LS(int t, int c, String v)
	{
		super.win_time = t;
		lV = c;
		sV = v;
	}

	int lV;
	String sV;

	@Override
	public void appendTo(StringBuffer sb, int baseTime)
	{
		sb.append(stat_number_encode.encode(super.win_time - baseTime))//
				.append(':')//
				.append(stat_number_encode.encode(lV)).append(':').append(sV).append('|');
	}

	@Override
	public Object from(String[] items, stat_win_fac<stat_win_base> buff, int base_time)
	{
		stat_win_LS ret = new stat_win_LS(stat_number_encode.decode_int(items[0]) + base_time, //
				stat_number_encode.decode_int(items[1]), items[2]);
		ret.fac = buff;
		return ret;

	}
}
