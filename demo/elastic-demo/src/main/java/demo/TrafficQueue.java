package demo;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.assertj.core.util.Lists;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class TrafficQueue {

	BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

	public TrafficQueue() {
		executor.setCorePoolSize( 2 );
		executor.setMaxPoolSize( 8 );
		executor.setQueueCapacity( 24 );
		executor.setKeepAliveSeconds( 300 );
	}

	/**
	 * 每秒进行一次ES写入
	 * */
	@Scheduled(cron = "0/1 * * * * ?")
	private void executeTask() {
		this.save();
	}

	public void put( Object object ) throws Exception {
		queue.put( object );
	}

	private void save() {
		List<Object> list = Lists.newArrayList();
		int count = queue.drainTo( list );
		if( count > 0 ) {
			executor.execute( new Runnable() {
				@Override
				public void run() {
					//list写入ES
				}
			} );
		}

	}

}
