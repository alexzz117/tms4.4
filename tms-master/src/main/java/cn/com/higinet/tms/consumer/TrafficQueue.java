package cn.com.higinet.tms.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
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
import cn.com.higinet.tms.common.elasticsearch.ElasticSearchAdapter;

@Service
public class TrafficQueue {

	private static final Logger logger = LoggerFactory.getLogger( TrafficQueue.class );
	private final long saveInterval = 1000; //定时提交时间间隔
	private final int saveSize = 10000; //当queue达到一定数量时提交
	private final int queueCapacity = 50000; //队列最大堆积数量，超过put操作将堵塞
	private Long preSaveTime = System.currentTimeMillis();
	private BlockingQueue<TrafficData> queue = new LinkedBlockingQueue<TrafficData>( queueCapacity );

	@Bean("trafficExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 4 );
		executor.setMaxPoolSize( 16 );
		executor.setQueueCapacity( 8 );
		executor.setKeepAliveSeconds( 300 );
		executor.setRejectedExecutionHandler( new RejectedExecutionHandler() {
			//当线程启动拒绝时，说明当前线程池占满，等待一定时间后重新启动
			@Override
			public void rejectedExecution( Runnable r, ThreadPoolExecutor executor ) {
				try {
					logger.info( "Traffic ElasticSearch 提交线程堵塞，2秒后重试" );
					Thread.sleep( 2000 );
					executor.execute( r );
				}
				catch( InterruptedException e ) {
					logger.error( e.getMessage(), e );
				}
			}
		} );
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
			List<TrafficData> list = new ArrayList<TrafficData>();
			queue.drainTo( list );
			this.save( list );
		}
	}

	public void put( TrafficData object ) throws Exception {
		queue.put( object );

		//当queue数量达到一定数量，将进行数据提交
		if( queue.size() >= saveSize ) {
			List<TrafficData> list = new ArrayList<TrafficData>();
			synchronized (queue) {
				if( queue.size() >= saveSize ) queue.drainTo( list );
			}
			this.save( list );
		}
	}

	private void save( List<TrafficData> list ) {
		if( list == null || list.isEmpty() ) return;
		executor.execute( new Runnable() {
			@Override
			public void run() {
				long time = System.currentTimeMillis();
				elasticsearchAdapter.batchUpdate( "trafficdata", list, TrafficData.class );
				System.out.println( list.size() + "条数据已提交，耗时：" + String.valueOf( System.currentTimeMillis() - time ) );
			}
		} );
		preSaveTime = System.currentTimeMillis(); //记录最近一次提交时间
	}

}
