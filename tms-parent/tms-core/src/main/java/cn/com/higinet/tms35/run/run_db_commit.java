package cn.com.higinet.tms35.run;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.core.cache.db_ref_row;
//import cn.com.higinet.tms35.core.cache.db_riskinfo_merchant;
import cn.com.higinet.tms35.core.cache.db_rule_action_hit;
import cn.com.higinet.tms35.core.cache.db_rule_hit;
//import cn.com.higinet.tms35.core.cache.db_session;

public class run_db_commit
{
	boolean m_is_move;
	run_env m_re;
	run_txn_values m_txn_data;
//	db_session m_session;
	List<db_rule_hit> m_rule_hit;
	List<db_rule_action_hit> m_rule_action_hit;
//	List<db_user> m_user;
	List<db_ref_row> m_acc;
//	List<db_merchant> m_merchant;
	
//	List<db_pos_merchant> m_pos_merchant;///pos商户信息
	
//	List<db_riskinfo_merchant> m_riskinfo_merchant;///商户风险信息

	run_db_commit(run_env re)
	{
		m_re = re;
		m_rule_hit = new ArrayList<db_rule_hit>(20);
		m_rule_action_hit = new ArrayList<db_rule_action_hit>(32);
		m_is_move = false;
	}

	public final run_txn_values get_txn_data()
	{
		return m_txn_data;
	}

	public final void set_txn_data(run_txn_values mTxnData)
	{
		m_txn_data = mTxnData;
	}

	/*public final db_session get_session()
	{
		return m_session;
	}

	public final void set_session(db_session mSession)
	{
		m_session = mSession;
	}*/

	public final List<db_rule_hit> get_rule_hit()
	{
		return m_rule_hit;
	}
	
	public final void set_rule_hit(List<db_rule_hit> list)
	{
		m_rule_hit = list;
	}
	
	public final List<db_rule_action_hit> get_rule_action_hit()
	{
		return m_rule_action_hit;
	}
	
	public final void set_rule_action_hit(List<db_rule_action_hit> list)
	{
		m_rule_action_hit = list;
	}

	public final List<db_ref_row> get_acc()
	{
		return m_acc;
	}

	public final void add_acc(db_ref_row acc)
	{
		if(m_acc == null)
			m_acc = new ArrayList<db_ref_row>();
		m_acc.add(acc);
	}

//	public final List<db_user> get_user()
//	{
//		return m_user;
//	}
//
//	public final void add_user(db_user user)
//	{
//		if(m_user == null)
//			m_user = new ArrayList<db_user>();
//		m_user.add(user);
//	}
//	//新增的商户信息
//	public final void add_merchant(db_merchant merchant)
//	{
//		if(m_merchant == null)
//			m_merchant = new ArrayList<db_merchant>();
//			m_merchant.add(merchant);
//	}
	
	//新增pos商户信息
//	public final void add_pos_merchant(db_pos_merchant pos_merchant)
//	{
//		if(m_pos_merchant == null)
//			m_pos_merchant = new ArrayList<db_pos_merchant>();
//			m_pos_merchant.add(pos_merchant);
//	}
	
	//商户风险信息
//	public final void add_riskinfo_merchant(db_riskinfo_merchant ris_merchant)
//	{
//		if(m_riskinfo_merchant == null)
//			m_riskinfo_merchant = new ArrayList<db_riskinfo_merchant>();
//			m_riskinfo_merchant.add(ris_merchant);
//	}
	
	public String get_user_id()
	{
		return m_re.get_user_id();
	}
	
	public String get_dispatch()
	{
		return m_re.get_dispatch();
	}
	
	public boolean is_move()
	{
		return m_is_move;
	}
	
	public void set_move(boolean is_move)
	{
		m_is_move = is_move;
	}
}
