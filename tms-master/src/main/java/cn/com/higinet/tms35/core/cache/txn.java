package cn.com.higinet.tms35.core.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.core.cond.node;
import cn.com.higinet.tms35.core.cond.parser.cond_parser;
import cn.com.higinet.tms35.core.dao.stmt.data_source;

/*
 * 描述一个交易的结构性数据
 * */
public class txn
{
	static Logger log = LoggerFactory.getLogger(txn.class);
	volatile static List<txn> list_ = new ArrayList<txn>();
	static final String stat_expr_format = "%s:%s";

	static public txn get(int txnId)
	{
		return list_.get(txnId);
	}

	static public void init(data_source ds, boolean load_rosterval)
	{
		log.info("进行化缓存初始化");
		db_cache dbc = db_cache.read(ds, false);
		db_roster.cache new_drc = null, old_drc = null;
		synchronized (txn.class) {
			if (load_rosterval)
			{
				new_drc = db_roster.cache.load(ds);
			}
			else
			{
				new_drc = db_roster.cache.load_roster(ds);
				old_drc = db_cache.g_drc;
				if (old_drc != null)
				{
					new_drc.merge_val(old_drc);
				}
			}
			db_cache.set_roster(new_drc);
			if (old_drc != null)
			{
				old_drc.clear();
			}
		}

		List<txn> list = new ArrayList<txn>();
		db_tab.cache dtc = dbc.table();
		for (int i = 0, len = dtc.tab_count(); i < len; i++)
		{
			db_tab tab = dtc.get(i);
			if (!tab.is_txnview())
				continue;
			init(dbc, list, tab);
		}

		list_ = list;
		db_cache.set(dbc);
		log.info("进行化缓存初始化成功");
	}

	// static ThreadLocal<Map<String, Object>> g_build_env = new
	// ThreadLocal<Map<String, Object>>();
	// static public void set_env(String name,Object o)
	// {
	// Map<String, Object> m=g_build_env.get();
	// if(m==null)
	// g_build_env.set(new LinkedHashMap<String, Object>(32));
	//		
	// m.put(name, o);
	// }

	private static void init(db_cache dbc, List<txn> list, db_tab tab)
	{
		try
		{
			// set_env("txn",tab);
			txn txn = new txn(list, dbc, tab);

			for (; tab.index >= list.size();)
				list.add(null);
			list.set(tab.index, txn);
		}
		catch (RuntimeException e)
		{
			// log.error(g_build_env.get().toString(), e);
			log.error(tab.toString(), e);
		}
		finally
		{
			// g_build_env.get().clear();
		}
	}
	
	public txn(){}

	public txn(List<txn> list, db_cache dbCache, db_tab tab)
	{
		if (log.isDebugEnabled())
			log.debug("初始化交易:" + tab.toString());

		g_dc = dbCache;
		m_tab = tab;
		m_fd = g_dc.field().get_txn_fields(tab.index);
		m_fd_srcid = g_dc.field().get_src_localid(tab.index);
		m_fd_refid = g_dc.field().get_refname_localid(tab.index);
		m_rule = g_dc.rule().get_txn_rules(tab.index);
		for (db_rule dr : m_rule)
		{
			m_rule_action.set(dr.index, g_dc.rule_action().get_rule_action(dr.index));
		}
		m_strategy = g_dc.strategy().get(tab.index);
		m_stat = g_dc.stat().get_txn_stats(tab.index);
		m_ref_tabs = g_dc.reftab().get_txn_tabref(tab.index);
		m_ref_stat = new linear<txn_ref_stat>(txn_ref_stat.comp_by_tab_id_param);
		m_ref_stat_id = new linear<txn_ref_stat>(txn_ref_stat.comp_by_id_param);
		m_ref_tab_fields = new TreeMap<Integer, List<Integer>>();
		m_disposal_assign_cond = new HashMap<String, node>();
		linear<db_disposal> dp_list = dbCache.disposal().get_by_dporder();
		for (db_disposal dp : dp_list) {
			m_disposal_assign_cond.put(dp.dp_code, str_tool.is_empty(dp.assign_cond)
					? null : cond_parser.build(dp.assign_cond));
		}
		init_ref_fields();
		init_ref_stat(list);
	}

	public db_cache dc()
	{
		return g_dc;
	}

