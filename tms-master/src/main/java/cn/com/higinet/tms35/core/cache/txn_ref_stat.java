package cn.com.higinet.tms35.core.cache;

import java.util.Comparator;

public class txn_ref_stat
{
	public static int REF_IN_ST = 1 << 0;
	public static int REF_IN_RULE = 1 << 1;
	public static int REF_IN_RULE_ACT = 1 << 2;
	public static int REF_IN_STAT = 1 << 3;

	public final static Comparator<txn_ref_stat> comp_by_id_param = new Comparator<txn_ref_stat>()
	{
		public int compare(txn_ref_stat o1, txn_ref_stat o2)
		{
			int c = o1.st_index - o2.st_index;
			if (c != 0)
				return c;

			c = o1.param.length - o2.param.length;
			if (c != 0)
				return c;

			for (int i = 0, len = o1.param.length; i < len; i++)
			{
				c = o1.param[i] - o2.param[i];
				if (c != 0)
					return c;
			}
			return 0;
		}
	};

	public final static Comparator<txn_ref_stat> comp_by_tab_id_param = new Comparator<txn_ref_stat>()
	{
		public int compare(txn_ref_stat o1, txn_ref_stat o2)
		{
			int c = o1.tab_name.compareTo(o2.tab_name);
			if (c != 0)
				return c;
			c = o1.st_index - o2.st_index;
			if (c != 0)
				return c;

			c = o1.param.length - o2.param.length;
			if (c != 0)
				return c;

			for (int i = 0, len = o1.param.length; i < len; i++)
			{
				c = o1.param[i] - o2.param[i];
				if (c != 0)
					return c;
			}
			return 0;
		}
	};

	public String tab_name;
	public int st_index;
	public int param[];
	public int ref_point;// 使用bit标识在哪个部分被引用
	
	public String toString()
	{
		return tab_name+":"+st_index+":"+ref_point+":"+Integer.toBinaryString(ref_point);
	}

	public txn_ref_stat(String tab_name, int st_index, int[] param)
	{
		this.tab_name = tab_name;
		this.st_index = st_index;
		this.param = param;
	}

	public void set_ref_point(int curRefPoint)
	{
		this.ref_point |= curRefPoint;
	}

	public boolean ref_in_risk_eval()
	{
		return (ref_point & REF_IN_RULE) != 0 || (ref_point & REF_IN_ST) != 0 || (ref_point & REF_IN_RULE_ACT) != 0;
	}

	public boolean ref_in_confirm()
	{
		return (ref_point & REF_IN_STAT) != 0 || (ref_point & REF_IN_RULE_ACT) != 0;
	}
}
