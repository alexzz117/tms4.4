package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.op;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

final public class db_fd
{
	static Logger log = LoggerFactory.getLogger(db_fd.class);
	public static final String CVT_FD_PREFIX="__CHAN__";

	static Comparator<db_fd> comp_by_tabname_refname = new Comparator<db_fd>()
	{
		public int compare(db_fd o1, db_fd o2)
		{
			int c = o1.tab_name.compareTo(o2.tab_name);
			if (c != 0)
				return c;

			return o1.ref_name.compareTo(o2.ref_name);
		}
	};

	static Comparator<db_fd> comp_by_refname = new Comparator<db_fd>()
	{
		public int compare(db_fd o1, db_fd o2)
		{
			return o1.ref_name.compareTo(o2.ref_name);
		}
	};

	static public class cache
	{
		public int INDEX_TXNCODE; // 交易流水号,主键
		public int INDEX_SESSIONID; // 会话标识
		public int INDEX_CHANCODE; // 渠道代码
		public int INDEX_TXNID; // 交易标识
		public int INDEX_TXNTYPE; // 交易类型
		public int INDEX_USERID; // 用户标识
		public int INDEX_TXNTIME; // 交易发生时间
		public int INDEX_FINISHTIME; // 交易确认时间
		public int INDEX_CREATETIME; // 交易识别时间
		public int INDEX_TXNSTATUS; // 交易状态
		public int INDEX_SCORE; // 交易分值
		public int INDEX_ISEVAL; // 是否进行风险评估，0-只记录日志，1-进行风险评估
		public int INDEX_TRIGRULENUM; // 规则触发数
		public int INDEX_HITRULENUM; // 规则命中数
		public int INDEX_DISPOSAL; // 处置方式
		public int INDEX_ISCORRECT; // 是否正确
		public int INDEX_IS_MAIN; // IS_MAIN
		public int INDEX_ISRISK; // 是否有风险
		public int INDEX_CONFIRMRISK; // 人工确认有无风险字段，0-无风险，1-有风险
		public int INDEX_ISMODELRISK; // 是否模型风险，0-否，1-是
		public int INDEX_MODELID; // 模型ID

		//不是所有的渠道都有着几个属性，项目中可以进行定制
		public int INDEX_DEVID; // 设备ID
		public int INDEX_DEVSTATUS; // 设备状态：null-不做设备识别，0=新设备，>0 && <1 匹配度，-1=可疑设备
		public int INDEX_USER_DEVSTATUS;// 用户设备绑定关系    0 使用过未绑定   1 绑定  2未使用过 
		public int INDEX_IP; // IP地址
		public int INDEX_COUNTRY; // 国家
		public int INDEX_REGION; // 省区
		public int INDEX_CITY; // 城市
		
		public int INDEX_STRATEGY; // 策略
		public int INDEX_PSSTATUS; // 处理状态 
		
		linear<db_fd> list_ = new linear<db_fd>(comp_by_tabname_refname); // tab_name,ref_name字典顺序
		linear<linear<db_fd>> list_txnid_localid = new linear<linear<db_fd>>();
		linear<linear<str_id>> list_txnid_srcid; // 一级索引为txn_id,二级索引为src_id
		linear<linear<str_id>> list_txnid_refname; // 一级索引为txn_id,二级索引为ref_name
		linear<linear<str_id>> list_txnid_fdname; // 一级索引为txn_id,二级索引为fd_name

		db_fd_ref.cache db_fd_ref_cache;

		public db_fd_ref.cache get_fd_ref_cache()
		{
			return db_fd_ref_cache;
		}

		// 交易视图字段，包括交易字段和签约字段
		// 一级索引为txn_id
		// 二级索引无，使用原生顺序，交易字段在前，引用字段在后

		public int get_local_index(int txnid, String ref_name)
		{
			linear<str_id> lin = list_txnid_refname.get(txnid);
			if (lin == null)
				return -1;
			str_id si = lin.get(new str_id(ref_name, 0));
			return si == null ? -1 : si.id;
		}
		
		public int get_fdname_local_index(int txnid, String fd_name)
		{
			linear<str_id> lin = list_txnid_fdname.get(txnid);
			if (lin == null)
				return -1;
			str_id si = lin.get(new str_id(fd_name, 0));
			return si == null ? -1 : si.id;
		}

