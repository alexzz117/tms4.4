package cn.com.higinet.tms.engine.core.dao;

import java.sql.Types;

import cn.com.higinet.tms.engine.core.cache.db_rule_action_hit;
import cn.com.higinet.tms.engine.core.dao.stmt.batch_stmt_jdbc_obj;
import cn.com.higinet.tms.engine.core.dao.stmt.data_source;

/**
 * 规则动作命中dao
 * @author lining
 *
 */
public class dao_rule_action_hit extends batch_stmt_jdbc_obj<db_rule_action_hit>
{
	static private dao_seq seq = dao_seq.get("SEQ_TMS_RUN_RULE_ACTION_HIT_ID");

	public static long next_hitid()
	{
		return dao_seq.resetId(seq.next());
	}

	public dao_rule_action_hit(data_source ds)
	{
		super(ds, make_insert_sql(), new int[] {
			Types.BIGINT, Types.VARCHAR, Types.INTEGER, Types.VARCHAR, 
			Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.BIGINT
		});
	}

	private static String make_insert_sql()
	{
		return "insert into TMS_RUN_RULE_ACTION_HIT (HITID, TXNTYPE, RULEID, TXNCODE, AC_ID, AC_COND, AC_EXPR, CREATETIME) values(?,?,?,?,?,?,?,?)";
	}

	@Override
	final public Object[] toArray(db_rule_action_hit e)
	{
		return new Object[] { e.hitid, e.txntype, e.ruleid, e.txncode, 
				e.acid, e.ac_cond, e.ac_expr, e.createtime };
	}

	public String name()
	{
		return "dao_rule_action_hit";
	}
}