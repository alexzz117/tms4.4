package cn.com.higinet.tms.consumer;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class CommitExecutorConfig {

	@Bean("commitTaskExecutor")
	public ThreadPoolTaskExecutor commitTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize( 4 ); //线程池维护线程的最小数量
		executor.setMaxPoolSize( 16 ); //线程池维护线程的最大数量
		executor.setQueueCapacity( 8 ); //持有等待执行的任务队列
		executor.setKeepAliveSeconds( 300 ); //空闲线程的存活时间
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.DiscardPolicy() ); //DiscardPolicys模式，不能执行的任务将被删除
		executor.initialize();
		return executor;
	}

	/*public static void main( String[] args ) {
		TrafficData data = CommitExecutorConfig.createData();
	
		Clockz.start();
		for( int i = 0; i < 1000000; i++ ) {
			Kryoz.toBytes( data );
		}
		System.out.println( Clockz.stop() );
	
		Clockz.start();
		for( int i = 0; i < 1000000; i++ ) {
			JSON.toJSONString( data );
		}
		System.out.println( Clockz.stop() );
	}
	
	private static TrafficData createData() {
		TrafficData data = new TrafficData();
		data.setTxnCode( UUID.randomUUID().toString() );
		data.setIpAddress( "11.11.11.11" );
		data.setDisposal( "PS01" );
		data.setCounTryCode( "5044啊啊啊啊啊啊啊大大大大大大撒大" );
		data.setRegionCode( "5044110000啊啊啊啊啊啊啊大大大" );
		data.setCityCode( "5044110000啊啊啊啊啊啊啊大大大大大大撒大苏打" );
		data.setIspCode( "" );
		data.setCreateTime( 1513040400000L );
		data.setFinishTime( 1513040400000L );
		data.setTxnStatus( 1 );
		data.setBatchNo( "123123啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setHitrulenum( 1 );
		data.setIsCorrect( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大" );
		data.setConfirmRisk( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大" );
		data.setIsEval( "1啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大" );
		data.setModelId( "102啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大" );
		data.setText1( "北京三里屯啊啊啊啊啊啊啊大大大大大大撒大苏打似的" );
		data.setText2( "北京西城区啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊啊啊大大大大大大撒大苏" );
		data.setText3( "啊啊啊啊啊啊啊大大大大大大撒大苏打似的啊啊啊啊啊" );
		data.setText5( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText6( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText7( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText8( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setText9( "北京三里屯啊啊啊啊啊啊啊大大大大" );
		data.setNum1( 1.00 );
		data.setNum2( 1.00 );
		data.setNum3( 1.00 );
		data.setNum4( 1.00 );
		data.setNum5( 1.00 );
		data.setNum6( 1.00 );
		data.setNum7( 1.00 );
		data.setNum8( 1.00 );
	
		return data;
	}*/
}
