package cn.com.higinet.tms.manager.modules.common;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms35.core.dao.dao_seq;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

@Component("sequenceService")
public class SequenceService {
	
	@Autowired
	private DataSource dynamicDataSource;
	@Autowired
	private SqlMap tmsSqlMap;
	
	/**
	 * 获取唯一序列ID
	 * @param seqName	序列名
	 * @return {long}
	 */
	public long getSequenceId(String seqName) {
		return getSequenceId(seqName, null);
	}
	
	/**
	 * 获取唯一序列ID
	 * @param seqName	序列名
	 * @param ds		数据源
	 * @return {long}
	 */
	public long getSequenceId(String seqName, DataSource ds) {
		if(ds == null) {
			ds = dynamicDataSource;
		}
		String sqlSeqNext = tmsSqlMap.getSql(StaticParameters.
				TMS_COMMON_SEQUENCEID).replaceFirst("\\$\\{SEQUENCENAME\\}", seqName);
		return dao_seq.db_next(new data_source(ds), sqlSeqNext);
	}
	
	/**
	 * 获取唯一序列ID
	 * @param seqName	序列名
	 * @return {String}
	 */
	public String getSequenceIdToString(String seqName) {
		return getSequenceIdToString(seqName, null);
	}
	
	/**
	 * 获取唯一序列ID
	 * @param seqName	序列名
	 * @param ds		数据源
	 * @return {String}
	 */
	public String getSequenceIdToString(String seqName, DataSource ds) {
		return String.valueOf(getSequenceId(seqName, ds));
	}
}