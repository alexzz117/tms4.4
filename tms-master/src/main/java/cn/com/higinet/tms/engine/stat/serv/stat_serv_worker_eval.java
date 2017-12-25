/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  stat_serv_worker_eval.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-22 13:59:55   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.stat.serv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import cn.com.higinet.tms.engine.comm.clock;
import cn.com.higinet.tms.engine.comm.tmsapp;
import cn.com.higinet.tms.engine.core.cache.db_cache;
import cn.com.higinet.tms.engine.core.cache.db_stat;
import cn.com.higinet.tms.engine.core.concurrent.tms_worker_base;
import cn.com.higinet.tms.engine.core.dao.stat_value;
import cn.com.higinet.tms.engine.core.persist.Stat;
import cn.com.higinet.tms.engine.stat.stat_row;

/**
 * 进行统计累计的类，4.2之前是基于stat_value（聚合存储）的方式来进行累计，4.3过后修改成基于stat_row的方式统计。
 * 统计目前是通过持久化接口{@link cn.com.higinet.tms.engine.core.persist.Stat}进行持久化，
 * 目前实现的持久化接口是基于cache实现的一套接口{@link cn.com.higinet.tms.engine.core.persist.impl.CacheStatImpl}。
 *
 * @ClassName:  stat_serv_worker_eval
 * @author: 王兴
 * @date:   2017-8-24 11:29:54
 * @since:  v4.3
 */
public class stat_serv_worker_eval extends tms_worker_base<stat_row> {

	/** 统计持久化接口. */
	private Stat stat;

	/** 从队列里面一次性取的需要累计的统计条数. */
	private static final int g_eval_bacth_size = tmsapp.get_config("tms.stat.eval.batchsize", 1024);

	/** 从队列里面提取统计的时间间隔. */
	private static final int g_eval_bacth_time = tmsapp.get_config("tms.stat.eval.batchtime", 100);

	/** 统计持久化提交的字节数阈值，所有累计的统计超过此字节数就会把当前提交队列中的统计提交持久化. */
	private static final int g_commit_size = tmsapp.get_config("tms.stat.commit_byte_size", 8) * 1024;

	/** 统计持久化提交的时间阈值，超过此时间就会把当前提交队列中的统计提交持久化. */
	private static final int g_commit_maxperiod = tmsapp.get_config("tms.stat.commit_period", 100);

	/** 统计的接口缓存. */
	private db_stat.cache db_stat_cache;

	/** 提交计时器，跟时间阈值计算相关. */
	private clock m_commit_clock = new clock();

	/**存放当前线程需要马上处理的全部统计集合. */
	private List<stat_row> m_cur_list = new ArrayList<stat_row>(1024);

	/** 提交队列，所有需要提交的统计都在此队列中. */
	private Map<String, stat_row> m_want_update = new HashMap<String, stat_row>(2048, 0.5f);// 提交队列

	/**
	 * @see cn.com.higinet.tms.engine.core.concurrent.tms_worker_base#setup(java.lang.String[])
	 */
	@Override
	@Deprecated
	public void setup(String[] param) {
		//		if (param[0].equals("reset_db")) {
		//			try {
		//				m_batch_stat_query.close();
		//				m_batch_update.close();
		//			} catch (Exception e) {
		//			}
		//
		//			data_source ds = new data_source(param[1], param[2], param[3], param[4]);
		//			m_batch_stat_query = new dao_stat_batch_query_lob(ds, g_query_batch_size, param[5]);
		//			m_batch_update = new dao_stat_update_lob(ds, g_update_batch_size, param[5]);
		//		}
	}

	/**
	 * 构造一个新的 stat serv worker eval 对象.
	 *
	 * @param name the name
	 * @param bufSize the buf size
	 * @param stat the stat
	 */
	public stat_serv_worker_eval(String name, int bufSize, Stat stat) {
		super(name, bufSize);
		this.stat = stat;
		db_stat_cache = db_cache.get().stat();
	}

