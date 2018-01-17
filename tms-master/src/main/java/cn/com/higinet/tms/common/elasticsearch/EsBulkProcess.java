package cn.com.higinet.tms.common.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.com.higinet.tms.base.util.Stringz;

@Component
public class EsBulkProcess {

	private static final Logger logger = LoggerFactory.getLogger( EsBulkProcess.class );

	//批量大小  默认为1000
	@Value("${elasticsearch.commit.number}")
	private int commitNum;

	//批量提交数据量大小，单位为M，默认为5M
	@Value("${elasticsearch.commit.bytesize}")
	private int byteSizeValue;

	//并发请求允许同时积累新的批量执行请求   0代表着只有一个单一的请求将被允许执行 默认为1
	private int concurrentRequests = 1;

	//多少时间刷新为5s
	@Value("${elasticsearch.flush.second}")
	private int flushTime;

	@SuppressWarnings("rawtypes")
	public <T> BulkProcessor getBulkProcessor( ElasticSearchConfig elasticsearchConfig, Map<String, T> dataMap, Listener listener ) {
		List<T> failList = new ArrayList<T>();
		List<T> sucList = new ArrayList<T>();
		logger.info( "getBulkProcessor : " + Stringz.valueOf( dataMap.size() ) );

		BulkProcessor bulkProcessor = BulkProcessor.builder( elasticsearchConfig.getTransportClient(), new BulkProcessor.Listener() {

			/**
			 * 提交前调用
			 * */
			@Override
			public void beforeBulk( long executionId, BulkRequest request ) {
				logger.info( "待提交数据：" + request.numberOfActions() );
			}

			/**
			 * 提交成功时调用，但有可能部分失败
			 * */
			@SuppressWarnings("unchecked")
			@Override
			public void afterBulk( long executionId, BulkRequest request, BulkResponse response ) {
				//部分提交成功
				if( response.hasFailures() ) {
					for( BulkItemResponse bulkItemResponse : response ) {
						if( bulkItemResponse.isFailed() ) failList.add( dataMap.get( bulkItemResponse.getId() ) );
						else sucList.add( dataMap.get( bulkItemResponse.getId() ) );
					}
					if( sucList.size() > 0 ) listener.onSuccess( sucList );
					if( failList.size() > 0 ) listener.onError( failList );
				}
				//全部成功
				else {
					sucList.addAll( dataMap.values() );
					listener.onSuccess( sucList );
				}
			}

			/**
			 * 所有提交失败时调用
			 * */
			@SuppressWarnings("unchecked")
			@Override
			public void afterBulk( long executionId, BulkRequest request, Throwable failure ) {
				logger.info( "happen fail = " + failure.getMessage() + " cause = " + failure.getCause() + ",afterBulk2 numberOfActions:" + request.numberOfActions() );
				failList.addAll( dataMap.values() );
				listener.onError( failList );
			}

		} ).setBulkActions( commitNum ).setBulkSize( new ByteSizeValue( byteSizeValue, ByteSizeUnit.MB ) ).setFlushInterval( TimeValue.timeValueSeconds( flushTime ) )
				//设置回退策略，当请求执行错误时，可进行回退操作,执行错误后延迟100MS，重试三次后执行回退
				.setBackoffPolicy( BackoffPolicy.exponentialBackoff( TimeValue.timeValueMillis( 100 ), 3 ) ).setConcurrentRequests( concurrentRequests ).build();
		return bulkProcessor;
	}

}
