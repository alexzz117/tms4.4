package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;

/**
 * 存储过程处理
 * 
 * @author liining
 * 
 */
@Service("storedProceProcess")
public class StoredProceProcessImpl extends QueryDataProcessCommon {
	private static Log log = LogFactory.getLog(StoredProceProcessImpl.class);
	private static final String CALL_SQL = "{call %s}";
	
	@Autowired
	private JdbcTemplate tmsJdbcTemplate;

	public Object dataProcess(Object... args) {
		try {
			JsonDataProcess process = (JsonDataProcess) args[0];
			HttpServletRequest request = (HttpServletRequest) args[1];
			String callSql = process.getCustom().getStmt().getContent();
			String callName = callSql.substring(0, callSql.indexOf("("));
			String callParam = callSql.substring(callName.length() + 1, callSql.indexOf(")"));
			String[] paramArrays = callParam.split("\\,");
			callParam = null;
			final List<Object> paramList = new ArrayList<Object>(paramArrays.length);
			for (String param : paramArrays) {
				param = param.replaceAll("\\:", "").trim();
				paramList.add(request.getParameter(param));
				if (callParam == null) {
					callParam = "?";
				} else {
					callParam += ",?";
				}
			}
			callSql = callName + "(" + callParam + ")";
			List<Map<String, Object>> result = tmsJdbcTemplate.execute(String.format(CALL_SQL, callSql), new CallableStatementCallback<List<Map<String, Object>>>() {
				public List<Map<String, Object>> doInCallableStatement(CallableStatement cs)
						throws SQLException, DataAccessException {
					for (int i = 0, len = paramList.size(); i < len; i++) {
						cs.setObject((i + 1), paramList.get(i));
					}
					cs.execute();
					ResultSet rs = cs.getResultSet();
					List<Map<String, Object>> resultList = resultSet2List(rs);
					rs.close();
					return resultList;
				}
			});
			super.dataProcess(process, result);
			Page<Map<String, Object>> page = new Page<Map<String,Object>>();
			page.setIndex(1);
			page.setSize(result.size());
			page.setTotal(result.size());
			page.setList(result);
			return page;
		} catch (Exception e) {
			log.error("Custom Query Execute StoredProcedure error." + e);
			throw new TmsMgrWebException(e.getMessage());
		}
	}

	/**
	 * ResultSet数据转List
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> resultSet2List(ResultSet rs) throws SQLException {
		if (rs == null)
			return Collections.emptyList();
		ResultSetMetaData md = rs.getMetaData();//得到结果集(rs)的结构信息，比如字段数、字段名等
		int columnCount = md.getColumnCount();//返回此 ResultSet 对象中的列数
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> rowData = null;
		while (rs.next()) {
			rowData = new HashMap<String, Object>(columnCount);
			for (int i = 1; i <= columnCount; i++) {
				rowData.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(rowData);
			//System.out.println("list:" + list.toString());
		}
		return list;
	}

}
