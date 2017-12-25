package cn.com.higinet.tms.engine.run;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.comm.tm_tool;
import cn.com.higinet.tms.engine.comm.tms_exception;
import cn.com.higinet.tms.engine.core.cache.db_fd;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.cache.db_tab_ref;
import cn.com.higinet.tms.engine.core.cache.linear;
import cn.com.higinet.tms.engine.core.cache.str_id;
import cn.com.higinet.tms.engine.core.cache.txn_ref_stat;
import cn.com.higinet.tms.engine.core.cond.func_map;
import cn.com.higinet.tms.engine.core.cond.ip_tool;
import cn.com.higinet.tms.engine.core.dao.dao_combin;
import cn.com.higinet.tms.engine.core.dao.row_in_db_impl;

public final class run_txn_values extends row_in_db_impl {
	static Logger log = LoggerFactory.getLogger(run_txn_values.class);

	//private RepositoryManager repositoryManager = bean.get(RepositoryManager.class); //4.3新增的数据仓管理

	static class entry {
		public entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String toString() {
			return "[" + key + "," + value + "]";
		}

		String key, value;
	}

	public run_env m_env;

	public List<Object> m_txn_data = null;

	// 渠道传过来的数据初始化
	private void init_online_data(linear<entry> lin) {
		db_fd.cache dfc = m_env.field_cache();
		m_txn_data = new ArrayList<Object>(lin.size());
		linear<str_id> list = dfc.get_src_localid(m_env.id());
		//list = dfc.get_refname_localid(m_env.id());
		if (list == null) {
			String s = "模型错误：当前交易[" + m_env.get_txntype() + "]的源字段--目标字段的映射没有定义";
			log.error(s, new tms_exception(s));
			return;
		}
		long db_time = tm_tool.lctm_ms();
		int c = 0, clen = lin.size();
		int i = 0, ilen = list.size();
		int comp = 0;
		str_id si = null;
		entry ci = null;
		for (; i < ilen && c < clen;) {
			si = list.get(i);
			db_fd fd = m_env.get_fd_def(si.id);
			ci = lin.get(c);
			comp = si.s != null ? ci.key.compareTo(si.s) : 1;
			if (comp < 0) {
				// System.out.println("发现了没有在模型中定义的数据:" + ci);
				c++;
			} else if (comp > 0) {
				set_fd(si.id, fd.srcdefault);
				i++;
			} else {
				// 对于单常量，使用先初始化，再运行的方式
				// 如果涉及到类型不同的，可能会出现问题...
				for (; si.s.equals(ci.key);) {
					Object o = null;
					if (fd.node != null) {
						o = fd.node.exec(m_env);
						if (func_map.is_nothing(o))
							o = null;
					} else
						o = ci.value;

					set_fd(si.id, o);
					if (++i >= ilen)
						break;
					si = list.get(i);
					fd = m_env.get_fd_def(si.id);
				}
				++c;
			}
		}

		for (; i < ilen; i++) {
			si = list.get(i);
			db_fd fd = m_env.get_fd_def(si.id);
			set_fd(si.id, fd.srcdefault);
		}

		i = 0;
		for (int len = this.m_txn_data.size(); i < len; i++) {
			String d = str_tool.to_str(m_txn_data.get(i));
			if (d == null)
				continue;

			db_fd fd = m_env.get_fd_def(i);
			if (fd.is_numberic_type() && str_tool.is_empty(d)) {
				throw new tms_exception("数值、时间类数据不可以传空字符串'':", fd.toString());
			} else if (fd.is_ipaddr() && !ip_tool.is_valid(d)) {
				/*
				 * add lining 2015-07-05 POC测试修改 begin
				 * 校验IP地址不合法, 将数据设置为null
				 */
				d = null;
				//throw new tms_exception("IP地址不合法:", d, fd.toString());
				/* add lining 2015-07-05 end */
			} else if (!str_tool.is_empty(fd.fd_name) && fd.sql_type() == Types.VARCHAR && d.getBytes().length > fd.dblen) {
				/* add lining 增加fd_name是否为空判断，fd_name为空时，数据不存储 */
				throw new tms_exception(fd.toStringL() + ":" + "超出数据库定义长度。");
			}

			set_fd(i, db_fd.convert_in(fd.type, "VARCHAR2", d));
		}
		this.set_fd(dfc.INDEX_DEVSTATUS, m_env.get_device_status());
		this.set_fd(dfc.INDEX_USER_DEVSTATUS, m_env.get_user_device_status());
		this.set_fd(dfc.INDEX_TXNTYPE, m_env.get_txntype());
		//this.set_fd(dfc.INDEX_CHANCODE, m_env.get_txn_chancode());
		this.set_fd(dfc.INDEX_CREATETIME, db_time);
	}

