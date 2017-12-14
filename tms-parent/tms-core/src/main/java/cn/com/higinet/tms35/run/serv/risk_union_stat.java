package cn.com.higinet.tms35.run.serv;

import java.util.ArrayList;
import java.util.List;

import cn.com.higinet.tms35.comm.tmsapp;
import cn.com.higinet.tms35.core.concurrent.tms_worker;
import cn.com.higinet.tms35.core.concurrent.tms_worker_base;
import cn.com.higinet.tms35.stat.stat_row;
import cn.com.higinet.tms35.stat.serv.stat_serv;

/**
 * 风险评估线程池
 * 
 * @author lining
 * 
 */
public class risk_union_stat extends tms_worker_base<stat_row>
{
	volatile static tms_worker<stat_row> inst_eval = null;
	final static int deque_size = tmsapp.get_config("tms.union_stat.dequesize", 1024);

	public static tms_worker<stat_row> worker()
	{
		if (inst_eval != null)
			return inst_eval;

		synchronized (risk_union_stat.class)
		{
			if (inst_eval != null)
				return inst_eval;
			return inst_eval = new risk_union_stat("union-stat", deque_size);
		}
	}

	public risk_union_stat(String name, int deque_size)
	{
		super(name, deque_size);
	}

	List<stat_row> req_list = new ArrayList<stat_row>(1024);

	int sum=0;
	@Override
	protected void run_once()
	{
		req_list.clear();
		int count = this.drainTo(req_list, 1024, 100);
		if (count == 0)
			return;
		
//		sum+=count;
//		log.info("sum="+sum+",count="+count);
		stat_serv.eval_inst().request(req_list);
	}

}