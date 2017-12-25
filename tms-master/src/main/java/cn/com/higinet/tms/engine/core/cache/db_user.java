package cn.com.higinet.tms.engine.core.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.bin_stream;
import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.dao.row_in_db_impl;

public class db_user extends row_in_db_impl
{
	
	private static final Logger log = LoggerFactory.getLogger( db_user.class );
	static class_field g_field = new class_field();
	static
	{
		for (Field f : db_user.class.getFields())
			g_field.add_field(f);
	}

	public db_user()
	{
	}

	public db_user(String userid)
	{
		this.userid = userid;
	}

	public String userid;// varchar2(32) 用户id
	public String username;// varchar2(32) 用户名
	public String cusname;// varchar2(32) 客户姓名
	public String certno;// varchar2(32) 证件号码
	public String certtype;// varchar2(2) 证件类型
	public String cellphoneno;// varchar2(16) 手机号码
	public String opennode;// varchar2(8) 渠道用户开户行
	public String opentime;// varchar2(32) 开户时间
	public String acctype;// char(1) 用户类型
	public String sex;// char(1) 性别
	public String birthdate;// varchar2(32) 出生日期
	public String risktype;// varchar2(128) 条件类型
	public double riskscore;// number(15) 风险分值
	public String opennodecity;// varchar2(6)
	public double sumscore;// number(10) 累计分值
	public int norisknum;// number(20) 连续无风险次数

	public void add_sum_score(double score)
	{
		sumscore += score;
	}

	public static void main(String[] a)
	{
	}

	public String get(String fdName)
	{
		return str_tool.to_str(g_field.get(this, str_tool.lower_case(fdName)));
	}

	public void put(String fd_name, String s)
	{
		g_field.set(this, str_tool.lower_case(fd_name), s);
	}

	public static db_user load_from(bin_stream bs)
	{
		db_user d = new db_user();
		d.set_indb(bs.load_bool());
		d.userid = bs.load_string();// varchar2(32) 用户id
		d.username = bs.load_string();// varchar2(32) 用户名
		d.cusname = bs.load_string();// varchar2(32) 客户姓名
		d.certno = bs.load_string();// varchar2(32) 证件号码
		d.certtype = bs.load_string();// varchar2(2) 证件类型
		d.cellphoneno = bs.load_string();// varchar2(16) 手机号码
		d.opennode = bs.load_string();// varchar2(8) 渠道用户开户行
		d.opentime = bs.load_string();// varchar2(32) 开户时间
		d.acctype = bs.load_string();// char(1) 用户类型
		d.sex = bs.load_string();// char(1) 性别
		d.birthdate = bs.load_string();// varchar2(32) 出生日期
		d.risktype = bs.load_string();// varchar2(128) 条件类型
		d.riskscore = bs.load_double();// varchar2(32) 风险分值
		d.opennodecity = bs.load_string();// varchar2(6)
		d.sumscore = bs.load_double();// number(10) 累计分值
		d.norisknum = bs.load_int();// number(20) 连续无风险次数
		return d;
	}

	public void save_to(bin_stream bs)
	{
		bs.save(this.is_indb()).save(userid)// varchar2(32) 用户id
				.save(username)// varchar2(32) 用户名
				.save(cusname)// varchar2(32) 客户姓名
				.save(certno)// varchar2(32) 证件号码
				.save(certtype)// varchar2(2) 证件类型
				.save(cellphoneno)// varchar2(16) 手机号码
				.save(opennode)// varchar2(8) 渠道用户开户行
				.save(opentime)// varchar2(32) 开户时间
				.save(acctype)// char(1) 用户类型
				.save(sex)// char(1) 性别
				.save(birthdate)// varchar2(32) 出生日期
				.save(risktype)// varchar2(128) 条件类型
				.save(riskscore)// varchar2(32) 风险分值
				.save(opennodecity)// varchar2(6)
				.save(sumscore)// number(10) 累计分值
				.save(norisknum);// number(20) 连续无风险次数
	}

	public static void save_to(bin_stream bs, List<db_user> list)
	{
		bs.save(list.size());
		for (int i = 0, len = list.size(); i < len; i++)
			list.get(i).save_to(bs);
	}

	static public List<db_user> load_from_list(bin_stream bs)
	{
		List<db_user> list = null;
		int n = bs.load_int();
		if (n > 0)
		{
			list = new ArrayList<db_user>();
			for (int i = 0; i < n; i++)
			{
				db_user sess = db_user.load_from(bs);
				list.add(sess);
			}
		}
		return list;
	}
}
