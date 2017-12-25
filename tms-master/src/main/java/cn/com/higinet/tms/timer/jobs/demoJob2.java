package cn.com.higinet.tms.timer.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.timer.core.TmsJob;

public class demoJob2 implements TmsJob {

	private static Logger logger = LoggerFactory.getLogger( demoJob2.class );

	@Override
	public void execute( JobExecutionContext context ) throws JobExecutionException {
		logger.info( context.getFireTime().toString() );
	}

	@Override
	public String getJobName() {
		return null;
	}

}
