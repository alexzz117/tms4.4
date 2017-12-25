package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;

import cn.com.higinet.tms.engine.comm.hash;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.db_rule_hit;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

public class dao_rule_hit extends batch_stmt_jdbc_obj<db_rule_hit>
{
	static private dao_seq seq = dao_seq.get("SEQ_TMS_RUN_RULETRIG_ID");
	static private final int SQL_EXECOP_MODEL = tmsapp.get_config("tms.sql.model", 4);

	public static long next_trigid()
	{
		return seq.next();
	}

	public dao_rule_hit(data_source ds)
	{
		super(ds, make_insert_sql(), new int[] { Types.BIGINT, Types.VARCHAR, Types.INTEGER, 
			Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.BIGINT, Types.DOUBLE

		// db_fd.get("TMS_RUN_RULETRIG", "TRIGID").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "ALERTID").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "TXNTYPE").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "RULEID").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "TXNCODE").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "MESSAGE").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "NUMTIMES").sql_type(),
				// db_fd.get("TMS_RUN_RULETRIG", "CREATETIME").sql_type()
				});
	}

	private static String make_insert_sql()
	{
		int hashId = hash.hash_id(Thread.currentThread().getName(), SQL_EXECOP_MODEL);
		return "/*\nhashId=" + hashId + "\n*/\ninsert into TMS_RUN_RULETRIG (TRIGID,TXNTYPE,RULEID,TXNCODE,MESSAGE,NUMTIMES,CREATETIME,RULE_SCORE) values(?,?,?,?,?,?,?,?)";
	}

	@Override
	final public Object[] toArray(db_rule_hit e)
	{
		return new Object[] { e.trigid, e.txntype, e.ruleid, e.txncode, e.message,
				e.numtimes, e.createtime, (long) (e.rule_score * 100) / 100. };
	}

	public String name()
	{
		return "dao_rule_hit";
	}
}
