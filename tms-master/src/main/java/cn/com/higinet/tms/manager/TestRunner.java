package cn.com.higinet.tms.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.manager.dao.SqlMap;

@Component
public class TestRunner implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger( TestRunner.class );

	@Autowired
	@Qualifier("tmsSqlMap")
	SqlMap tmsSqlMap;

	@Override
	public void run( String... arg0 ) throws Exception {
		//logger.info( tmsSqlMap.getSql( "tms.common.systime" ) );

		/*long l = System.currentTimeMillis();
		for( int i = 0; i < 50000; i++ ) {
			logger.info( "sssssssssss" );
		}
		logger.info( Stringz.valueOf( System.currentTimeMillis() - l ) );*/
	}

}
