package cn.com.higinet.tms.consumer.traffic;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.base.entity.TrafficData;
import cn.com.higinet.tms.common.elasticsearch.EsAdapter;
import cn.com.higinet.tms.common.elasticsearch.EsListener;

@Service
public class TrafficQueue implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger( TrafficQueue.class );

	private BlockingQueue<TrafficData> queue;

	@Value("${elasticsearch.trafficdata.indexName}")
	private String trafficDataIndexName; //TrafficData ES索引名

	@Value("${elasticsearch.trafficdata.queueCapacity}")
	private int queueCapacity; //队列最大堆积数量，当超过时，put操作将堵塞

	@Autowired
	private EsAdapter<TrafficData> esAdapter;

	@PostConstruct
	private void init() {
		//初始化traffic数据队列
		queue = new LinkedBlockingQueue<TrafficData>( queueCapacity );

		esAdapter.setListener( new EsListener<TrafficData>() {
			@Override
			public void before( Long executionId, List<TrafficData> allList ) {

			}

			@Override
			public void onSuccess( Long executionId, List<TrafficData> sucList ) {

			}

			@Override
			public void onError( Long executionId, List<TrafficData> failIdList ) {

			}

			@Override
			public void after( Long executionId, List<TrafficData> allList ) {

			}
		} );

		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					take();
				}
				catch( Exception e ) {
					logger.error( e.getMessage(), e );
				}
			}
		} );
		thread.setDaemon( true );
		thread.start();
	}

	public void put( TrafficData tarfficData ) throws Exception {
		queue.put( tarfficData );
	}

	public void take() throws Exception {
		while( true ) {
			//TrafficData data = queue.poll( 200, TimeUnit.MILLISECONDS );
			TrafficData data = queue.take();
			if( data != null ) esAdapter.batchSubmit( trafficDataIndexName, data );
		}
	}

	/**
	 * Bean销毁时调用
	 * */
	@Override
	public void destroy() throws Exception {
		while( queue.size() > 0 ) {
			logger.info( "TrafficQueue is not empty, can't shutdown, waiting 1s" );
			Thread.sleep( 1000 );
		}
		logger.info( "TrafficQueue Shutdown" );
	}

}
