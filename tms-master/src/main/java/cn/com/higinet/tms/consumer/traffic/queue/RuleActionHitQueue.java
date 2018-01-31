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

import cn.com.higinet.tms.base.entity.online.tms_run_rule_action_hit;
import cn.com.higinet.tms.common.elasticsearch.EsAdapter;
import cn.com.higinet.tms.common.elasticsearch.EsListener;

@Component
public class RuleActionHitQueue implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger( RuleActionHitQueue.class );

	private BlockingQueue<tms_run_rule_action_hit> queue;

	@Value("${elasticsearch.ruleActionHit.indexName}")
	private String esIndexName; //ES索引名

	@Value("${elasticsearch.ruleActionHit.queueCapacity:50000}")
	private int queueCapacity; //队列最大堆积数量，当超过时，put操作将堵塞

	@Autowired
	private EsAdapter<tms_run_rule_action_hit> esAdapter;

	@PostConstruct
	private void init() {
		//初始化traffic数据队列
		queue = new LinkedBlockingQueue<tms_run_rule_action_hit>( queueCapacity );

		esAdapter.setListener( new EsListener<tms_run_rule_action_hit>() {
			@Override
			public void before( Long executionId, List<tms_run_rule_action_hit> allList ) {

			}

			@Override
			public void onSuccess( Long executionId, List<tms_run_rule_action_hit> sucList ) {

			}

			@Override
			public void onError( Long executionId, List<tms_run_rule_action_hit> failIdList ) {

			}

			@Override
			public void after( Long executionId, List<tms_run_rule_action_hit> allList ) {

			}
		} );

		Thread thread = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					while( true ) {
						tms_run_rule_action_hit data = queue.take();
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

	public void put( tms_run_rule_action_hit object ) throws Exception {
		queue.put( object );
	}

	public void put( List<tms_run_rule_action_hit> objects ) throws Exception {
		if( objects == null ) return;
		for( tms_run_rule_action_hit item : objects ) {
			queue.put( item );
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