		public db_fd get(String tab_name, String ref_name)
		{
			return list_.get(mk_this(tab_name, ref_name));
		}

		public db_fd get(int txnid, String ref_name)
		{
			int i = get_local_index(txnid, ref_name);
			if (i < 0)
				return null;
			return i >= 0 ? get(txnid, i) : null;
		}

		public linear<db_fd> get_txn_fields(int txnid)
		{
			return list_txnid_localid.get(txnid);
		}

		public db_fd get(int txnid, int local_index)
		{
			linear<db_fd> t = list_txnid_localid.get(txnid);
			if (t == null)
				return null;
			return t.get(local_index);
		}

		// 交易识别时使用，一个遍历即可对应出所有的字段
		public linear<str_id> get_src_localid(int txnid)
		{
			return list_txnid_srcid.get(txnid);
		}

		// 引用字段
		public linear<str_id> get_refname_localid(int txnid)
		{
			return list_txnid_refname.get(txnid);
		}

		public linear<str_id> get_fdname_localid(int txnid)
		{
			return list_txnid_fdname.get(txnid);
		}

		static public cache load(data_source ds, db_tab.cache dtc, db_tab_ref.cache dtrc)
		{
			cache c = new cache();
			c.init_base_fds(ds, dtc);
			c.init_txn_fields(ds, dtc, dtrc);
			return c;
		}

		public void clear()
		{
			if (list_ != null)
				list_.clear();
			if (list_txnid_localid != null)
				list_txnid_localid.clear();
			if (list_txnid_refname != null)
				list_txnid_refname.clear();
			if (list_txnid_srcid != null)
				list_txnid_srcid.clear();
		}

		public void init_txn_fields(data_source ds, db_tab.cache dtc, db_tab_ref.cache dtrc)
		{
			list_txnid_localid = new linear<linear<db_fd>>();
			list_txnid_srcid = new linear<linear<str_id>>();
			list_txnid_refname = new linear<linear<str_id>>();
			list_txnid_fdname = new linear<linear<str_id>>();

			db_fd_ref.cache dfrc = db_fd_ref.cache.load(ds, dtc, this, dtrc);
			db_fd_ref_cache = dfrc;
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;

				linear<db_fd> list1 = new linear<db_fd>(init_txn_fields(dtc, dfrc, tab));
				linear<str_id> list2 = new linear<str_id>(str_id.comp_str, list1.size());
				linear<str_id> list3 = new linear<str_id>(str_id.comp_str, list1.size());
				linear<str_id> list4 = new linear<str_id>(str_id.comp_str, list1.size());
				for (int x = 0, xlen = list1.size(); x < xlen; x++)
				{
					db_fd fd = list1.get(x);
					if (fd.is_txn_field())
					{
						list2.add(new str_id(fd.srcid, x));
						list4.add(new str_id(fd.fd_name, x));
					}
					else if (fd.ref != null && !str_tool.is_empty(fd.ref.storecolumn))
					{
						list4.add(new str_id(fd.ref.storecolumn, x));
					}
					list3.add(new str_id(list1.get(x).ref_name, x));
				}

				list2.sort();
				list3.sort();
				list4.sort();

				list_txnid_localid.set(tab.index, list1);
				list_txnid_srcid.set(tab.index, list2);
				list_txnid_refname.set(tab.index, list3);
				list_txnid_fdname.set(tab.index, list4);
			}

			db_tab tab = dtc.get("T");

			INDEX_SESSIONID = init_field_index(tab.index, "SESSIONID");
			INDEX_TXNID = init_field_index(tab.index, "TXNID");
			INDEX_TXNCODE = init_field_index(tab.index, "TXNCODE");
			INDEX_TXNTYPE = init_field_index(tab.index, "TXNTYPE");
			INDEX_USERID = init_field_index(tab.index, "USERID");
			INDEX_TXNTIME = init_field_index(tab.index, "TXNTIME");
			INDEX_FINISHTIME = init_field_index(tab.index, "FINISHTIME");
			INDEX_CREATETIME = init_field_index(tab.index, "CREATETIME");
			INDEX_TXNSTATUS = init_field_index(tab.index, "TXNSTATUS");
			INDEX_CHANCODE = init_field_index(tab.index, "CHANCODE");
			INDEX_SCORE = init_field_index(tab.index, "SCORE");
			
