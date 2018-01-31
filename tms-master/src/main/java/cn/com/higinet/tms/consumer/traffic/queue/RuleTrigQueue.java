package cn.com.higinet.tms.consumer.traffic.queue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.base.entity.online.tms_run_ruletrig;
import cn.com.higinet.tms.common.elasticsearch.EsAdapter;

@Component
public class RuleTrigQueue implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger( RuleTrigQueue.class );

	private BlockingQueue<tms_run_ruletrig> queue;

	@Value("${elasticsearch.ruleTrig.indexName}")
	private String esIndexName; //ES索引名

	@Value("${elasticsearch.ruleTrig.queueCapacity:50000}")
	private int queueCapacity; //队列最大堆积数量，当超过时，put操作将堵塞

	@Autowired
	private EsAdapter<tms_run_ruletrig> esAdapter;

	@PostConstruct
	private void init() {
		//初始化traffic数据队列
		queue = new LinkedBlockingQueue<tms_run_ruletrig>( queueCapacity );

		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					while( true ) {
						tms_run_ruletrig data = queue.take();
						if( data != null ) esAdapter.batchSubmit( esIndexName, data );
					}
				}
				catch( Exception e ) {
					logger.error( e.getMessage(), e );
				}
			}
		} );
		thread.setDaemon( true );
		thread.start();
	}

	public void put( tms_run_ruletrig ruleTrig ) throws Exception {
		queue.put( ruleTrig );
	}

	public void put( List<tms_run_ruletrig> objects ) throws Exception {
		if( objects == null ) return;
		for( tms_run_ruletrig ruleTrig : objects ) {
			queue.put( ruleTrig );
		}
	}

	/**
	 * Bean销毁时调用
	 * */
	@Override
	public void destroy() throws Exception {
		/*while( queue.size() > 0 ) {
			logger.info( "TrafficQueue is not empty, can't shutdown, waiting 1s" );
			Thread.sleep( 1000 );
		}
		logger.info( "TrafficQueue Shutdown" );*/
	}

}
