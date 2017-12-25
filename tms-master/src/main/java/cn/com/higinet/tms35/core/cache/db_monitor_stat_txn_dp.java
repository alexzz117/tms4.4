package cn.com.higinet.tms35.core.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import cn.com.higinet.tms35.comm.comp_tool;
import cn.com.higinet.tms35.comm.str_tool;
import cn.com.higinet.tms35.comm.tms_exception;
import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc;
import cn.com.higinet.tms35.core.dao.stmt.batch_stmt_jdbc.row_fetch;
import cn.com.higinet.tms35.core.dao.stmt.data_source;
import cn.com.higinet.tms35.run.run_env;

/**
 * @author hyc
 * @version y1.5
 */
public class db_monitor_stat_txn_dp extends db_monitor_stat {
	
	db_monitor_stat_txn_dp() {}
	
	public db_monitor_stat_txn_dp(run_env re) {
		super(re);
		time = re.get_txn_time();
		countrycode = (String) re.get_fd_value(re.field_cache().INDEX_COUNTRY);
		regioncode = (String) re.get_fd_value(re.field_cache().INDEX_REGION);
		citycode = (String) re.get_fd_value(re.field_cache().INDEX_CITY);
		txnid = re.get_txntype();
		channelid = re.get_chan_chancode();
		txncode = re.get_txn_code();
		this.disposal = (String) re.get_fd_value(re.field_cache().INDEX_DISPOSAL);
		Object sc = re.get_fd_value(re.field_cache().INDEX_SCORE);
		if (sc == null || str_tool.is_empty(sc.toString())) {
			score = Integer.MAX_VALUE; 
		}else{
			score = Double.parseDouble(sc.toString());
		}
		if (str_tool.is_empty(countrycode)) {
			countrycode = NULL_CODE;
		}
		if (str_tool.is_empty(regioncode)) {
			regioncode = NULL_CODE;
		}
		if (str_tool.is_empty(citycode)) {
			citycode = NULL_CODE;
		}
	}
	
	/**
	 * @author hyc
	 *
	 */
	static public class cache
	{
		//服务开始时间		
		public static long start_time = System.currentTimeMillis();
		/**
		 * 数据保持时间一天，单位：毫秒数
		 */
		private static long keepTime = 24*60*60*1000;//1小时
		private static long checkTime = 1*60*60*1000;//1小时
		private static String sc_min = "PS02";
		private static int dp_data_Size = 0;
		private static boolean isFirst = true;
		/**
		 * 是否启动写入数据功能
		 */
		private static boolean writeOn = false;
		/**
		 * 最大缓存数据
		 */
		static int dp_max_data_Size = 1000;
		static {
			keepTime = tmsapp.get_config("tms.monitor.stat.dp.keepTime", 24*60*60*1000);
			dp_max_data_Size = tmsapp.get_config("tms.monitor.stat.dp.cachesize", 1000);
			sc_min = tmsapp.get_config("tms.monitor.stat.dp.min", "PS02").toUpperCase();
			writeOn = (tmsapp.get_config("tms.monitor.stat.dp.isOn", 0) == 1); 
		}
//		static Comparator<Long> comp_time = new Comparator<Long>(){  
//	        public int compare(Long o1, Long o2) {  
//	            return comp_tool.comp(o1, o2);
//	        }  
//	    };   
	    static Comparator<String> comp_dp = new Comparator<String>(){  
	    	public int compare(String b1, String b2) {  
	    		return b1.compareTo(b2);  
	    	}  
	    }; 
		static PriorityQueue<String> dpCode = new PriorityQueue<String>(16,comp_dp);  

		static Map<String, Queue<db_monitor_stat_txn_dp>> data = new HashMap<String, Queue<db_monitor_stat_txn_dp>>();
		
		/**
		 * 需要作出修改的数据
		 */
		static List<db_monitor_stat_txn_dp> updateList = new ArrayList<db_monitor_stat_txn_dp>();
		
		public static cache load(data_source ds)
		{
			cache c = new cache();
			if(writeOn)
			c.init(ds);
			return c;
		}
		
		public void init(data_source ds)
		{
			String sql = "select TXNCODE,IPPORT, COUNTRYCODE, REGIONCODE, CITYCODE, CHANNELID, TXNID, TIME, DISPOSAL "
					+ "	FROM TMS_MONITOR_TXN_DP_STAT where IPPORT = ? order by TIME";
			batch_stmt_jdbc stmt = new batch_stmt_jdbc(ds, sql, new int[] {
					java.sql.Types.VARCHAR});
			try
			{
				stmt.query(new Object[] {String.format(COMBINATION_FIELD_STRING,
						ipaddr, port)}, new row_fetch()
				{
					public boolean fetch(ResultSet rs) throws SQLException
					{
						db_monitor_stat_txn_dp txn = new db_monitor_stat_txn_dp();
						txn.assign_stat(rs);
						add_stat_txn_dp(txn,true);
						return true;
					}
				});
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				throw new tms_exception("load db_monitor_stat_txn_dp.cache error.");
			}
			finally
			{
				stmt.close();
			}
		}
		
