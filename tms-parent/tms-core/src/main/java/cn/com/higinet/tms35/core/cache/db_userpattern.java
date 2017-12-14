package cn.com.higinet.tms35.core.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.bin_stream;
import cn.com.higinet.tms35.core.cond.date_tool;

public class db_userpattern
{
	static Logger log = LoggerFactory.getLogger(db_userpattern.class);
	private final static String split_0 = "\n";
	private final static String split_1 = "\\|";
	private final static String split_2 = "#";
	
	public String userid;// 用户ID
	public String pattern_s;// 行为习惯字符串
	public Map<Integer,List<Pattern>> pattern_m;// 行为习惯对象
	
	public db_userpattern()
	{
	
	}
	
	public db_userpattern(String userid)
	{
		this.userid = userid;
	}
	
	/**
	* 方法描述:加载行为习惯缓存
	* @param bin_stream
	* @return
	*/
	
	public static db_userpattern load_from(bin_stream b) {
		db_userpattern u = new db_userpattern();
		u.userid = b.load_string();
		u.pattern_s = b.load_string();
		u.pattern_m = db_userpattern.userPattern2Map(u.pattern_s);
		return u;
	}

	/**
	* 方法描述:更新缓存数据
	* @param bs
	*/
	
	public void save_to(bin_stream bs) {
		bs.save(userid)
		  .save(pattern_s);
	}
	
	public static Map<Integer,List<Pattern>> userPattern2Map(String us){
		int STATID_INDEX = 0;
		int UPVALUE_INDEX = 1;
		int STARTDATE_INDEX = 2;
		int ENDDATE_INDEX = 3;
		int UPID_INDEX = 4;
		Map<Integer, List<Pattern>> r = new TreeMap<Integer, List<Pattern>>();
		if(us == null || us.length() == 0) return r;
		String[] oneStat = us.split(split_0);
		for (int i = 0; i < oneStat.length; i++) { 
			String[] oneStat_p = oneStat[i].split(split_1);
			List<Pattern> list = new ArrayList<Pattern>();
			String stat_id = "";
			for (int j = 0; j < oneStat_p.length; j++) {
				String[] oneStat_p_split = oneStat_p[j].split(split_2);
				String start_time = oneStat_p_split.length > STARTDATE_INDEX ? oneStat_p_split[STARTDATE_INDEX]:"";
				long start_time_l = start_time.length() == 0 ? Long.MIN_VALUE :date_tool.parse(start_time+" 00:00:00").getTime();
				String end_time = oneStat_p_split.length > ENDDATE_INDEX ? oneStat_p_split[ENDDATE_INDEX]:"";
				long end_time_l = end_time.length() == 0 ? Long.MAX_VALUE : date_tool.parse(end_time+" 24:00:00").getTime();
				//long now = System.currentTimeMillis();
				/*if((start_time.length() == 0 || now >= start_time_l) 
						&& (end_time.length() == 0 || now < end_time_l)){*/
					Pattern p = new Pattern();
					p.pattern_id = oneStat_p_split.length > UPID_INDEX ? oneStat_p_split[UPID_INDEX]:"";
					p.stat_id = oneStat_p_split.length > STATID_INDEX ? oneStat_p_split[STATID_INDEX]:"";
					p.pattern_value = oneStat_p_split.length > UPVALUE_INDEX ? oneStat_p_split[UPVALUE_INDEX]:"";
					p.start_time = start_time_l;
					p.end_time = end_time_l;
					list.add(p);
				//}
				stat_id = oneStat_p_split[0];
			}
			if(list.size() > 0) {
				r.put(Integer.parseInt(stat_id), list);
			}
		}
		return r ;
	}
	
	public static class Pattern{
		public String stat_id;
		public String pattern_id;
		public long start_time;
		public long end_time;
		public String pattern_value;
		/**
		* 方法描述:
		* @param txntime
		* @return
		*/
		public boolean is_enable(long txntime) {
			return (txntime >= start_time) && (txntime < end_time);
		}
	}
}