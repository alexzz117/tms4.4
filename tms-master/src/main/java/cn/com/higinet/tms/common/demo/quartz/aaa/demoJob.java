package cn.com.higinet.tms.common.demo.quartz.aaa;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class demoJob implements BaseJob {

	private static Logger logger = LoggerFactory.getLogger( demoJob.class );

	@Override
	public void execute( JobExecutionContext context ) throws JobExecutionException {
		logger.info( context.getFireTime().toString() );
	}

}
