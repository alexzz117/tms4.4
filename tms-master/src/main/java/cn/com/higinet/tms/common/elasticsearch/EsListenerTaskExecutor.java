package cn.com.higinet.tms.common.elasticsearch;

import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class EsListenerTaskExecutor {

	ThreadPoolTaskExecutor executor;

	@PostConstruct
	private void init() {
		executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 0 );
		executor.setMaxPoolSize( 4 );
		executor.setQueueCapacity( 16 );
		executor.setKeepAliveSeconds( 120 );
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
		executor.initialize();
	}

	public void execute( Runnable task ) {
		executor.execute( task );
	}
}