	public db_cache g_dc;
	public db_tab m_tab; // 对应的交易视图
	public linear<db_fd> m_fd; // 所有的该交易可以使用的字段，包含继承的字段
	public linear<str_id> m_fd_srcid; // 该交易所有的交易字段，渠道传过来,使用src_name进行排序
	public linear<str_id> m_fd_refid; // 该交易引用的交易字段，渠道传过来,使用ref_name进行排序
	public linear<db_rule> m_rule; // 所有的规则,包含上层
	public linear<linear<db_rule_action>> m_rule_action = new linear<linear<db_rule_action>>();
	public linear<db_strategy> m_strategy; //所以的策略，包含上层
	public linear<db_stat> m_stat; // 所有的统计
	public linear<txn_ref_stat> m_ref_stat; // 所有引用的统计，包含上层，根据tab、index、param排序
	private linear<txn_ref_stat> m_ref_stat_id;// 所有引用的统计，包含上层，根据index、param排序
	public linear<db_tab_ref> m_ref_tabs; // 所有引用的签约、实体表
	public TreeMap<Integer, List<Integer>> m_ref_tab_fields; // 引用表在当前交易引用的字段
	public Map<String, node> m_disposal_assign_cond;// 处置报警分派条件 

	private void init_ref_fields()
	{
		m_ref_tab_fields.clear();
		for (db_tab_ref ref : m_ref_tabs)
		{
			List<Integer> ref_field = find_ref_field(ref.ref_id);
			m_ref_tab_fields.put(ref.ref_id, ref_field);
		}
	}

	private List<Integer> find_ref_field(int refId)
	{
		List<Integer> ret = new ArrayList<Integer>(m_fd.size());
		for (int i = 0, len = m_fd.size(); i < len; i++)
		{
			db_fd fd = m_fd.get(i);
			if (fd.ref != null && fd.ref.ref_id == refId)
				ret.add(i);
		}

		if (ret.size() == 0 && this.m_tab.txn_id != null)
		{
			// db_tab_ref ref = g_dc.reftab().get_by_refid(refId);
			// log.warn("警告，引用表[" + ref.ref_desc + "]没有引用任何字段!");
		}

		return new ArrayList<Integer>(ret);
	}

	// 初始化该交易引用的所有的统计（事中统计、规则、动作、开关、策略等条件所引用的）
	private final void node_p1(Object who, node n, db_tab tab)
	{
		if (n == null)
			return;
		try
		{
			n.prepare_rule(this, null);
			n.prepare1(this, null);
		}
		catch (RuntimeException e)
		{
			log.error(who.toString(), e);
			throw e;
		}
	}

	private final void node_p23(Object who, node n)
	{
		if (n == null)
			return;
		try
		{
			n.prepare2(null);
			n.prepare3(this);
		}
		catch (RuntimeException e)
		{
			log.error(who.toString(), e);
			throw e;
		}
	}

	private int cur_ref_point;
	private db_stat cur_stat;

	public void set_txn_ref(txn_ref_stat txnRefStat, db_stat st)
	{
		txnRefStat.set_ref_point(cur_ref_point);

		// 被统计引用，并且引用者具有online属性；需要在执行规则前加载
		if (cur_stat != null && cur_stat.stat_online != 0)
		{
			txnRefStat.set_ref_point(txn_ref_stat.REF_IN_RULE);
		}
		
		/*
		 * add lining 2015-07-05 begin
		 * 现在st_index、param排序的集合中查找txn_ref_stat对象
		 * 能查到：将查到对象和当前对象的引用点合并
		 * 查不到：将当前对象分别插入到两个集合中
		 * */
		int index = m_ref_stat_id.index(txnRefStat);
		if (index >= 0)
		{
			// 有此引用统计
			txn_ref_stat ref_stat = m_ref_stat_id.get(index);
			ref_stat.ref_point |= txnRefStat.ref_point;
		}
		else
		{
			// 无此引用统计
			m_ref_stat.insert(txnRefStat);
			m_ref_stat_id.insert(txnRefStat);
		}
		//m_ref_stat.add(txnRefStat);
		/* add lining 2015-07-05 end */
	}

