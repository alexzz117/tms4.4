package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.com.higinet.tms.manager.dao.SqlMap;
import cn.com.higinet.tms35.comm.MD5Util;
import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.hash;
import cn.com.higinet.tms35.comm.roster_refresh_worker;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.core.concurrent.rw_lock;
import cn.com.higinet.tms35.core.cond.date_tool;
import cn.com.higinet.tms35.core.cond.ip_tool;
import cn.com.higinet.tms35.core.dao.dao_seq;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.core.dao.stmt.sql_reconnect_exception;

public final class db_roster
{
	static public class value
	{
		public long id;//名单值ID
		public String val;//名单值, MD5转码后的值
		public long enable_time, disable_time;

		public boolean is_enable(long now)
		{
			return enable_time <= now && now < disable_time //
					|| enable_time == 0 && now < disable_time //
					|| enable_time <= now && 0 == disable_time //
					|| enable_time == 0 && 0 == disable_time;
		}
		
		final public boolean equals(String v)
		{
			if (v == null)
				return false;
			return this.val.equals(v);
		}
	}

	private static java.util.Comparator<value> value_id_comp = new Comparator<value>()
	{
		public int compare(value o1, value o2)
		{
			int c = o1.val.compareTo(o2.val);
			if (c != 0)
				return c;
			return comp_tool.comp(o1.id, o2.id);
		}
	};

	static public class cache
	{
		private Map<Integer, db_roster> id_map = new HashMap<Integer, db_roster>();
		private Map<String, db_roster> name_map = new HashMap<String, db_roster>();
		private Map<Integer, linear<value>> val_map = new HashMap<Integer, linear<value>>();
		
		public static cache load(data_source ds)
		{
			cache c = load_roster(ds);
			c.init_val(ds);
			return c;
		}
		
		public static cache load_roster(data_source ds)
		{
			cache c = new cache();
			c.init_(ds);
			return c;
		}

		public void init_(data_source ds)
		{
			String sql = "select ROSTERID,ROSTERNAME,DATATYPE,ROSTERTYPE,ROSTERDESC,ISCACHE,ENABLETIME,DISABLETIME,STATUS "
					+ " from TMS_MGR_ROSTER " + " order by ROSTERID";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_roster r = new db_roster();
						r.id = rs.getInt("rosterid");
						r.name = str_tool.upper_case(rs.getString("rostername"));
						r.type = rs.getString("datatype");
						r.is_cache = rs.getInt("iscache") != 0;
						r.enable_time = rs.getLong("enabletime");
						r.disable_time = rs.getLong("enabletime");
						r.status = rs.getInt("status");
						
						id_map.put(r.id, r);
						name_map.put(r.name, r);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_roster.cache error.");
			}
			finally
			{
				stmt.close();
			}
			
			for (Integer roster_id : id_map.keySet())
			{
				this.val_map.put(roster_id, new linear<value>(value_id_comp));
			}

			//init_val(ds);
		}

		private void init_val(data_source ds)
		{
			String sql = "select ROSTERID,ROSTERVALUEID,ROSTERVALUE,ENABLETIME,DISABLETIME "
					+ " from TMS_MGR_ROSTERVALUE "
					+ "where ROSTERID in (select ROSTERID from TMS_MGR_ROSTER where ISCACHE!='0') "
					+ "order by ROSTERID,ROSTERVALUE";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {});
			try
			{
				stmt.query_fetch_all(new Object[] {}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_roster dr = null;
						linear<value> lv = null;
						int id;
						while (rs.next())
						{
							id = rs.getInt("ROSTERID");
							if (dr == null || dr.id != id)
							{
								dr = get(id);
								lv = val_map.get(id);
							}
							value v = new value();
							v.id = rs.getLong("ROSTERVALUEID");
							v.val = rs.getString("ROSTERVALUE");
							v.enable_time = rs.getLong("ENABLETIME");
							v.disable_time = rs.getLong("DISABLETIME");
							lv.add(v);
						}
						return true;
					}

				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_roster.cache error.");
			}
			finally
			{
				stmt.close();
			}

