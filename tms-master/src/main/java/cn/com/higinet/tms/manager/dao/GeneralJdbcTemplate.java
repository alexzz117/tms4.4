package cn.com.higinet.tms.manager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

public class GeneralJdbcTemplate extends JdbcTemplate {
	
	public GeneralJdbcTemplate() {
		super();
	}
	
	public GeneralJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}
	
	public GeneralJdbcTemplate(DataSource dataSource, boolean lazyInit) {
		super(dataSource, lazyInit);
	}
	
	@Override
	public <T> T query(
			PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse)
			throws DataAccessException {
		Assert.notNull(rse, "ResultSetExtractor must not be null");
//		logger.debug("Executing prepared SQL query");//此处是用的apache.common-logging

		return execute(psc, new PreparedStatementCallback<T>() {
			@Override
			public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ResultSet rs = null;
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					rs = ps.executeQuery();
					int absolute = getAbsolute();
					if (absolute > 0) {
						rs.absolute(absolute);
					}
					ResultSet rsToUse = rs;
					if (getNativeJdbcExtractor() != null) {
						rsToUse = getNativeJdbcExtractor().getNativeResultSet(rs);
						
					}
					return rse.extractData(rsToUse);
				}
				finally {
					JdbcUtils.closeResultSet(rs);
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
	
	private int absolute;

	public int getAbsolute() {
		return absolute;
	}

	public void setAbsolute(int absolute) {
		this.absolute = absolute;
	}
}