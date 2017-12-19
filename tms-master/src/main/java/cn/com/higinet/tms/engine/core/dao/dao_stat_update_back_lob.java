package cn.com.higinet.tms.engine.core.dao;

import java.sql.SQLException;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.comm.utf8_split;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_stat_update_back_lob
{
	static final Logger log = LoggerFactory.getLogger(dao_stat_update_back_lob.class);
	static final String sql_insert = "insert into tms_run_stat_back (STAT_VALUE,STAT_VALUE2,STAT_LOB,STAT_PARAM) values(?,?,?,?)";

	batch_stmt_jdbc stmt;
	int m_batch_size, commit_count;

	public dao_stat_update_back_lob(data_source db, int batch_size)
	{
		commit_count = 0;
		m_batch_size = batch_size;
		stmt = new batch_stmt_jdbc(db, sql_insert,
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.VARCHAR });
	}

	final public boolean update(stat_value sv) throws SQLException
	{
		String v = sv.toString(true);
		if (!v.isEmpty())
		{
			utf8_split s = new utf8_split(v, 4000);

			if (s.s3 != null)
				log.warn("备份统计数据已经大于8000字节:stat_param=" + sv.m_param);

			stmt.update(new Object[] { s.s1, s.s2, s.s3, sv.m_param });
			commit_count++;
		}

		if (commit_count >= this.m_batch_size)
		{
			flush();
			return true;
		}

		return false;
	}

	final public void flush() throws SQLException
	{
		if (commit_count > 0)
		{
			stmt.flush();
			stmt.commit();
			commit_count = 0;
		}
	}

	public void close()
	{
		stmt.close();
	}

	final public void set_batch_size(int parseInt)
	{
		this.m_batch_size = parseInt;
	}
}