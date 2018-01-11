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
import cn.com.higinet.tms.base.util.Clockz;
import cn.com.higinet.tms.common.elasticsearch.ElasticSearchAdapter;

@Service
public class TrafficQueue {

	private static final Logger logger = LoggerFactory.getLogger( TrafficQueue.class );
	private final long saveInterval = 1000; //定时提交时间间隔
	private int saveDataSize = 5000; //当queue达到一定数量时提交
	private int queueCapacity = 50000; //队列最大堆积数量，当超过时，put操作将堵塞
	private Long preSaveTime = System.currentTimeMillis();
	private BlockingQueue<TrafficData> queue = new LinkedBlockingQueue<TrafficData>( queueCapacity );

	@Bean("trafficExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 4 ); //线程池维护线程的最小数量
		executor.setMaxPoolSize( 16 ); //线程池维护线程的最大数量
		executor.setQueueCapacity( 8 ); //持有等待执行的任务队列
		executor.setKeepAliveSeconds( 300 ); //空闲线程的存活时间
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.DiscardPolicy() ); //DiscardPolicys模式，不能执行的任务将被删除
		return executor;
	}

	@Autowired
	@Qualifier("trafficExecutor")
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private ElasticSearchAdapter elasticsearchAdapter;

	/**
	 * 定时进行一次ES写入
	 * 当并发量较小时，主要是定时提交起作用
	 * fixedDelay 堵塞执行模式
	 * */
	@Scheduled(fixedDelay = saveInterval)
	private void executeTask() {
		if( (System.currentTimeMillis() - preSaveTime) > (saveInterval - 100) ) {
			this.save( null );
		}
	}

	public void put( TrafficData tarffic ) throws Exception {
		queue.put( tarffic );
		
		//当queue数量达到一定数量，将进行数据提交
		//当并发量较大时，主要是这里的提交起作用
		if( queue.size() >= saveDataSize ) {
			this.save( saveDataSize );
		}
	}

	private void save( Integer size ) {
		executor.execute( new Runnable() {
			@Override
			public void run() {
				Clockz.start();
				
				List<TrafficData> list = new ArrayList<TrafficData>();
				synchronized( queue ) {
					if( size != null && size > 0 ) {
						if( queue.size() < size ) return;
						else queue.drainTo( list, size );
					}
					else {
						queue.drainTo( list ); //获取全部
					}
				}
				if( list.size() > 0 ) {
					elasticsearchAdapter.batchUpdate( "trafficdata", list, TrafficData.class );
					preSaveTime = System.currentTimeMillis(); //记录最近一次提交时间
				}
				
				logger.info( list.size() + "条数据已提交，耗时：" + Clockz.stop() );
			}
		} );
	}

}
