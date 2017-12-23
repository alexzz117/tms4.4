package cn.com.higinet.tms.common.demo.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import cn.com.higinet.tms.base.util.Stringz;

public class demoJob extends QuartzJobBean {

	private static final Logger logger = LoggerFactory.getLogger( demoJob.class );

	@Override
	protected void executeInternal( JobExecutionContext arg0 ) throws JobExecutionException {
		logger.info( Stringz.valueOf( System.currentTimeMillis() )  );
	}

}
