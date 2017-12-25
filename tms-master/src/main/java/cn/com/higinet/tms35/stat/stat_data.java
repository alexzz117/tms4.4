package cn.com.higinet.tms35.stat;

import java.util.Comparator;

import cn.com.higinet.tms35.comm.comp_tool;

public final class stat_data 
{
	public stat_data copy()
	{
		stat_data ret = new stat_data(this.m_id, this.m_param);
		if (m_value != null)
			ret.m_value = new String(m_value);

		return ret;
	}

	public stat_data(int statId, String statParam)
	{
		this(statId, statParam, null);
	}

	public stat_data(int mId, String mParam, String mValue)
	{
		m_param = mParam;
		m_value = mValue;
		m_id = mId;
	}

	private String m_param;
	private int m_id;
	private String m_value;// 统计数据

	public boolean equals(Object obj)
	{
		if (obj instanceof stat_data)
		{
			stat_data sd = (stat_data) obj;
			return m_id == sd.m_id && m_param.equals(sd.m_param);
		}
		return super.equals(obj);
	}

	public String get_param()
	{
		return m_param;
	}

	public String get_value()
	{
		return m_value;
	}

	public int get_id()
	{
		return m_id;
	}

	private int _hash_code;

	public int hashCode()
	{
		if (_hash_code != 0)
			return _hash_code;

		int ret = m_id;
		char[] s = m_param.toCharArray();
		for (int i = 0, len = s.length; i < len; i++)
		{
			int t = 0;
			for (; i < len && s[i] != txn_stat.ch_split; i++)
				t = (t << 1) ^ s[i];

			ret = ret ^ t;
		}

		return _hash_code = (ret == 0 ? 1 : ret);
	}

	public static Comparator<stat_data> comp = new Comparator<stat_data>()
	{
		public int compare(stat_data o1, stat_data o2)
		{
			int i = o1.m_param.compareTo(o2.m_param);
			if (i != 0)
				return i;
			return comp_tool.comp(o1.m_id , o2.m_id);
		}
	};

	public static void main(String[] args)
	{
		System.out.println(new stat_data(1, "12345-12344-sdfjkl;dfjkl;sdf").hashCode());
	}

	public void set_value(String string)
	{
		m_value=string;
	}
}
