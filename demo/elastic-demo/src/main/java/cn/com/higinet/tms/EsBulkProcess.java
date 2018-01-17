package cn.com.higinet.tms;

import java.util.ArrayList;
import java.util.HashMap;
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

import cn.com.higinet.tms.adapter.ElasticsearchAdapter;
import cn.com.higinet.tms.adapter.MarkerEnum;
import cn.com.higinet.tms.provider.ElasticsearchConfig;

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
	public <T> BulkProcessor getBulkProcessor(ElasticsearchConfig elasticsearchConfig,String indexName,Class<T> classType,List<T> dataList){
		ElasticsearchAdapter adapter = new ElasticsearchAdapter();
		Map<MarkerEnum,List<String>> result = new HashMap<MarkerEnum,List<String>>();
		List<String> failList = new ArrayList<String>();
		BulkProcessor bulkProcessor = BulkProcessor.builder(elasticsearchConfig.getTransportClient(), new BulkProcessor.Listener() {
			@Override
			public void beforeBulk( long executionId, BulkRequest request ) {
				logger.info( "executionId:" + executionId + ",numberOfActions:" + request.numberOfActions() );
			}
			@SuppressWarnings("unchecked")
			@Override
			public void afterBulk( long executionId, BulkRequest request, BulkResponse response ) {
				if(response.hasFailures()){
					int i = 0;
					for (BulkItemResponse bulkItemResponse : response) {
					    if (bulkItemResponse.isFailed()) { 
					    	failList.add(bulkItemResponse.getId());
					    	i++;
					    }
					}
					result.put(MarkerEnum.PARTIAL_SUCCESS, failList);
					logger.info("总共请求为："+request.numberOfActions()+",异常为："+i);
				}else{
					result.put(MarkerEnum.ALL_SUCCESS, null);
				}
				adapter.callback(indexName,classType,result,dataList);
			}
			@SuppressWarnings("unchecked")
			@Override
			public void afterBulk( long executionId, BulkRequest request, Throwable failure ) {
				result.put(MarkerEnum.ALL_FAIL, null);
				logger.info( "happen fail = " + failure.getMessage() + " cause = " + failure.getCause() +",afterBulk2 numberOfActions:" + request.numberOfActions());
				adapter.callback(indexName,classType,result,dataList);
			}
		} ).setBulkActions( commitNum ).setBulkSize( new ByteSizeValue( byteSizeValue, ByteSizeUnit.MB ) ).setFlushInterval( TimeValue.timeValueSeconds( flushTime ) )
				//设置回退策略，当请求执行错误时，可进行回退操作,执行错误后延迟100MS，重试三次后执行回退
		.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).setConcurrentRequests( concurrentRequests ).build();
		return bulkProcessor;
	}

}
