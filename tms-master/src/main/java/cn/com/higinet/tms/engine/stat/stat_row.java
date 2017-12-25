/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  stat_row.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 15:14:55   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.stat;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import cn.com.higinet.tms.engine.comm.str_tool;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.concurrent.counter;
import cn.com.higinet.tms.engine.core.dao.stat_value;

public final class stat_row implements Cloneable {
	public static final String SEPARATOR = "-";

	private static Logger logger = LoggerFactory.getLogger(stat_row.class);

	public static final int QUERY = 1;

	public static final int CLEAR = 2;

	public static final int UNION = 4;

	public static final int ROLLBACK = 8;

	public int m_row_type;

	public int m_stat_id;

	public String uniqId;//用于唯一ID stat_param+"-"+stat_id

	public String m_param;

	private String m_value; //当前统计的值

	public String m_txn_val; //交易流水中传入的统计值，累计统计时候用

	public long m_txn_time; //交易流水时间撮

	public boolean dirty = false; //是否有修改数据，如果有修改数据，则会进行持久化

	private stat_row existsRow;//用于累计统计，相同的persistenceId的stat_row对象只修改第一个row

	stat_value m_other_src;

	public counter m_batch;

	public stat_row() {
	}

	public stat_row(int row_type, String mParam, String value) {
		m_row_type = row_type;
		m_param = mParam;
		m_other_src = new stat_value(m_param, value);
	}

	public stat_row(int statId, String mParam, int row_type) {
		this(statId, mParam, row_type, 0, null);
	}

	public stat_row(int statId, String mParam, int row_type, long txnTime, String txn_value) {
		m_stat_id = statId;
		m_param = mParam;
		m_txn_time = txnTime;
		m_txn_val = txn_value;
		m_row_type = row_type;
		uniqId = mParam + SEPARATOR + m_stat_id;//4.3新增
	}

	public final boolean is_query() {
		return 0 != (m_row_type & QUERY);
	}

	public final boolean is_union() {
		return 0 != (m_row_type & UNION);
	}

	public final boolean is_rollback() {
		return 0 != (m_row_type & ROLLBACK);
	}

	public final boolean is_clear() {
		return 0 != (m_row_type & CLEAR);
	}

	public String toString() {
		return "[" + m_param + m_stat_id + "|" + ":" + m_value + "]";
	}

	public stat_row getExistsRow() {
		return existsRow;
	}

	public void setExistsRow(stat_row existsRow) {
		this.existsRow = existsRow;
	}

	public boolean hasExistsRow() {
		return existsRow != null;
	}

	/**
	 * 返回持久化的 ID，用于KV持久化.
	 *
	 * @return persistence ID 属性
	 */
	public String getUniqID() {
		return StringUtils.isEmpty(uniqId) ? m_param + "-" + m_stat_id : uniqId;
	}

	public String get_value() {
		return m_value;
	}

	public boolean set_value(String value) {
		if (value != m_value && ((value == null && m_value != null) || (value != null && m_value == null) || !value.equals(m_value))) {
			m_value = value;
			dirty = true;
			return true;
		}
		return false;
	}

	public counter getCounter() {
		return m_batch;
	}

	public int dec_batch() {
		int ret = -1;
		if (this.m_batch != null) {
			ret = m_batch.dec();
			m_batch = null;
		}
		return ret;
	}

	final public stat_value get_other_stat_value() {
		if (m_other_src == null)
			m_other_src = new stat_value(m_param);
		return m_other_src;
	}

	public static final Map.Entry<Integer, String> itget(Iterator<Map.Entry<Integer, String>> it) {
		if (it.hasNext())
			return it.next();

		return null;
	}

	final public void do_stat_lob(db_stat.cache dc) {
		String currentValue = existsRow != null ? existsRow.get_value() : m_value; //如果当前stat_row是在一个链中，那么当前用于累计值取上一个stat_row计算完的值，否则取自己的值 2017-08-21 王兴
		db_stat m_stat = dc.get_by_statid(m_stat_id);
		if (m_stat == null) {
			//m_stat_value.del(m_stat_id);
			logger.warn("统计ID=" + m_stat_id + ", 未找到对应的统计结构.");
			return;
		}
		String value = m_stat.func_st.set(currentValue, m_stat, (int) (m_txn_time / 1000 / 60), m_txn_val);
		if (existsRow != null) {
			if (existsRow.set_value(value)) {//如果返回true，表示已经修改目前已存在的stat_row的值
				dirty = true;
			}
		} else {
			set_value(value);
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	final static char item_sp = '\t';

	public void to_net_request(StringBuffer sb) {
		sb.append(stat_number_encode.encode(m_row_type)).append('\t');
		sb.append(m_param).append('\t');
		sb.append(stat_number_encode.encode(m_stat_id));
		if (is_query() || is_clear())
			sb.append('\n');
		else if (this.is_union() || this.is_rollback()) {
			String v = this.m_other_src.toString();
			sb.append('\t').append((str_tool.is_empty(v) ? v : v.replaceAll("\n", "\b\b")));
			sb.append('\n');
		} else {
			sb.append('\t');
			sb.append(stat_number_encode.encode(m_txn_time)).append('\t');
			if (m_txn_val == null)
				sb.append("");
			else
				sb.append("'").append(m_txn_val).append("'");
			sb.append('\n');
		}
	}

	public static stat_row from_net_request(String line, counter c) throws IOException {
		stat_row row = new stat_row();
		int from = row.read_stat_type(line, 0);
		from = row.read_stat_param(line, from);
		from = row.read_stat_id(line, from);

		if (row.is_query() || row.is_clear()) {
		} else if (row.is_union() || row.is_rollback()) {
			from = row.read_other_value(line, from);
		} else {
			from = row.read_stat_txn_time(line, from);
			from = row.read_txn_value(line, from);
		}
		row.m_batch = c;
		c.inc();
		return row;
	}

	final int read_stat_type(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		m_row_type = stat_number_encode.decode_int(line.substring(from, p));
		return p + 1;
	}

	final int read_stat_id(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		m_stat_id = stat_number_encode.decode_int(line.substring(from, p));
		return p + 1;
	}

	final int read_stat_param(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		m_param = line.substring(from, p);
		return p + 1;
	}

	final int read_stat_value(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		set_value(line.substring(from, p));
		return p + 1;
	}

	final int read_other_value(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		String v = line.substring(from, p);
		this.m_other_src = new stat_value(this.m_param, (str_tool.is_empty(v) ? v : v.replaceAll("\b\b", "\n")));
		return p + 1;
	}

	final int read_txn_value(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p < from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		String v = line.substring(from, p);
		m_txn_val = (v.length() == 0 ? null : v.substring(1, v.length() - 1));
		return p + 1;
	}

	final int read_stat_txn_time(String line, int from) throws IOException {
		int p = line.indexOf(item_sp, from);
		if (p < 0)
			p = line.length();
		if (p <= from)
			throw new IOException("分析统计元素'stat_row_type'出错");

		m_txn_time = stat_number_encode.decode_long(line.substring(from, p));

		return p + 1;
	}

	public void set_counter(counter counter) {
		m_batch = counter;
		if (m_batch != null)
			m_batch.inc();
	}

	public void set_error() {
		if (m_batch == null)
			return;

		m_batch.set_error(-1);
	}
}