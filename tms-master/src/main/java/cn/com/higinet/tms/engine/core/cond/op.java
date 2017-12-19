package cn.com.higinet.tms.engine.core.cond;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class op
{
	static final Logger log = LoggerFactory.getLogger(op.class);
	static public final byte long_ = 1;
	static public final byte double_ = 2;
	static public final byte time_ = 3;
	static public final byte datetime_ = 4;
	static public final byte span_ = 5;
	static public final byte string_ = 6;
	static public final byte add_ = 7;
	static public final byte neg_ = 8;
	static public final byte sub_ = 9;
	static public final byte mul_ = 10;
	static public final byte div_ = 11;
	static public final byte mod_ = 12;
	static public final byte eq_ = 13;
	static public final byte ne_ = 14;
	static public final byte lt_ = 15;
	static public final byte le_ = 16;
	static public final byte gt_ = 17;
	static public final byte ge_ = 18;
	static public final byte and_ = 19;
	static public final byte or_ = 20;
	static public final byte in_ = 21;
	static public final byte notin_ = 22;
	static public final byte id_ = 23;
	final static public byte func_ = 24;
	static public final byte stat_ = 25;
	static public final byte table_ = 26;
	static public final byte param_ = 27;
	static public final byte const_ = 28;
	static public final byte any_ = 29;
	static public final byte stat_param_ = 30;
	static public final byte rule_ = 31;
	static public final byte op_end = 32;

	static final String[] op_name = new String[] { //
	"null", "long", "double", "time", "datetime",// 
			"span", "string", "+", "-", "-", //
			"*", "/", "%", "=", "!=", //
			"<", "<=", ">", ">=", "and", //
			"or", "in", "notin", "id", "func", //
			"stat", "table", "param", "const", "any",//
			"stat_param_", "rule", "op_end" };

	static final String[] op_name_loc = new String[] { //
	"空值", "整数", "浮点数", "时间", "日期",// 
			"时间段", "字符串", "加上", "减去", "负的", //
			"乘以", "除以", "取余数", "等于", "不等于", //
			"小于", "小于等于", "大于", "大于等于", "并且", //
			"或者", "包含于", "不包含于", "标示符", "函数", //
			"统计", "名单", "函数参数", "常量", "any",//
			"统计参数", "规则", "op_end" };

	// static final String[] op_name = new String[] { "null", "long", "double",
	// "string", "datetime", "time", "span",
	// "add", "neg", "sub", "mul", "div", "mod", "eq", "ne", "lt", "le", "gt",
	// "ge", "and", "or", "in", "notin",
	// "id", "func", "stat", "table", "param",
	// "const","any","stat_param_","rule","op_end" };

	static class type_map implements Comparable<type_map>
	{

		public String tms_type_name;
		public byte cond_type;

		type_map(String name, int type)
		{
			tms_type_name = name;
			cond_type = (byte) type;
		}

		public int compareTo(type_map o)
		{
			return tms_type_name.compareTo(o.tms_type_name);
		}
	}

	static type_map[] g_tm = new type_map[] { new type_map("STRING", string_),
			new type_map("INT", long_), new type_map("LONG", long_),
			new type_map("FLOAT", double_), new type_map("DOUBLE", double_),
			new type_map("MONEY", double_), new type_map("TIME", time_),
			new type_map("DATETIME", datetime_), new type_map("DEVID", string_),
			new type_map("IP", string_), new type_map("USERID", string_),
			new type_map("ACC", string_), new type_map("CODE", string_) };

	static
	{
		Arrays.sort(g_tm);
	}

	static public byte name2type(String name)
	{
		return g_tm[Arrays.binarySearch(g_tm, new type_map(name, 0))].cond_type;
	}
	
	static public String type2name(int type) {
	    byte b_type = (byte) type;
	    if (b_type == double_) {
	        return "DOUBLE";
	    } else if (b_type == long_) {
	        return "LONG";
	    } else if (b_type == datetime_) {
	        return "DATETIME";
	    } else if (b_type == time_) {
	        return "TIME";
	    } else {
	        return "STRING";
	    }
	}

	// static List<String> g_func = new ArrayList<String>();
	// static {
	// Arrays.sort(map);
	// for (int i = 0, len = map.length; i < len; i++) {
	// if (map[i].op == func_)
	// g_func.add(map[i].name);
	// }
	//
	// Collections.sort(g_func);
	// List tmp = new ArrayList<String>(g_func.size());
	// tmp.add(g_func.get(0));
	// for (int i = 0, len = g_func.size(); i < len; i++) {
	// if (tmp.get(tmp.size() - 1).equals(g_func.get(i)))
	// continue;
	// tmp.add(g_func.get(i));
	// }
	//
	// g_func = tmp;
	// }
	//
	// public static boolean is_func(String name) {
	// return Collections.binarySearch(g_func, name) >= 0;
	// }

	// static byte[] fun_type=new byte[op_end];
	// static
	// {
	// Arrays.fill(fun_type, (byte)-1);
	// type_map m=null;
	//		
	// for(int i=0,len=map.length;i<len;i++)
	// {
	// m=map[i];
	// if(fun_type[m.op]==-1)
	// {
	// fun_type[m.op]=m.type;
	// for(i++;i<len && m.op==map[i].op;++i)
	// {
	// if(fun_type[m.op]!=map[i].type)
	// fun_type[m.op]=-1;
	// }
	// }
	// }
	// System.out.println(fun_type);
	// }
}
