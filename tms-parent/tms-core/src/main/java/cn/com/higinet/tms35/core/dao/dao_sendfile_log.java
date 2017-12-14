package cn.com.higinet.tms35.core.dao;

import java.sql.SQLException;
import java.sql.Types;

import cn.com.higinet.tms35.core.cache.db_sendfile_log;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_sendfile_log extends batch_stmt_jdbc_obj<db_sendfile_log> {

	public dao_sendfile_log(data_source ds) {
	    super(ds, make_insert_sql(), new int[] { Types.VARCHAR, Types.VARCHAR,
            Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
            Types.BIGINT, Types.BIGINT, Types.BIGINT });
	}
	
	private static String make_insert_sql()
    {
        return "insert into TMS_MGR_SENDFILE_LOG(LOGID, SRCADDR, TAGADDR, SRCFILENAME, TAGFILENAME, STATUS, STARTTIME, ENDTIME, FINISHTIME) values(?,?,?,?,?,?,?,?,?)";
    }
	
	@Override
	public void update(db_sendfile_log e) throws SQLException
	{
		update(null, e);
	}

	@Override
	public String name() {
		return "filesend_log";
	}

	@Override
	public Object[] toArray(db_sendfile_log e) {
        return new Object[] { e.logid, e.srcaddr, e.tagaddr, e.srcfilename,
                e.tagfilename, e.status, e.starttime, e.endtime, e.finishtime };
    }
}