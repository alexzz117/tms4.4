package cn.com.higinet.tms35.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.c3p0.ComboPooledDataSource;
import cn.com.higinet.rapid.base.dao.SqlMap;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_seq {
	static Logger log = LoggerFactory.getLogger(dao_seq.class);
	final static long isOffline = (3 == tmsapp.get_config("sync.isOn", 1)) ? 1 : 0;
	static String sqlSeqNext = ((SqlMap) bean.get("tmsSqlMap")).getSql("tms.common.sequenceid").replaceFirst("\\$\\{SEQUENCENAME\\}", "%s");
	volatile static List<dao_seq> list_ = null;
	//added by wujw,20151224 数据源重用
	private static data_source dataSource = null;

	/*
	 * 将规则和告警id增加1或者2功能
	 */
	public static long resetId(long orgId) {
		// return (orgId<<1)+isOffline;//建议使用这个，在线为偶数，离线为奇数
		// return Long.parseLong((isOffline == 1 ? "2" : "1") + orgId);//
		// 在线首位为1，离线首位为2
		return orgId;
	}

	public static void init(data_source ds) {
		
		//added by wujw,20151224
		dataSource = ds;

		if (list_ != null)
			return;

		final String sql = "select SEQ_NAME, SEQ_STEP from TMS_COM_SEQ order by SEQ_NAME";
		final List<dao_seq> list = new ArrayList<dao_seq>();

		batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
		try {
			stmt.query(new Object[] {}, new row_fetch() {
				public boolean fetch(ResultSet rs) throws SQLException {
					dao_seq seq = new dao_seq();
					seq.m_seq_name = rs.getString(1);
					seq.m_step = rs.getInt(2);
					list.add(seq);
					return true;
				}
			});
		} catch (SQLException e) {
			log.error(null, e);
		} finally {
			stmt.close();
		}

		list_ = new ArrayList<dao_seq>(list);
	}

	long m_step;
	String m_seq_name;
	long m_db_next;
	long m_cur_id;

	dao_seq() {
		m_db_next = 0;
		m_cur_id = 0;
	}

	dao_seq(String seq_name) {
		m_seq_name = seq_name;
		m_db_next = 0;
		m_cur_id = 0;
	}

	static java.util.Comparator<dao_seq> comp_by_name = new Comparator<dao_seq>() {
		public int compare(dao_seq o1, dao_seq o2) {
			return o1.m_seq_name.compareTo(o2.m_seq_name);
		}
	};

	synchronized public long next() {
		
		if (m_cur_id >= m_db_next * m_step) {
			//m_db_next = db_next();
			
			//added by wujw,20151224
			//数据源重用
			if (null == dataSource) {
				dataSource = new data_source();
			}
			m_db_next = db_next(dataSource);
			m_cur_id = (m_db_next - 1) * m_step;
		}

		return ++m_cur_id;
	}

	synchronized public long next(data_source ds) {
		if (m_cur_id >= m_db_next * m_step) {
			m_db_next = db_next(ds);
			m_cur_id = (m_db_next - 1) * m_step;
		}
		return ++m_cur_id;
	}

	// 不直接new datasource,用全局的ds连接,
	//这个方法会不断新建连接,可能上一个ds空闲,不建议用这个方法
	private long db_next() {
		return db_next(new data_source());
	}

	private long db_next(data_source ds) {
		if (ds == null) {
			return db_next();
		}
		return db_next(ds, String.format(sqlSeqNext, m_seq_name));
	}

	public static long db_next(data_source ds, String seq_next) {
		if (ds == null) {
			ds = new data_source();
		}
		
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, seq_next, new int[] {});
		final AtomicLong seqid = new AtomicLong(0);
		try {
			stmt.query(new Object[] {}, new row_fetch() {
				public boolean fetch(ResultSet rs) throws SQLException {
					seqid.set(rs.getLong(1));
					return true;
				}
			});
		} catch (SQLException e) {
			log.error("-----sequence exception-------", e);
		} finally {
			stmt.close();
		}

		return seqid.get();
	}

	public static dao_seq get(String string) {
		int i = java.util.Collections.binarySearch(list_, new dao_seq(string), comp_by_name);
		if (i < 0)
			return null;
		return list_.get(i);
	}
}