package cn.com.higinet.tms.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.base.util.Timez;
import cn.com.higinet.tms.common.elasticsearch.ElasticSearchAdapter;

@Service
public class TrafficQueue {

	private static final Logger logger = LoggerFactory.getLogger( TrafficQueue.class );
	private final long saveInterval = 1000; //定时提交时间间隔
	private int saveDataSize = 5000; //当queue达到一定数量时提交
	private int queueCapacity = 50000; //队列最大堆积数量，超过put操作将堵塞
	private Long preSaveTime = System.currentTimeMillis();
	private BlockingQueue<TrafficData> queue = new LinkedBlockingQueue<TrafficData>( queueCapacity );

	@Bean("trafficExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 4 ); //线程池维护线程的最小数量
		executor.setMaxPoolSize( 16 ); //线程池维护线程的最大数量
		executor.setQueueCapacity( 8 ); //持有等待执行的任务队列
		executor.setKeepAliveSeconds( 300 ); //空闲线程的存活时间
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.DiscardPolicy() ); //不能执行的任务将被删除
		return executor;
	}

	@Autowired
	@Qualifier("trafficExecutor")
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private ElasticSearchAdapter elasticsearchAdapter;

	/**
	 * 定时进行一次ES写入
	 * fixedDelay 堵塞执行模式
	 * */
	@Scheduled(fixedDelay = saveInterval)
	private void executeTask() {
		if( (System.currentTimeMillis() - preSaveTime) > (saveInterval - 100) ) {
			this.save();
		}
	}

	public void put( TrafficData object ) throws Exception {
		queue.put( object );
		//当queue数量达到一定数量，将进行数据提交
		if( queue.size() >= saveDataSize ) {
			synchronized (queue) {
				this.save();
			}

		}
	}

	private void save() {
		executor.execute( new Runnable() {
			@Override
			public void run() {
				Timez.start();
				List<TrafficData> list = new ArrayList<TrafficData>();
				queue.drainTo( list, saveDataSize );
				if( list.size() > 0 ) elasticsearchAdapter.batchUpdate( "trafficdata", list, TrafficData.class );
				System.out.println( list.size() + "条数据已提交，耗时：" + String.valueOf( System.currentTimeMillis() - Timez.stop() ) );
			}
		} );
		preSaveTime = System.currentTimeMillis(); //记录最近一次提交时间
	}

}