	public void init_stat_data() {
		db_fd.cache dfc = m_env.get_txn().g_dc.field();
		db_stat.cache dsc = m_env.get_txn().g_dc.stat();
		linear<db_stat> list_stat = m_env.get_txn().m_stat;
		linear<txn_ref_stat> list_ref_stat = m_env.get_txn().m_ref_stat;
		for (int i = 0, len = list_ref_stat.size(); i < len; i++) {
			txn_ref_stat ref_stat = list_ref_stat.get(i);
			if (ref_stat.param.length > 0) {
				continue;
			}
			db_stat stat = dsc.get(ref_stat.st_index);
			if (stat.is_valid == 0 || str_tool.is_empty(stat.storecolumn) || !list_stat.m_list.contains(stat)) {
				continue;
			}
			int index = dfc.get_fdname_local_index(id(), stat.storecolumn);
			if (index > -1) {
				this.set_fd(index, m_env.get_stat_value(i, null));
			}
		}
	}

	public void update_ref_tabdata(run_db_commit rdc, dao_combin dc) throws Exception {
		for (db_tab_ref ref : m_env.get_txn().m_ref_tabs) {
			List<Integer> fields = m_env.get_txn().m_ref_tab_fields.get(ref.ref_id);
			update_ref_data(rdc, dc, ref, fields, m_env.get_txn().m_fd, m_env.get_txn().m_fd_refid);
		}
	}

	private void update_ref_data(run_db_commit rdc, dao_combin dc, db_tab_ref ref, List<Integer> fields, linear<db_fd> mFd, linear<str_id> mFdRefid) throws Exception {}

	private void init_ref_data(dao_combin dc) throws Exception {
		for (db_tab_ref ref : m_env.get_txn().m_ref_tabs) {
			List<Integer> fields = m_env.get_txn().m_ref_tab_fields.get(ref.ref_id);
			read_ref_data(dc, ref, fields, m_env.get_txn().m_fd);
		}
	}

	// sql其实可以在模型初始化时生成，以后优化~
	private void read_ref_data(dao_combin dc, db_tab_ref ref, List<Integer> fields, linear<db_fd> mFd) throws Exception {}

	public run_txn_values(run_env runEnv) {
		this.m_env = runEnv;
		m_txn_data = new ArrayList<Object>(32);
	}

	public run_txn_values() {
		m_txn_data = new ArrayList<Object>(32);
	}

	// 返回交易属性值
	public Object get_fd(int i) {
		if (i >= m_txn_data.size())
			return null;

		return m_txn_data.get(i);
	}

	public void set_fd(int i, Object object) {
		while (i >= m_txn_data.size())
			m_txn_data.add(null);
		m_txn_data.set(i, object);
	}

	public int id() {
		return m_env.id();
	}

	static final Comparator<entry> entry_comp = new Comparator<entry>() {
		public int compare(entry o1, entry o2) {
			return o1.key.compareTo(o2.key);
		}
	};

	public void init_data(dao_combin dc, Map<String, String> m) throws Exception {
		final linear<entry> lin = new linear<entry>(entry_comp);
		for (Entry<String, String> e : m.entrySet())
			lin.add(new entry(str_tool.upper_case(e.getKey()), str_tool.trim(e.getValue())));
		lin.sort();

		init_online_data(lin);
		init_ref_data(dc);
		// init_run_translate();

		// check_field_valid();
		check_cantnull();
	}

	private void check_cantnull(int index) {
		if (!str_tool.is_empty(str_tool.to_str(m_txn_data.get(index))))
			return;

		db_fd fd = m_env.get_fd_def(index);

		throw new tms_exception(fd.toStringL(), "不能为空");
	}

	private void check_cantnull() {
		// public int INDEX_TXNCODE; // 交易流水号,主键
		// public int INDEX_SESSIONID; // 会话标识
		// public int INDEX_CHANCODE; // 渠道代码
		// public int INDEX_TXNID; // 交易标识
		// public int INDEX_TXNTYPE; // 交易类型
		// public int INDEX_USERID; // 用户标识
		// public int INDEX_TXNTIME; // 交易发生时间
		// public int INDEX_FINISHTIME; // 交易确认时间
		// public int INDEX_CREATETIME; // 交易识别时间
		// public int INDEX_TXNSTATUS; // 交易状态
		// public int INDEX_SCORE; // 交易分值

		db_fd.cache dc = m_env.get_txn().g_dc.field();
		//check_cantnull(dc.INDEX_USERID);// 注册时, 用户ID没有
		check_cantnull(dc.INDEX_TXNID);
		if (m_env.is_confirm()) {
			check_cantnull(dc.INDEX_TXNSTATUS);
		}
	}

	int size() {
		if (this.m_txn_data == null)
			return 0;
		return this.m_txn_data.size();
	}

	public void extend(run_txn_values rf) {
		for (int i = 0, len = rf.size(); i < len; i++) {
			if (get_fd(i) != null)
				continue;
			if (rf.get_fd(i) == null)
				continue;

			/*if (i == m_env.field_cache().INDEX_SCORE) {
				continue;
			}*/

			this.set_fd(i, rf.get_fd(i));
		}
	}

	public String bast_tab() {
		return "TMS_RUN_TRAFFICDATA";
	}

	public boolean not_empty()//把is_empty修改成not_empty，这里判断的意义明显是非空，不知道谁起的名字叫is_empty  edit by王兴  2017-07-07
	{
		return size() > 0;
	}

	public String get_txn_code() {
		return m_env.get_txn_code();
	}
}