			INDEX_ISEVAL = init_field_index(tab.index, "ISEVAL");
			INDEX_TRIGRULENUM = init_field_index(tab.index, "TRIGRULENUM");
			INDEX_HITRULENUM = init_field_index(tab.index, "HITRULENUM");
			INDEX_DISPOSAL = init_field_index(tab.index, "DISPOSAL");
			INDEX_ISCORRECT = init_field_index(tab.index, "ISCORRECT");
			INDEX_IS_MAIN = init_field_index(tab.index, "IS_MAIN");
			INDEX_ISRISK = init_field_index(tab.index, "ISRISK");
			INDEX_CONFIRMRISK = init_field_index(tab.index, "CONFIRMRISK");
			INDEX_ISMODELRISK = init_field_index(tab.index, "ISMODELRISK");
			INDEX_MODELID = init_field_index(tab.index, "MODELID");

			INDEX_IP = init_field_index(tab.index, "IPADDR");
			INDEX_COUNTRY = init_field_index(tab.index, "COUNTRYCODE");
			INDEX_REGION = init_field_index(tab.index, "REGIONCODE");
			INDEX_CITY = init_field_index(tab.index, "CITYCODE");
			INDEX_DEVID = init_field_index(tab.index, "DEVICEID");
			INDEX_PSSTATUS = init_field_index(tab.index, "PSSTATUS");
			INDEX_STRATEGY = init_field_index(tab.index, "STRATEGY");
			if (db_device.IS_DEVFINGER_ON) {
				INDEX_DEVSTATUS = init_field_index(tab.index, "DEVICESTATUS");
				INDEX_USER_DEVSTATUS = init_field_index(tab.index, "USERDEVICESTATUS");
			}
		}
		
		public void reload_stat_fdname(db_tab.cache dtc, db_stat.cache dsc)
		{
			for (int i = 0, len = dtc.tab_count(); i < len; i++)
			{
				db_tab tab = dtc.get(i);
				if (!tab.is_txnview())
					continue;
				linear<db_fd> list = dsc.get_txn_storefd(tab);
				linear<str_id> list1 = list_txnid_fdname.get(tab.index);
				int count = list_txnid_localid.get(tab.index).size();
				for (int c = 0, clen = list.size(); c < clen; c++)
				{
					if (list.get(c).is_txn_field())
					{
						list1.add(new str_id(list.get(c).fd_name, (count + c)));
					}
				}
				list1.sort();
			}
		}

		private int init_field_index(int tab_index, String ref_name)
		{
			int ret = get_local_index(tab_index, ref_name);
			if (ret < 0)
				throw new tms_exception("没有找到模型中必须定义的字段：", ref_name);

			return ret;
		}

		private linear<db_fd> init_txn_fields(db_tab.cache dtc, db_fd_ref.cache dfrc, db_tab tab)
		{
			linear<db_fd> list = new linear<db_fd>();
			if (!str_tool.is_empty(tab.parent_tab))// 根交易表
			{
				db_tab ptab = dtc.get(tab.parent_tab);
				linear<db_fd> pfds = list_txnid_localid.get(ptab.index);
				for (db_fd fd : pfds) {
					list.add(new db_fd(fd));
				}
				//list.addAll(list_txnid_localid.get(ptab.index));
			}

			get_txn_fields(tab.tab_name, list);

			dfrc.get_txn_refields(tab, list);
			return list;
		}

		void get_txn_fields(String tab_name, linear<db_fd> list)
		{
			int first = list_.lower_bound(mk_this(tab_name, ""));
			int last = list_.upper_bound(mk_this(tab_name, str_tool.MAX_VALUE));
			list.addAll(list_.sub(first, last));
		}

