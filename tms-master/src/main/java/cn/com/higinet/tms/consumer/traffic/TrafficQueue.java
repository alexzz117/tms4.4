package cn.com.higinet.tms.consumer.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.base.util.Clockz;
import cn.com.higinet.tms.common.config.ApplicationContextUtil;
import cn.com.higinet.tms.common.elasticsearch.ElasticSearchAdapter;

@Service
public class TrafficQueue {

	private static final Logger logger = LoggerFactory.getLogger( TrafficQueue.class );
	private Long preSaveTime = System.currentTimeMillis();
	private BlockingQueue<TrafficData> queue;

	@Value("${elasticsearch.trafficdata.indexName}")
	private String trafficDataIndexName; //trafficdata es索引名

	@Value("${elasticsearch.trafficdata.queueCapacity}")
	private int queueCapacity; //队列最大堆积数量，当超过时，put操作将堵塞

	@Value("${elasticsearch.trafficdata.saveMaxSize}")
	private int saveMaxSize; //单次提交的数量

	@Value("${elasticsearch.trafficdata.saveWhenSize}")
	private int saveWhenSize; //当queue达到一定数量时提交

	@Autowired
	@Qualifier("commitTaskExecutor")
	private ThreadPoolTaskExecutor executor;

	private ElasticSearchAdapter<TrafficData> elasticsearchAdapter;

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {
		//初始化traffic数据队列
		queue = new LinkedBlockingQueue<TrafficData>( queueCapacity );

		try {
			elasticsearchAdapter = ApplicationContextUtil.getBean( ElasticSearchAdapter.class );
			logger.info( "elasticsearchAdapter is not null" );
		}
		catch( Exception e ) {
			logger.info( "elasticsearchAdapter is null" );
			return;
		}
	}

	/**
	 * 定时进行一次ES写入
	 * 当并发量较小时，主要是定时提交起作用
	 * fixedRate 并发执行模式
	 * */
	@Scheduled(fixedRate = 2000)
	private void executeTask() {
		if( (System.currentTimeMillis() - preSaveTime) >= 2000 && queue.size() > 0 ) {
			this.save( null );
		}
	}

	public void put( TrafficData tarffic ) throws Exception {
		queue.put( tarffic );

		//当queue数量达到一定数量，将进行数据提交
		//当并发量较大时，主要是按量提交起作用
		if( queue.size() >= saveWhenSize ) {
			this.save( saveMaxSize );
		}
	}

	private void save( Integer size ) {
		if( elasticsearchAdapter == null ) return;

		executor.execute( new Runnable() {
			@Override
			public void run() {
				Clockz.start();
				List<TrafficData> list = new ArrayList<TrafficData>();
				synchronized( queue ) {
					if( size != null && size > 0 ) { //按量提交
						if( queue.size() < size ) return;
						else queue.drainTo( list, size );
					}
					else { //获取提交
						queue.drainTo( list );
					}
				}
				if( list.size() > 0 ) {
					elasticsearchAdapter.batchUpdate( trafficDataIndexName, list );
					logger.info( list.size() + "条数据已提交，耗时：" + Clockz.stop() );
				}
			}
		} );
		preSaveTime = System.currentTimeMillis(); //记录最近一次提交时间
	}

}
