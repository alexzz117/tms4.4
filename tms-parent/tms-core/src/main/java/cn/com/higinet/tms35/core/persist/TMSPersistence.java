/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  TMSPersistence.java   
 * @Package cn.com.higinet.tms35.core.persist   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-25 11:03:23   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35.core.persist;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import cn.com.higinet.tms.common.repository.persistence.AbstractSimpleRDBMSPersistence;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/**
 * TMS内部的持久化接口
 *
 * @ClassName:  TMSPersistence
 * @author: 王兴
 * @date:   2017-9-25 11:03:23
 * @since:  v4.3
 */
public class TMSPersistence extends AbstractSimpleRDBMSPersistence {

	private data_source ds;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.persistence.AbstractRDBMSPersistence#executeSql(java.lang.String, java.util.List, int[])
	 */
	@Override
	protected int executeSql(String sql, List<Object> params, int[] paramTypes) throws Exception {
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, paramTypes);
		try{
			return stmt.execute(params.toArray()) ? 1 : 0;
		} finally {
			stmt.reset();
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		super.initialize();
		ds = new data_source((DataSource) bean.get("tmsDataSource"));
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		super.destroy();
	}

	@Override
	protected Map<String, String> executeQuery(String sql, List<Object> params, int[] paramTypes) throws Exception {
		batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, paramTypes);
		final Map<String, String> res = new HashMap<String, String>();
		try {
			stmt.query(params.toArray(), new row_fetch() {
				@Override
				public boolean fetch(ResultSet rs) throws SQLException {
					ResultSetMetaData rsm = rs.getMetaData();
					for (int i = 1, len = rsm.getColumnCount(); i < len + 1; i++) {
						res.put(rsm.getColumnName(i), rs.getString(i));
					}
					return true;
				}

			});
		} finally {
			stmt.reset();
		}
		return res;
	}

}
