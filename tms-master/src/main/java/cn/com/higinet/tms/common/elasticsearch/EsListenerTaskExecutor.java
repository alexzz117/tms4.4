package cn.com.higinet.tms.common.elasticsearch;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 任务线程池，用于执行ES提交的监听函数
 * */
@Component
public class EsListenerTaskExecutor {

	private ThreadPoolTaskExecutor executor;

	@PostConstruct
	private void init() {
		executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 0 );
		executor.setMaxPoolSize( 4 );
		executor.setQueueCapacity( 16 );
		executor.setKeepAliveSeconds( 60 );
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.initialize();
	}

	public void execute( Runnable task ) {
		if( executor == null ) this.init();
		executor.execute( task );
	}
}