			for (Integer roster_id : id_map.keySet())
			{
				linear<value> lv = this.val_map.get(roster_id);
				lv.sort();
			}
		}
		
		public void merge_val(db_roster.cache c)
		{
			if (c == null || c.val_map == null || c.val_map.isEmpty())
				return;
			for (Integer roster_id : this.val_map.keySet()) {
				linear<value> curr_lv = this.val_map.get(roster_id);
				linear<value> cache_lv = c.val_map.get(roster_id);
				if (cache_lv != null)
					curr_lv.addAll(cache_lv);
				
			}
		}
		
		public void clear()
		{
			this.id_map.clear();
			this.name_map.clear();
			this.val_map.clear();
		}
		
		private linear<value> init_roster_val(final int roster_id)
		{
			final linear<value> lv = new linear<db_roster.value>(value_id_comp);
			String sql = "select ROSTERVALUEID,ROSTERVALUE,ENABLETIME,DISABLETIME "
					+ " from TMS_MGR_ROSTERVALUE where ROSTERID = ?";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(new data_source(), sql, new int[] { Types.INTEGER });
			try
			{
				stmt.query_fetch_all(new Object[] { roster_id }, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						while (rs.next())
						{
							value v = new value();
							v.id = rs.getLong("ROSTERVALUEID");
							v.val = rs.getString("ROSTERVALUE");
							v.enable_time = rs.getLong("ENABLETIME");
							v.disable_time = rs.getLong("DISABLETIME");
							lv.add(v);
						}
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				throw new tms_exception("load db_roster.id = " + roster_id + " value data error.");
			}
			finally
			{
				stmt.close();
			}
			lv.sort();
			return lv;
		}
		
		public int get_id(String name)
		{
			db_roster dr = name_map.get(name);
			if (dr != null)
				return dr.id;
			return -1;
		}
		
		public db_roster get(int id)
		{
			db_roster dr = id_map.get(id);
			return dr;
		}
		
		public db_roster get(String name)
		{
			db_roster dr = name_map.get(name);
			return dr;
		}
		
		private value get(long id, String val, linear<value> list)
		{
			if (list == null)
				return null;
			value v = new value();
			v.id = id;
			v.val = val;
			return list.get(v);
		}

		// IP地址必须缓存处理!
		public boolean val_in(Number p, Object val, long now_Ms)
		{
			return val_in(p.intValue(), val, now_Ms);
		}

		public boolean val_in(int id, Object val, long now_Ms)
		{
			if (val == null)
				return false;

			db_roster dr = id_map.get(id);
			if (dr == null)
				return false;

			if (!dr.time_enable(now_Ms))
				return false;

			if (!dr.is_cache)
				return val_in_db(dr, val, now_Ms);

			return val_in_cache(dr, val, now_Ms);
		}
		
		private boolean val_in_cache(db_roster dr, Object val, long now_ms)
		{
			linear<value> lin = val_map.get(dr.id);
			if (lin == null)
				return false;
			rw_lock lock = rw_lock.get_sync_rw_lock(lin);
			lock.wait_read();
			try
			{
				long now = now_ms;
				value v = new value();
				v.id = Integer.MAX_VALUE;
				v.val = String.valueOf(val);
				// 查找两次，第一次查找未加密的数据，第二次查找加密数据
				if (find_in_cache(v, now, lin))
				{
					return true;
				}
				v.val = MD5Util.getMD5Hex16(v.val);
				return find_in_cache(v, now, lin);
			}
			finally
			{
				lock.done_read();
			}
		}
		
		private boolean find_in_cache(value v, long now, linear<value> lin)
		{
			int e = lin.upper_bound(v) - 1;
			for (; e >= 0; --e)
			{
				value v1 = (value) lin.get(e);
				if (v1.val.compareTo(v.val) < 0)
					return false;
				if (v1.is_enable(now) && v1.equals(v.val))
					return true;
			}
			return false;
		}

		/**
		 * 查询名单值是否在数据库中
		 * @param dr		名单数据
		 * @param val		名单值
		 * @param now_ms	交易时间
		 * @return
		 */
		private boolean val_in_db(db_roster dr, Object val, long now_ms)
		{
			long now = now_ms;
			int hashId = hash.hash_id(Thread.currentThread().getName(), 4);
			StringBuffer sql = new StringBuffer();
			sql.append("/*\nhashId=").append(hashId).append("\n*/\n");
			int[] paramType = null;
			if (dr.type.equals("long"))
			{
				paramType = new int[] { Types.INTEGER, Types.BIGINT, Types.VARCHAR, 
						Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT };
				sql.append(((SqlMap) bean.get("tmsSqlMap")).getSql("tms.roster.interval.long"));
			}
			else
			{
				paramType = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR, 
						Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT };
				sql.append("select 1 from TMS_MGR_ROSTERVALUE t where ");
				sql.append("t.ROSTERID=? and (t.ROSTERVALUE=? or t.ROSTERVALUE=?)");
			}
			sql.append(" and (t.ENABLETIME<=? and t.DISABLETIME>?");
			sql.append(" or  t.ENABLETIME=0 and t.DISABLETIME>?");
			sql.append(" or  t.ENABLETIME<=? and t.DISABLETIME=0");
			sql.append(" or  t.ENABLETIME=0 and t.DISABLETIME=0)");
			
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(new data_source(), sql.toString(), paramType);
			final AtomicBoolean b = new AtomicBoolean(false);
			try
			{
				stmt.query_fetch_all(new Object[] { dr.id, val, MD5Util
						.getMD5Hex16(String.valueOf(val)),now, now, now, now }, new row_fetch()
				{
					@Override
					public boolean fetch(ResultSet rs) throws SQLException
					{
						b.set(rs.next());
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				stmt.close();
			}

			return b.get();
		}
		
		/**
		 * 更新名单数据
		 * @param list	名单数据
		 */
		public void update_roster(List<Map<String, String>> list)
		{
			if (list == null || list.isEmpty())
				return;
			rw_lock lock = rw_lock.get_sync_rw_lock(this);
			lock.wait_write();
			try
			{
				for (Map<String, String> map : list)
				{
					int roster_id = Integer.parseInt(map.get("rosterId"));
					String flag = map.get("method");
					db_roster dr = id_map.get(roster_id);
					if ("d".equals(flag))
					{
						// 删除
						if (dr != null)
						{
							id_map.remove(dr.id);
							name_map.remove(dr.name);
							val_map.remove(dr.id);
						}
					}
					else
					{
						boolean is_cache = false;// 以前是否缓存名单值数据
						if (dr == null)
						{
							// 新增名单
							dr = new db_roster();
							dr.id = roster_id;
							dr.name = map.get("rostername");
							dr.type = map.get("datatype");
							dr.is_cache = Integer.parseInt(map.get("iscache")) != 0;
							
							id_map.put(dr.id, dr);
							name_map.put(dr.name, dr);
							val_map.put(dr.id, new linear<value>(value_id_comp));
						}
						else
						{
							// 已有名单
							is_cache = dr.is_cache;
							
							dr.name = map.get("rostername");
							dr.type = map.get("datatype");
							dr.is_cache = Integer.parseInt(map.get("iscache")) != 0;
						}
						
						// 不缓存名单值
						if (!dr.is_cache)
						{
							// 清空名单值缓存数据
							val_map.get(dr.id).clear();
						}
						// 以前没缓存，现在需要缓存
						if (!is_cache && dr.is_cache)
						{
							// 查询名单下名单值数据并缓存
							linear<value> lin = init_roster_val(dr.id);
							val_map.put(dr.id, lin);
						}
					}
				}
			}
			finally
			{
				lock.done_write();
			}
		}
		
		@SuppressWarnings("unchecked")
		public void update_rostervalue(List<Map<String, Object>> list)
		{
			if (list == null || list.isEmpty())
				return;
			for (Map<String, Object> map : list)
			{
				int roster_id = Integer.parseInt(String.valueOf(map.get("rosterId")));
				List<Map<String, String>> values = (List<Map<String, String>>) map.get("values");
				update_rostervalue(roster_id, values);
			}
		}
		
		/**
		 * 更新名单值数据
		 * @param roster_id		名单ID
		 * @param list			名单值数据
		 */
		private void update_rostervalue(int roster_id, List<Map<String, String>> list)
		{
			if (list == null || list.isEmpty())
				return;
			db_roster dr = id_map.get(roster_id);
			if (dr == null || !dr.is_cache)
				return;
			linear<value> lin = val_map.get(roster_id);
			if (lin == null)
			{
				return;
			}
			rw_lock lock = rw_lock.get_sync_rw_lock(lin);
			lock.wait_write();
			try
			{
				for (Map<String, String> map : list)
				{
					try
					{
						long id = Long.parseLong(map.get("rosterValueId"));
						String flag = map.get("flag");
						if ("d".equals(flag))
						{
							// 删除名单值
							for (int i = 0, len = lin.size(); i < len; i++)
							{
								value v = lin.get(i);
								if (v.id == id)
								{
									lin.m_list.remove(i);
									break;
								}
							}
						}
						else
						{
							String val = map.get("rosterValue");
							value v = new value();
							v.id = id;
							v.val = val;
							v.enable_time = Long.parseLong(map.get("enabletime"));
							v.disable_time = Long.parseLong(map.get("disabletime"));
							
							value v1 = get(v.id, v.val, lin);
							if (v1 != null)
							{
								// 已在缓存中，更新
								v1.enable_time = v.enable_time;
								v1.disable_time = v.disable_time;
							}
							else
							{
								// 未在缓存中，插入
								lin.insert(v);
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			finally
			{
				lock.done_write();
			}
		}

		/**
		 * 添加名单值
		 * @param name		名单名称
		 * @param val		名单值
		 * @param now_ms	交易时间
		 */
		public void add_value(String name, Object val, long now_ms)
		{
			db_roster dr = name_map.get(name);
			if (dr != null)
			{
				add_value(dr.id, val, now_ms);
			}
		}

		/**
		 * 添加名单值
		 * @param id		名单ID
		 * @param val		名单值
		 * @param now_ms	交易时间
		 */
		public void add_value(int id, Object val, long now_ms)
		{
			if (val == null)
				return;

			db_roster dr = id_map.get(id);
			if (dr == null)
				return;

			String s = str_tool.to_str(val);

			if (dr.type.equals("ip"))
			{
				if (!is_ipaddr(s))
					throw new tms_exception("IP地址格式不合法：" + s);
			}
			else if (dr.type.equals("long"))
			{
				Long.parseLong(s);
			}

			if (this.val_in(id, val, now_ms))
				return;

			long val_id = insert_val_db(dr, val, now_ms);
			if (!dr.is_cache)
				return;
			insert_val_cache(dr, val_id, val, now_ms);
			// 通知其他风险评估服务刷新缓存
			Map<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("rosterId", id);
			map.put("rosterValueId", val_id);
			map.put("rosterValue", val);
			map.put("enabletime", now_ms);
			map.put("disabletime", 0);
			roster_refresh_worker.worker().request(map);
		}

		/**
		 * 向缓存中添加名单值
		 * @param dr		名单对象
		 * @param id		名单值ID
		 * @param val		名单值
		 * @param now_ms	交易时间
		 */
		private void insert_val_cache(db_roster dr, long id, Object val, long now_ms)
		{
			linear<value> lin = val_map.get(dr.id);
			rw_lock lock = rw_lock.get_sync_rw_lock(lin);
			lock.wait_write();
			try
			{
				value v = new value();
				v.val = String.valueOf(val);
				v.id = id;
				v.enable_time = now_ms;
				v.disable_time = 0;
				lin.insert(v);
			}
			finally
			{
				lock.done_write();
			}
		}

		private static batch_stmt_jdbc stmt_insert_value = null;
		private static batch_stmt_jdbc stmt_insert_value_tmp = null;

		public static void init_for_service_eval()
		{
			String sql = "insert into TMS_MGR_ROSTERVALUE(ROSTERID,ROSTERVALUEID,ROSTERVALUE,ENABLETIME,DISABLETIME,CREATETIME,MODIFYTIME) "
					+ "values(?,?,?,?,?,?,?)";
			int[] params = new int[] { Types.BIGINT, Types.BIGINT, Types.VARCHAR,
					Types.BIGINT, Types.BIGINT, Types.BIGINT, Types.BIGINT };
			stmt_insert_value = new batch_stmt_jdbc(new data_source(), sql, params);
			
			stmt_insert_value_tmp = new batch_stmt_jdbc(new data_source(bean.get_DataSource_tmp()), sql, params);

		}

		synchronized private long insert_val_db(db_roster dr, Object var, long now_ms)
		{
			long roster_value_id = -1;
			try
			{
				String v = str_tool.to_str(var);

				long time = now_ms;
				roster_value_id = Long.valueOf("1" + dao_seq.get("SEQ_TMS_ROSTERVALUE_ID").next());
				stmt_insert_value.update(new Object[] { dr.id, roster_value_id, v, time, 0,
						time, time });// 1+ID，区分在线添加还是离线添加同步到在线
				stmt_insert_value.flush();
				stmt_insert_value.commit();

				stmt_insert_value_tmp.update(new Object[] { dr.id,roster_value_id, v, time, 0,
						time, time });
				stmt_insert_value_tmp.flush();
				stmt_insert_value_tmp.commit();
			}
			catch (sql_reconnect_exception e)
			{
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			return roster_value_id;
		}
	}

	public int id;
	public String name;
	public String type;
	public int status;
	public long enable_time;
	public long disable_time;
	public boolean is_cache;

	db_roster()
	{
	}

	public String toString()
	{
		return "[" + name + "]";
	}

	public boolean time_enable(long now)
	{
		return enable_time <= now && now < disable_time //
				|| enable_time == 0 && now < disable_time //
				|| enable_time <= now && 0 == disable_time //
				|| enable_time == 0 && 0 == disable_time;
	}

	static boolean is_ipaddr(String ip)
	{
		return ip_tool.is_valid(ip);
	}

	public static void main(String[] argv)
	{

		System.out.println(date_tool.format(new Date(1379260800000L)));

		// cache c = cache.load(new data_source());
		// System.out.println(c.val_in(0, "10.8.1.11"));
	}
}