	/**
	 * 提交统计
	 */
	private void try_commit() {
		int commit_size = 0;
		Collection<stat_row> values = m_want_update.values();
		for (stat_row row : values) {
			String value = row.get_value();
			commit_size += value != null ? value.length() : 0;
		}

		if (commit_size == 0 || commit_size < g_commit_size && m_commit_clock.count_ms() < g_commit_maxperiod) {
			return;
		}
		int commitCount = m_want_update.size();
		clock c = new clock();
		if (!m_want_update.isEmpty()) {
			try {
				stat.saveList(new ArrayList<stat_row>(m_want_update.values()));
			} catch (Exception e) {
				log.error(null, e);
			} finally {
				m_want_update.clear();
			}
		}
		if (c.count_ms() > 1000) {
			log.warn(String.format("统计提交时间大于1000ms: %d ms" + ",一共提交了%d条记录，一共提交%d字节。", c.count_ms(), commitCount, commit_size));
		}

		m_commit_clock.reset();
	}

	/**
	 * @see cn.com.higinet.tms.engine.core.concurrent.tms_worker_base#run_once()
	 */
	protected void run_once() {
		m_cur_list.clear();
		drainTo(m_cur_list, g_eval_bacth_size, g_eval_bacth_time);

		this.try_commit();

		if (m_cur_list.isEmpty())
			return;

		db_stat_cache = db_cache.get().stat();
		List<stat_row> queryList = new ArrayList<stat_row>(1024);
		// 将当前的统计请求加入缓冲区，如果存在，则直接使用
		String id;
		for (stat_row row : m_cur_list) {
			id = row.getUniqID();
			stat_row existsRow = m_want_update.get(id);
			//4.3版本时候修改，检查是否在局部缓存，如果存在，则每次都设置同一个对象为已存在stat_row
			//以后每个stat_row更新统计数据的时候只更新已存在的stat_row的数据  2017-08-21 王兴
			if (existsRow != null) {
				row.setExistsRow(existsRow);
				continue;
			}
			m_want_update.put(id, row);
			queryList.add(row);
		}
		// 读取需要查询的统计数据,读取完成后,local_set中存储了所有本次统计所需要的stat_value
		if (!queryList.isEmpty()) {
			try {
				queryList = stat.queryList(queryList);
			} catch (Exception e) {
				log.error(null, e);
				return;
			}
		}
		for (stat_row sd : m_cur_list) {
			String statUniqId = sd.getUniqID();
			if (sd.is_query()) {
			} else if (sd.is_clear()) {//直接从持久化中删除
				try {
					stat.delete(sd);
				} catch (Exception e) {
					//log.error(StringUtils.format("删除统计值失败，统计\"%s\"，统计值\"%s\"", statUniqId, sd.get_value()), e);
				} finally {
					m_want_update.remove(statUniqId);//从提交队列中移除
				}
				continue;
			} else if (sd.is_union()) {//迭代系统专用的统计合并, 遍历将要进行合并的统计id, 将合并前的统计id的值复制下来
				//备份的是迭代和在线两边都有的那个统计值，因为这些统计值会在合并的时候被修改，迭代和在线的统计的交集
				stat_value sv = sd.get_other_stat_value();
				List<stat_row> backupRows = null;
				try {
					backupRows = prepareForUnion(sv); //包含迭代所有涉及的stat_row，如果stat_row的值为null，说明当前持久化中不包含这个stat_row
					stat.backup(backupRows);//这个other_stat_value是在迭代合并或者回滚操作才会用到
				} catch (Exception e) {
					//log.error(StringUtils.format("备份统计值失败，stat_param:%s,stat_value[%s]", sv.m_param, sv.toString()), e);
				}
				if (backupRows != null) {
					Map<Integer, String> iterStatRows = sd.get_other_stat_value().getM_map(); //迭代系统传过来要合并的统计值
					//backupRows的size根iterStatRows一样，因为backupRows完全是根据iterStatRows的keySet来获取的
					for (int i = 0, len = backupRows.size(); i < len; i++) {
						stat_row row = backupRows.get(i);
						int statId = row.m_stat_id;
						db_stat stat = db_stat_cache.get_by_statid(statId);
						stat_row existsRow = m_want_update.get(row.getUniqID()); //这里防止覆盖本地提交队列中已经有的stat_row
						String value = iterStatRows.get(statId);
						String s = stat.func_st.union(value, existsRow != null ? existsRow.get_value() : row.get_value(), stat);
						if (row.set_value(s)) {
							m_want_update.put(row.getUniqID(), row); //进入持久化队列
						}
					}
				}
			} else if (sd.is_rollback()) {//迭代系统专用的统计回滚
				//回滚这块的逻辑跟备份有关，备份目前是单个统计备份，相当于之前聚合存储拆分开后备份的，如果要回滚，则一条一条回滚
				Map<Integer, String> rows = sd.get_other_stat_value().getM_map();
				if (rows != null && rows.size() > 0) {//因为备份的时候是按一条一条备份的，因此这里只去第一条，不管其他的
					Iterator<Entry<Integer, String>> rowsToRollback = rows.entrySet().iterator();
					String uniqId = sd.get_other_stat_value().m_param;
					String statParam = uniqId.substring(0, uniqId.lastIndexOf(stat_row.SEPARATOR));
					while (rowsToRollback.hasNext()) {
						Entry<Integer, String> entry = rowsToRollback.next();
						int statId = entry.getKey();
						String value = entry.getValue();
						stat_row row = new stat_row(statId, statParam, 0);
						row.set_value(value);
						m_want_update.put(row.getUniqID(), row);  //这里是回滚操作，备份是什么值，那就从什么值开始累计，不管之前是否有累计过其他值
					}
				}
			} else {
				//正常统计累计
				sd.do_stat_lob(db_stat_cache);
				sd.dec_batch();
				if (sd.isDirty()) {
					if (!sd.hasExistsRow()) {//如果不存在ExistsRow的stat_row直接进更新队列
						m_want_update.put(statUniqId, sd);
					} else {
						m_want_update.put(statUniqId, sd.getExistsRow());//这里预防ExistsRow不是dirty的情况，重复插入同一个对象没有影响
					}
				}
			}
		}

		try_commit();
	}

