package cn.com.higinet.tms35.core.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service("dao_base_tmp")
public class dao_base_tmp
{

	@Autowired
	private JdbcTemplate tmpJdbcTemplate;

	public int update(String sql)
	{
		return tmpJdbcTemplate.update(sql);
	}

	public int update(String sql, Object[] params)
	{
		return tmpJdbcTemplate.update(sql, params);
	}

	public int[] batch_update(String sql, final Object[] params, final int column)
	{
		return tmpJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
		{
			public void setValues(PreparedStatement ps, int i) throws SQLException
			{
				for (int b = i * column, c = 0; c < column; c++, b++)
					ps.setObject(c + 1, params[b]);
			}

			public int getBatchSize()
			{
				return params.length / column;
			}
		});
	}

	public int[] batch_update(String sql, final List<Object[]> listParam)
	{
		return tmpJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
		{
			public void setValues(PreparedStatement ps, int i) throws SQLException
			{
				Object[] params = listParam.get(i);
				for (int j = 0; j < params.length; j++)
					ps.setObject(j + 1, params[j]);
			}

			public int getBatchSize()
			{
				return listParam.size();
			}
		});
	}

	public int[] batch_update(String sql, final List<Object> params, final int column)
	{
		return tmpJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter()
		{
			public void setValues(PreparedStatement ps, int i) throws SQLException
			{
				for (int b = i * column, c = 0; c < column; c++, b++)
					ps.setObject(c + 1, params.get(b));
			}

			public int getBatchSize()
			{
				return params.size() / column;
			}
		});
	}

	public int setFetchSize(int fetchSize)
	{
		int ret = tmpJdbcTemplate.getFetchSize();
		tmpJdbcTemplate.setFetchSize(fetchSize);
		return ret;
	}

	public SqlRowSet query(String sql, Object[] params)
	{
		return tmpJdbcTemplate.queryForRowSet(sql, params);
	}

	public SqlRowSet query(String sql)
	{
		return query(sql, new Object[] {});
	}

}
