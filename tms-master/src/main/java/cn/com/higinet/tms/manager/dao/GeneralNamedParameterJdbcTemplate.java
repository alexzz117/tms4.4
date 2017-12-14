package cn.com.higinet.tms.manager.dao;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class GeneralNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {
	
	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	public GeneralNamedParameterJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}
	
	public GeneralNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
		super(classicJdbcTemplate);
	}
	
	@Override
	protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
		 ParsedSql parsedSql = getParsedSql(sql);
		 String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		 Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		 int[] paramTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		 PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, paramTypes);
		 if(this.resultSetType != ResultSet.TYPE_FORWARD_ONLY){
			 pscf.setResultSetType(this.resultSetType);
		 }
		 return pscf.newPreparedStatementCreator(params);
	}

	public int getResultSetType() {
		return resultSetType;
	}

	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}
}