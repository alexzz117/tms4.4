package cn.com.higinet.tms35.core.dao;


import java.sql.Types;

import cn.com.higinet.tms35.core.cache.db_alert;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

public class dao_alert extends batch_stmt_jdbc_obj<db_alert>
{
	static private dao_seq seq = dao_seq.get("SEQ_TMS_RUN_ALERT_ID");

	public static long next_alert_id()
	{
		return dao_seq.resetId(seq.next());
	}

	public dao_alert(data_source ds)
	{
		super(ds, make_insert_sql(), new int[] { Types.BIGINT, Types.VARCHAR, Types.INTEGER,
				Types.INTEGER, Types.DOUBLE, Types.VARCHAR, Types.INTEGER, Types.BIGINT,
				Types.INTEGER });
	}

	private static String make_insert_sql()
	{
		return "insert into TMS_RUN_ALERT(ALERTID, TXNCODE,TRIGRULENUM,HITRULENUM,SCORE,DISPOSAL,ISCORRECT,CREATETIME,IS_MAIN) values(?,?,?,?,?,?,?,?,?)";
	}

	final public Object[] toArray(db_alert e)
	{
		return new Object[] { e.alertid, e.txncode, e.trigrulenum, e.hitrulenum, (long)(e.score*100)/100.,
				e.disposal, e.iscorrect, e.createtime, e.is_main };
	}

	@Override
	public String name()
	{
		return "dao_alert";
	}
}