	/**
	 * 合并前的准备工作，这里主要是将迭代系统传过来的stat_value中，现目前在在线系统中存在的统计提取出来，最终备份到备份库中
	 *
	 * @param svForUnion the sv for union
	 * @return the list
	 * @throws Exception 
	 */
	private List<stat_row> prepareForUnion(stat_value svForUnion) throws Exception {
		List<Integer> statIds = new ArrayList<Integer>(svForUnion.getM_map().keySet());
		List<stat_row> queryList = new ArrayList<stat_row>(statIds.size());
		String statParam = svForUnion.m_param;
		for (int i = 0, len = statIds.size(); i < len; i++) {
			queryList.add(new stat_row(statIds.get(i), statParam, 0));
		}
		return stat.queryList(queryList);
	}

	/**
	 * 获取合并前的统计值
	 * 4.3版本已经废除此备份方法
	 * @param sv
	 *            统计服务缓存中的统计值
	 * @param uv
	 *            迭代服务发送的统计值
	 * @return
	 */
	@Deprecated
	private Map<Integer, String> union_before_value(stat_value sv, stat_value uv) {
		Iterator<Map.Entry<Integer, String>> i1 = uv.Iterator();// 迭代服务发送的统计值
		Iterator<Map.Entry<Integer, String>> i2 = sv.Iterator();// 统计服务缓存中的统计值

		Map.Entry<Integer, String> m1 = stat_row.itget(i1);
		Map.Entry<Integer, String> m2 = stat_row.itget(i2);

		Map<Integer, String> tmp = new TreeMap<Integer, String>();
		for (; m1 != null && m2 != null;) {
			int c = m1.getKey() - m2.getKey();
			if (c == 0) {
				tmp.put(m1.getKey(), m2.getValue());
				m1 = stat_row.itget(i1);
				m2 = stat_row.itget(i2);
			} else if (c < 0) {
				tmp.put(m1.getKey(), null);
				m1 = stat_row.itget(i1);
			} else {
				m2 = stat_row.itget(i2);
			}
		}

		for (; m1 != null;) {
			tmp.put(m1.getKey(), null);
			m1 = stat_row.itget(i1);
		}

		return tmp;
	}

	@Override
	public void shutdown(boolean abort) {
		super.shutdown(abort);
	}
}
