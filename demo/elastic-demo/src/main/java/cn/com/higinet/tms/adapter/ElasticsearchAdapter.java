package cn.com.higinet.tms.adapter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.persistence.Id;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.Pagination;
import cn.com.higinet.tms.provider.ElasticsearchConfig;

@Component
public class ElasticsearchAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchAdapter.class);

	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
	
	//批量大小  默认为1000
	private int commitNum = 5000;
	
	//批量提交数据量大小，单位为M，默认为5M
	private int byteSizeValue = 5;
	
	//并发请求允许同时积累新的批量执行请求   0代表着只有一个单一的请求将被允许执行 默认为1
	private int concurrentRequests = 1;
	
	//多少时间刷新为5s
	private int flushTime = 5;
	
	
	/**
	 * 存在2个问题，1：后面返回的实体怎么转换 2：查询中模糊查询和区间查询，非精确查询怎么区别
	 * @param indexName
	 * @param pageSize
	 * @param pageNo
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Pagination search(String indexName,Integer pageSize,Integer pageNo
			,Map<String,Object> map,Class<T> entityType){
		Pagination page = new Pagination();
		pageNo = null==pageNo?1:pageNo;
		page.setPageNo(pageNo);
		page.setPageSize(pageSize);
		SearchRequestBuilder searchRequestBuilder = elasticsearchConfig.getTransportClient().prepareSearch(indexName)
				.setTypes(indexName).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		searchRequestBuilder.setFrom((pageNo - 1) * pageSize).setSize(pageSize).setExplain(true);;
		 for (Map.Entry<String,Object> entry : map.entrySet()) {
			 searchRequestBuilder.setQuery(QueryBuilders.termQuery(entry.getKey(), entry.getValue())); 
		 }
//			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").gte(startTime));
//			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").from(startTime).to(endTime));
//			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").lt(endTime));
//			searchRequestBuilder.setQuery(QueryBuilders.termQuery("ipaddr", data.getIpaddr()));
//			searchRequestBuilder.setQuery(QueryBuilders.termQuery("iseval", data.getIseval()));
//			searchRequestBuilder.setQuery(QueryBuilders.termQuery("disposal", data.getDisposal()));
		SearchHits hits =  searchRequestBuilder.get().getHits();
		List<T> list = new ArrayList<T>();
		for (SearchHit searchHit : hits) {
			Map<String, Object> source = searchHit.getSourceAsMap();
			String json = searchHit.getSourceAsString();
			if (!source.isEmpty()) {  
				 ObjectMapper mapper = new ObjectMapper(); 
				 try {
					 list.add(mapper.readValue(json, entityType));
				} catch (JsonParseException e) {
					logger.error("convert json to entity is error",e);
				} catch (JsonMappingException e) {
					logger.error("convert json to entity is error",e);
				} catch (IOException e) {
					logger.error("convert json to entity is error",e);
				} 
			}
		}
		page.setDataList(list);
		return page;
	}
	
	/**
	 * 根据ID获取数据
	 * @param indexName
	 * @param primaryKeyValue
	 * @param obj
	 * @return
	 */
	public <T> Object getById(String indexName,String primaryKeyValue,Class<T> entityType){
		GetResponse response = elasticsearchConfig.getTransportClient().prepareGet(indexName, indexName, primaryKeyValue).get();
		String json = response.getSourceAsString();
		Object data = null;
		if (!response.getSourceAsMap().isEmpty()) {  
			ObjectMapper mapper = new ObjectMapper();
			 try {
				data = mapper.readValue(json, entityType);
			} catch (JsonParseException e) {
				logger.error("convert json to entity is error",e);
			} catch (JsonMappingException e) {
				logger.error("convert json to entity is error",e);
			} catch (IOException e) {
				logger.error("convert json to entity is error",e);
			} 
		}
		return data;
	}
	
	/**
	 * 根据ID删除数据
	 * @param indexName
	 * @param primaryKeyValue
	 */
	public void deleteById(String indexName,String primaryKeyValue){
		elasticsearchConfig.getTransportClient().prepareDelete(indexName, indexName, primaryKeyValue).get();
	}
	
	/**
	 * 批量添加数据
	 * @param <T>
	 * @param <T>
	 * @param indexName
	 * @param mappingName
	 * @param list
	 */
	public <T> void batchUpdate(String indexName,List<T> dataList,Class<T> entityType){
		 try {
			String primaryKeyName = getPrimaryKeyName(entityType);
			if(StringUtils.isEmpty(primaryKeyName)){
				logger.warn("primaryKeyName is empty");
				return ;
			}
			BulkProcessor bulkProcessor = getBulkProcessor();
			for (int i = 0; i < dataList.size(); i++){
				JSONObject jsonObject = (JSONObject) JSONObject.toJSON(dataList.get(i));
				bulkProcessor.add(new IndexRequest(indexName, indexName, jsonObject.getString(primaryKeyName)).source(jsonObject));
			 }
			bulkProcessor.close();
		} catch (Exception e) {
			logger.error("batchUpdate is error",e);
		}
	}
	
	/**
	 * 存在更新不存在插入
	 * @param <T>
	 * @param indexName
	 * @param obj
	 */
	public <T> void upsertData(String indexName,T t){
		try {
			Map<String,String> map = getPrimaryKeyValue(t);
			String primaryKeyName = map.get("primaryKeyName");
			String primaryKeyValue = map.get("primaryKeyValue");
			if(map.isEmpty()||StringUtils.isEmpty(primaryKeyValue)){
				logger.warn("primaryKeyValue is empty");
				return ;
			}
			JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);	
			IndexRequest indexRequest = new IndexRequest(indexName, indexName, primaryKeyValue)
				        .source(XContentFactory.jsonBuilder()
				            .startObject()
				                .field(primaryKeyName, primaryKeyValue)
				            .endObject());
			UpdateRequest updateRequest = new UpdateRequest(indexName, indexName, primaryKeyValue)
			        .doc(jsonObject)
			        .upsert(indexRequest);
			try {
				elasticsearchConfig.getTransportClient().update(updateRequest).get();
			} catch (InterruptedException e) {
				logger.error("upsert is error",e);
			} catch (ExecutionException e) {
				logger.error("upsert is error",e);
			}
		} catch (IOException e) {
			logger.error("upsert is error",e);
		}
	}
	
	/**
	 * 根据字段进行更新
	 * @param indexName
	 * @param obj
	 */
	public <T> void updateData(String indexName,T t){
		Map<String,String> map = getPrimaryKeyValue(t);
		if(map.isEmpty()){
			return ; 
		}
		String primaryKeyValue = map.get("primaryKeyValue");
		if(StringUtils.isEmpty(primaryKeyValue)){
			logger.warn("primaryKeyValue is empty");
			return ;
		}
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);
		UpdateRequest updateRequest = new UpdateRequest().index(indexName).type(indexName).id(primaryKeyValue);
		try {
			updateRequest.doc(jsonObject);
			elasticsearchConfig.getTransportClient().update(updateRequest).get();
		} catch (InterruptedException e) {
			logger.error("update is error",e);
		} catch (ExecutionException e) {
			logger.error("update is error",e);
		}
	}
	
	
	private BulkProcessor getBulkProcessor(){
		BulkProcessor bulkProcessor = BulkProcessor.builder(elasticsearchConfig.getTransportClient(),
            new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId,BulkRequest request) {
            	  logger.info("executionId:"+executionId+",numberOfActions:"+request.numberOfActions());
                }
                @Override
                public void afterBulk(long executionId, BulkRequest request,  BulkResponse response) {
                	logger.info("executionId:"+executionId+",is exists error:"+response.hasFailures());
                }
                //设置ConcurrentRequest 为0，Throwable不抛错
                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                	logger.info("happen fail = " + failure.getMessage() + " cause = " +failure.getCause());
                }
            })
            .setBulkActions(commitNum)
            .setBulkSize(new ByteSizeValue(byteSizeValue, ByteSizeUnit.MB))
            .setFlushInterval(TimeValue.timeValueSeconds(flushTime))
            .setConcurrentRequests(concurrentRequests)
            .build();
		return bulkProcessor;
	}
    
	/**
	 * 获取主键名称
	 * @param clazz
	 * @param obj
	 * @return
	 */
	private <T> String getPrimaryKeyName(Class<T> entityType){
    	String primaryKeyName = "";
    	if(entityType == null) {
    		return primaryKeyName;
        }
		Field[] field  = entityType.getDeclaredFields();
		for(int i=0;i<field.length;i++){
			if(field[i].isAnnotationPresent(Id.class)) {
		       primaryKeyName = field[i].getName();
		       break;
		     }
		}
        return primaryKeyName;
    }
	
	private <T> Map<String,String> getPrimaryKeyValue(T t){
		Class<?> clazz = t.getClass();
		Map<String,String> map = new HashMap<String,String>();
		String value = "";
		Field[] field  = clazz.getDeclaredFields();
		try {
			for(int i=0;i<field.length;i++){ 
				if(field[i].isAnnotationPresent(Id.class)) {
					String key = field[i].getName();
					PropertyDescriptor pd=new PropertyDescriptor(key,clazz);
					Method getMethod = pd.getReadMethod();  
			        Object obj = getMethod.invoke(t);
			        if(obj!=null){
			        	value = String.valueOf(obj); 
			        }
			        map.put("primaryKeyName",field[i].getName());
			        map.put("primaryKeyValue", value);
			        break;
				}
			}
		} catch (IllegalAccessException e) {
			logger.error("get primary key value is error",e);
		} catch (IllegalArgumentException e) {
			logger.error("get primary key value is error",e);
		} catch (InvocationTargetException e) {
			logger.error("get primary key value is error",e);
		} catch (IntrospectionException e) {
			logger.error("get primary key value is error",e);
		}
		return map;
	}
}