		public void init_base_fds(data_source ds, final db_tab.cache dtc)
		{
			String sql = "select TAB_NAME, FD_NAME, NAME, REF_NAME,FD_DESC, TYPE, DB_TYPE, DB_LEN, "
					+ "CODE, IS_KEY, IS_NULL, IS_LIST, IS_QUERY, ORDERBY, LINK, SRC_ID, "
					+ "SRC_TYPE, SRC_DEFAULT, GENESISRUL, TXN_ORDER "
					+ "from TMS_COM_FD "
					+ "order by TAB_NAME,FD_NAME";

			final cache This = this;
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_fd fd = new db_fd();
						fd.belong = This;
						fd.is_key = rs.getInt("IS_KEY");
						fd.is_query = rs.getInt("IS_QUERY");
						fd.is_list = rs.getInt("IS_LIST");
						fd.orderby = rs.getInt("ORDERBY");
						fd.txn_order = rs.getInt("TXN_ORDER");

						fd.is_null = rs.getInt("IS_NULL");
						fd.dblen = rs.getInt("DB_LEN");
						fd.tab_name = str_tool.upper_case(rs.getString("TAB_NAME"));
						fd.fd_name = str_tool.upper_case(rs.getString("FD_NAME"));
						fd.ref_name = str_tool.upper_case(rs.getString("REF_NAME"));
						fd.name = rs.getString("NAME");
						fd.desc = rs.getString("FD_DESC");
						fd.type = str_tool.upper_case(rs.getString("TYPE"));
						fd.dbtype = str_tool.upper_case(rs.getString("DB_TYPE"));
						fd.code = rs.getString("CODE");
						fd.cvt_func = rs.getString("GENESISRUL");
						fd.link = rs.getString("LINK");
						fd.srcid = str_tool.upper_case(rs.getString("SRC_ID"));
						fd.srcdefault = rs.getString("SRC_DEFAULT");

						db_tab tab = dtc.get(fd.tab_name);
						if (tab == null)
							throw new tms_exception("字段", fd.toString(), "初始化，没有找到'", fd.tab_name,
									"'的定义");
						fd.tab_id = tab.index;

						if (!str_tool.is_empty(fd.cvt_func))
							fd.node = cond_parser.build(fd.cvt_func.replaceAll("\\?", CVT_FD_PREFIX+fd.srcid));

						if (tab.is_txnview())
							fd.is_txn_field = 1;
						else
							fd.is_txn_field = 0;

						list_.add(fd);
						
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_fd_cache error.");
			}
			finally
			{
				stmt.close();
			}
		}

		final static db_fd mk_this(String tabname, String ref_name)
		{
			return new db_fd(tabname, ref_name);
		}

		public db_fd get_key(String tab_name)
		{

			int first = list_.lower_bound(mk_this(tab_name, ""));
			int last = list_.upper_bound(mk_this(tab_name, str_tool.MAX_VALUE));

			for (; first < last; first++)
			{
				if (list_.get(first).is_key != 0)
					return list_.get(first);
			}

			return null;
		}

		public linear<db_fd> get_tab_fields(String tab_name)
		{
			int first = list_.lower_bound(mk_this(tab_name, ""));
			int last = list_.upper_bound(mk_this(tab_name, str_tool.MAX_VALUE));
			return list_.sub(first, last);
		}

		public linear<db_fd> all()
		{
			return this.list_;
		}
	}

	public db_fd()
	{
	}

	public db_fd(String tab_name, String ref_name)
	{
		this.tab_name = tab_name;
		this.ref_name = ref_name;
	}

	public db_fd(db_fd_ref ref)
	{
		this.ref = ref;
		this.tab_id = ref.ref_fd.tab_id;
		this.tab_name = ref.ref_fd.tab_name;
		this.ref_name = ref.ref_name;
		this.type = ref.ref_fd.type;
		this.belong = ref.ref_fd.belong;
	}
	
	public db_fd(db_tab tab, db_stat stat)
	{
		this.tab_name = tab.tab_name;
		this.tab_id = tab.index;
		this.fd_name = stat.storecolumn;
		if (!str_tool.is_empty(this.fd_name))
		{
			this.is_txn_field = 1;
		}
		this.type = stat.type();
	}
	
	public db_fd(db_fd fd) {
		this.is_key = fd.is_key;
		this.is_sys = fd.is_sys;
		this.is_query = fd.is_query;
		this.is_list = fd.is_list;
		this.orderby = fd.orderby;
		this.txn_order = fd.txn_order;
		this.is_null = fd.is_null;
		this.dblen = fd.dblen;
		this.tab_name = fd.tab_name;
		this.fd_name = fd.fd_name;
		this.ref_name = fd.ref_name;
		this.name = fd.name;
		this.desc = fd.desc;
		this.type = fd.type;
		this.dbtype = fd.dbtype;
		this.code = fd.code;
		this.cvt_func = fd.cvt_func;
		this.link = fd.link;
		this.srcid = fd.srcid;
		this.srcdefault = fd.srcdefault;
		this.is_txn_field = fd.is_txn_field;
		this.tab_id = fd.tab_id;
		this.ref = fd.ref;
		if (fd.node != null) {
			this.node = new node(fd.node.m_op);
			this.node.copy_from(fd.node);
		}
		this.belong = fd.belong;
	}

	public int is_key; // 是否主键
	public int is_sys; // 字段类型（系统字段、用户字段）
	public int is_query; // 是否可被系统自定义查询作为条件
	public int is_list; // 是否可被自定义查询显示
	public int orderby; // 显示、查询时的序号
	public int txn_order; // 模型中的字段排序
	public int is_null; // 是否允许为空
	public int dblen; // 字段字节长度

	public String tab_name; // 表名称，交易流水表名称
	public String fd_name; // 字段数据名称,用户定义公式时的引用名
	public String ref_name; // 用户定义公式时的引用名，缺省为fd_name
	public String name; // 字段显示名称
	public String desc; // 字段描述性文字，说明
	public String type; // 系统数据类型
	public String dbtype; // 数据库类型
	public String code; // 该字段引用的代码类型
	public String cvt_func; // 数据转换函数
	public String link; // 该字段所描述对象的显示视图描述
	public String srcid; // 数据来源名称
	public String srcdefault; // 数据来源为空时的缺省值

	public int is_txn_field;
	public int tab_id;

	public db_fd_ref ref;

	public node node;// 转换函数编译后的节点

	cache belong;

	public String toString()
	{
		return "[" + fd_name + "," + ref_name + "," + type + "]";
	}

	public String toStringL()
	{
		return "[" + this.name + "," + fd_name + "," + srcid + "," + type + "(" + dblen + ")]";
	}

	public int get_index(int txnid)
	{
		return belong.get_local_index(txnid, ref_name);
	}

	public boolean is_txn_field()
	{
		return is_txn_field == 1;
	}

	static TreeMap<String, Integer> map_sql_type = new TreeMap<String, Integer>();
	static
	{
		map_sql_type.put("STRING", Types.VARCHAR);
		map_sql_type.put("INT", Types.INTEGER);
		map_sql_type.put("LONG", Types.BIGINT);
		map_sql_type.put("FLOAT", Types.DOUBLE);
		map_sql_type.put("DOUBLE", Types.DOUBLE);
		map_sql_type.put("MONEY", Types.DOUBLE);
		map_sql_type.put("TIME", Types.BIGINT);
		map_sql_type.put("DATETIME", Types.BIGINT);
		map_sql_type.put("IP", Types.VARCHAR);
		map_sql_type.put("DEVID", Types.VARCHAR);
		map_sql_type.put("USERID", Types.VARCHAR);
		map_sql_type.put("ACC", Types.VARCHAR);
		map_sql_type.put("CODE", Types.VARCHAR);
	}

	static public Object convert_in(String type, String dbtype, String value)
	{
		if (value == null)
			return null;

		switch (op.name2type(type))
		{
		case op.long_:
			if (value.length() == 0)
				return null;
			return new Long(value);
		case op.time_:
		case op.datetime_:
		{
			if (value.length() == 0)
				return null;

			if (dbtype.equals("VARCHAR2"))
				return new Long(date_tool.parse(value).getTime());

			if (dbtype.equals("NUMBER"))
				return new Long(value);

			return new Long(date_tool.parse(value).getTime());
		}
		case op.double_:
			if (value.length() == 0)
				return null;
			return new Double(value);
		}

		return value;
	}

	static public Object convert_out(String type, String dbtype, Object value)
	{
		if (value == null)
			return null;

		switch (op.name2type(type))
		{
		case op.time_:
		case op.datetime_:
		{
			if (dbtype.toUpperCase().equals("VARCHAR2"))
				return date_tool.format(new Date(((Number) value).longValue()));
		}
		}

		return value;
	}

	static public int sql_type(String typename)
	{
		return map_sql_type.get(typename);
	}

	public Integer sql_type()
	{
		return sql_type(this.type);
	}

	public boolean is_numberic_type()
	{
		return op.name2type(this.type) < op.string_;
	}

	public boolean is_ipaddr()
	{
		return this.type.equals("IP");
	}
}