		public List<db_monitor_stat_txn_dp> get_stat_txn_dp_cache()
		{
			List<db_monitor_stat_txn_dp> list = new ArrayList<db_monitor_stat_txn_dp>();
			if(!writeOn)
				return list;
			//清理一下数据
			clear();
			list.addAll(updateList);
			updateList.clear();
			return list;
		}
		private void add_stat_txn_dp(db_monitor_stat_txn_dp stat_txn_dp,boolean isInit)
		{
			if(!writeOn)
				return ;
			//城市为空或者分值不在统计区间内			
			if (stat_txn_dp.citycode == NULL_CODE ||
					str_tool.is_empty(stat_txn_dp.citycode)
				|| (stat_txn_dp.disposal.toUpperCase().compareTo(sc_min) >= 0)) {
				return;
			}
			String dp = stat_txn_dp.disposal;
			//如何当前缓存数据达到最大值，
			add(stat_txn_dp,dp);
			if(dp_data_Size > dp_max_data_Size)
			{
				db_monitor_stat_txn_dp del = removeLow();
				if(del.txncode.equalsIgnoreCase(stat_txn_dp.txncode)){
				}else
				{
					del.set_indb(false);
					updateList.add(del);
					if(!isInit){
						stat_txn_dp.set_indb(true);
						updateList.add(stat_txn_dp);
					}
				}
			}else{
				if(!isInit){
					stat_txn_dp.set_indb(true);
					updateList.add(stat_txn_dp);
				}
			}
		}
		public void add_stat_txn_dp(db_monitor_stat_txn_dp stat_txn_dp)
		{
			add_stat_txn_dp(stat_txn_dp, false);
		}
		private void add(db_monitor_stat_txn_dp obj,String dp){
			Queue<db_monitor_stat_txn_dp> queue = data.get(dp);
			if (null == queue) 
			{
				dpCode.add(dp);
				queue = new PriorityQueue<db_monitor_stat_txn_dp>(1024,new Comparator<db_monitor_stat_txn_dp>(){  
			        public int compare(db_monitor_stat_txn_dp o1, db_monitor_stat_txn_dp o2) {  
			            return comp_tool.comp(o1.time, o2.time);
			        }  
			    });
				data.put(dp, queue);
			}
			queue.offer(obj);
			dp_data_Size++;
		}
		private db_monitor_stat_txn_dp removeLow(){
			String dp = dpCode.peek();
			Queue<db_monitor_stat_txn_dp> queue = data.get(dp);
			db_monitor_stat_txn_dp del = queue.poll();
			if(queue.isEmpty()){
					data.remove(dp);
					dpCode.remove();
			}
			dp_data_Size--;
			return del;
		}
		/**
		 * 清除不在当前时间天(baseTime)内的数据
		 */
		private void clear(){
			long l = getTime();
			if( l - start_time >=checkTime || isFirst){
				long c = l - keepTime;
				isFirst = false;
				start_time = l;
				//清楚超时，比较当前时间比业务时间大于1天(keepTime)的
				for(Queue<db_monitor_stat_txn_dp> queue:data.values()){
					boolean isRun = true;
					while(isRun && !queue.isEmpty()){
						db_monitor_stat_txn_dp dp = queue.peek();
						if(dp.time < c){
							dp.set_indb(false);
							updateList.add(dp);
							queue.remove();
							dp_data_Size--;
						}else{
							isRun = false;
						}
					}
				}
			}
		}
		private static long getTime(){
			return System.currentTimeMillis();
		}
	}
	@Override
    public void assign_stat(ResultSet rs) throws SQLException {
		super.assign_stat(rs);
		this.txncode = rs.getString("TXNCODE");
        this.txnid = rs.getString("TXNID");
        this.channelid = rs.getString("CHANNELID");
        this.countrycode = rs.getString("COUNTRYCODE");
        this.regioncode = rs.getString("REGIONCODE");
        this.citycode = rs.getString("CITYCODE");
        this.disposal = rs.getString("DISPOSAL");
        this.time =  rs.getLong("TIME");
	}
	@Override
	public db_monitor_stat update_stat(db_monitor_stat stat) {
		db_monitor_stat_txn_dp stat_txn = (db_monitor_stat_txn_dp) stat;
		super.update_stat(stat_txn);
		return this;
	}
	
	@Override
	public String name() {
//		return super.name() + "_" + txnid + "_" + channelid + "_" + disposal;
		return txncode;
	}
	public String txncode;//交易流水号
	public String countrycode;// 国家代码
	public String regioncode;// 省份代码
	public String citycode;// 城市代码
	public String txnid;//交易编码
	public String channelid;// 渠道代码
	public String disposal;// 处置编码
	public double score;// 分值
}