	private void init_ref_stat(List<txn> list)
	{
		if (!str_tool.is_empty(m_tab.parent_tab))
		{
			db_tab ptab = g_dc.table().get(m_tab.parent_tab);
			m_ref_stat.addAll(list.get(ptab.index).m_ref_stat);
		}

		for (db_fd fd : m_fd)
			node_p1(fd, fd.node, m_tab);
		for (db_tab_ref tab : m_ref_tabs)
			node_p1(tab, tab.expr_node, m_tab);

		for (Iterator<List<Integer>> it_list = this.m_ref_tab_fields.values().iterator(); it_list
				.hasNext();)
			for (Integer i : it_list.next())
			{
				db_fd fd = m_fd.get(i);
				node_p1(fd, fd.ref.src, m_tab);
				node_p1(fd, fd.ref.cond, m_tab);
			}

		for (db_rule dr : m_rule)
		{
			if (!dr.is_enable)
				continue;
			cur_ref_point = txn_ref_stat.REF_IN_RULE;
			if (dr.id == 55) {
				System.out.println(dr);
			}
			node_p1(dr, dr.node, m_tab);
			cur_ref_point = txn_ref_stat.REF_IN_RULE_ACT;
			linear<db_rule_action> rule_action = m_rule_action.get(dr.index);
			for (db_rule_action ac : rule_action)
			{
				if (!ac.is_enable)
					continue;
				node_p1(ac, ac.node_cond, m_tab);
				node_p1(ac, ac.node_expr, m_tab);
			}
		}
		
		// add lining 添加默认规则，将当前交易下所有存储字段不为空的统计全部加入到引用统计中 begin
		cur_ref_point = txn_ref_stat.REF_IN_RULE;
		for (db_stat st : m_stat) {
			if (st.is_valid == 0 || !st.stat_txn.equals(m_tab.tab_name)
				|| str_tool.is_empty(st.storecolumn))
				continue;
			node n = cond_parser.build(String.format(stat_expr_format, m_tab.tab_name, st.stat_name));
			node_p1("默认规则[编译交易" + m_tab.toString() + "下所有存储字段不为空的统计]", n, m_tab);
		}
		// add lining end
		
		cur_ref_point = txn_ref_stat.REF_IN_ST;
		for (db_strategy st : m_strategy)
		{
			if (!st.st_enable)
				continue;
			node_p1(st, st.node, m_tab);
		}

		cur_ref_point = txn_ref_stat.REF_IN_STAT;
		for (db_stat st : m_stat)
		{
			if (st.is_valid == 0)
				continue;
			cur_stat = st;
			node_p1(st, st.node, m_tab);
		}
		
		Iterator<Entry<String, node>> it = m_disposal_assign_cond.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, node> en = it.next();
			node_p1(null, en.getValue(), m_tab);
		}
		
		cur_stat = null;

		/* add lining 2015-07-05 注释掉此段代码
		 * sort(m_ref_stat, txn_ref_stat.comp_by_id_param_refpoint);
		set_tool.unique(m_ref_stat, txn_ref_stat.comp_by_id_param_refpoint);

		// 此处将被引用统计所引用的点进行合并
		List<txn_ref_stat> ref_stat = new ArrayList<txn_ref_stat>();
		txn_ref_stat rs = null;
		for (int i = 0, len = m_ref_stat.size(); i < len;)
		{
			rs = m_ref_stat.get(i);
			ref_stat.add(rs);
			for (i++; i < len && 0 == txn_ref_stat.comp_by_id_param.compare(rs, m_ref_stat.get(i)); i++)
				rs.ref_point |= (m_ref_stat.get(i).ref_point);
		}
		m_ref_stat = ref_stat;*/

		for (db_fd fd : m_fd)
			node_p23(fd, fd.node);
		for (db_tab_ref tab : m_ref_tabs)
			node_p23(tab, tab.expr_node);
		for (Iterator<List<Integer>> it_list = this.m_ref_tab_fields.values().iterator(); it_list
				.hasNext();)
		{
			for (Integer i : it_list.next())
			{
				db_fd fd = m_fd.get(i);
				node_p23(fd, fd.ref.src);
				node_p23(fd, fd.ref.cond);
			}
		}

		for (db_stat st : m_stat)
		{
			node_p23(st, st.node);
		}

		for (db_rule dr : m_rule)
		{
			if (!dr.is_enable)
				continue;
			node_p23(dr, dr.node);
			linear<db_rule_action> rule_action = m_rule_action.get(dr.index);
			for (db_rule_action ac : rule_action)
			{
				if (!ac.is_enable)
					continue;
				node_p23(ac, ac.node_cond);
				node_p23(ac, ac.node_expr);
			}
		}
		for (db_strategy st : m_strategy)
			node_p23(st, st.node);
		
		Iterator<Entry<String, node>> _it = m_disposal_assign_cond.entrySet().iterator();
		while(_it.hasNext()) {
			Entry<String, node> en = _it.next();
			node_p23(null, en.getValue());
		}
	}

	final public int id()
	{
		return m_tab.index;
	}

	final public db_fd get_fd(int localid)
	{
		return m_fd.get(localid);
	}

	public linear<txn_ref_stat> get_ref_stat()
	{
		return m_ref_stat;
	}

	public db_tab get_tab()
	{
		return m_tab;
	}
	
	public int get_stat_local_id(txn_ref_stat ref_stat)
	{
		int i = m_ref_stat.index(ref_stat);
		return i;
	}

	public int get_stat_local_id(int n1, int[] param)
	{
		txn_ref_stat stat = m_ref_stat_id.get(new txn_ref_stat(null, n1, param));
		return m_ref_stat.index(stat);
	}

	public int get_field_localindex(String refName)
	{
		return g_dc.field().get_local_index(m_tab.index, refName);
	}

	public linear<db_rule_action> get_action(int rule_index)
	{
		return m_rule_action.get(rule_index);
	}
	
	public node get_assign_cond(String dp) {
		return m_disposal_assign_cond.get(dp);
	}
}
