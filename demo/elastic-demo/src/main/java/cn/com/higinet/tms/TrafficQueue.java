package cn.com.higinet.tms;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import cn.com.higinet.tms.adapter.ElasticsearchAdapter;

@Service
public class TrafficQueue {

	BlockingQueue<Trafficdata> queue = new LinkedBlockingQueue<Trafficdata>();
	
	@Autowired
	@Qualifier("trafficExecutor")
	private ThreadPoolTaskExecutor executor;
	
	@Autowired
	private ElasticsearchAdapter elasticsearchAdapter;
	/**
	 * 每秒进行一次ES写入
	 * */
	@Scheduled(cron = "0/1 * * * * ?")
	private void executeTask() {
		this.save();
	}

	public void put( Trafficdata object ) throws Exception {
		queue.put( object );
	}

	private void save() {
		List<Trafficdata> list = Lists.newArrayList();
		int count = queue.drainTo( list );
		System.out.println(count+ "条数据待提交");
		if( count > 0 ) {
			executor.execute( new Runnable() {
				@Override
				public void run() {
					elasticsearchAdapter.batchUpdate("trafficdata", list, Trafficdata.class);
					System.out.println(count+ "条数据已提交");
				}
			} );
		}

	}

}
