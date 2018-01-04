package cn.com.higinet.tms;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
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

import cn.com.higinet.tms.provider.ElasticsearchConfig;

@Component
public class ElasticsearchServiceImpl{
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchServiceImpl.class);

	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
	
	/**
	 * 添加索引
	 * @param indexName
	 * @param primanyShard
	 * @param replicaShard
	 * @return
	 */
	public boolean addIndex(String indexName,Integer primanyShard,Integer replicaShard){
		 Settings settings = Settings.builder().put("index.number_of_shards", primanyShard)
				 .put("index.number_of_replicas", replicaShard).build();
		 	IndicesAdminClient indicesAdminClient = elasticsearchConfig.getTransportClient().admin().indices();
		    CreateIndexResponse response = indicesAdminClient.prepareCreate(indexName).setSettings(settings).get();
		    return response.isAcknowledged();
	}
	
	/**
	 * 添加mapping
	 * @param indexName
	 * @param mappingName
	 * @return
	 */
	public boolean createMapping(String indexName,String mappingName){
		if(!exists(elasticsearchConfig.getTransportClient(),indexName)){
			logger.warn("index is not exist");
			return false;
		}
		PutMappingRequest mapping = Requests.putMappingRequest(indexName).type(mappingName).source(getClassMapping(mappingName));
		PutMappingResponse response = elasticsearchConfig.getTransportClient().admin().indices().putMapping(mapping).actionGet();
		return response.isAcknowledged();
	}
	
	/**
	 * 根据交易流水删除
	 * @param indexName
	 * @param mappingName
	 * @param txncode
	 */
	public void deleteById(String indexName,String mappingName,String txncode){
		elasticsearchConfig.getTransportClient().prepareDelete(indexName, mappingName, txncode).get();
	}
	
	public Trafficdata getById(String indexName,String mappingName,String txncode){
		GetResponse response = elasticsearchConfig.getTransportClient().prepareGet(indexName, mappingName, txncode).get();
		String json = response.getSourceAsString();
		Trafficdata data = null;
		if (!response.getSourceAsMap().isEmpty()) {  
			 ObjectMapper mapper = new ObjectMapper(); 
			 try {
				 data = mapper.readValue(json, Trafficdata.class);
			} catch (JsonParseException e) {
				logger.error("convert json to entity is error",e);
			} catch (JsonMappingException e) {
				logger.error("convert json to entity is error",e);
			} catch (IOException e) {
				logger.error("convert json to entity is error",e);
			} 
		}
		System.out.println("根据txncode查询的结果为:"+json);
		return data;
	}
	
	/**
	 * 根据条件查询  如果是分页后面还需要修改
	 * @param indexName
	 * @param mappingName
	 * @param data
	 */
	public List<Trafficdata> searchTrafficdata(String indexName,String mappingName,Trafficdata data,Long startTime,Long endTime){
		SearchRequestBuilder searchRequestBuilder = elasticsearchConfig.getTransportClient().prepareSearch(indexName)
				.setTypes(mappingName).setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		if(!StringUtils.isEmpty(data.getTxncode())){
			searchRequestBuilder.setQuery(QueryBuilders.termQuery("txncode", data.getTxncode()));
		}
		if(startTime!=null&&endTime==null){
			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").gte(startTime));
		}else if(startTime!=null&&endTime!=null){
			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").from(startTime).to(endTime));
		}else if(startTime==null&&endTime!=null){
			searchRequestBuilder.setPostFilter(QueryBuilders.rangeQuery("txntime").lt(endTime));
		}
		if(!StringUtils.isEmpty(data.getIpaddr())){
			searchRequestBuilder.setQuery(QueryBuilders.termQuery("ipaddr", data.getIpaddr()));
		}
		if(!StringUtils.isEmpty(data.getIseval())){
			searchRequestBuilder.setQuery(QueryBuilders.termQuery("iseval", data.getIseval()));
		}
		if(!StringUtils.isEmpty(data.getDisposal())){
			searchRequestBuilder.setQuery(QueryBuilders.termQuery("disposal", data.getDisposal()));
		}
		SearchResponse response = searchRequestBuilder.setFrom(0).setSize(60).setExplain(true).get();
		SearchHits hits =  response.getHits();
		List<Trafficdata> list = new ArrayList<Trafficdata>();
		for (SearchHit searchHit : hits) {
			Map<String, Object> source = searchHit.getSourceAsMap();
			String json = searchHit.getSourceAsString();
			if (!source.isEmpty()) {  
				 ObjectMapper mapper = new ObjectMapper(); 
				 try {
					 list.add(mapper.readValue(json, Trafficdata.class));
				} catch (JsonParseException e) {
					logger.error("convert json to entity is error",e);
				} catch (JsonMappingException e) {
					logger.error("convert json to entity is error",e);
				} catch (IOException e) {
					logger.error("convert json to entity is error",e);
				} 
			}
		}
		System.out.println(list.size());
		return list;
	}
	
	/**
	 * 根据字段进行更新
	 * @param indexName
	 * @param mappingName
	 * @param data
	 */
	public void updateTrafficdata(String indexName,String mappingName,Trafficdata data){
		UpdateRequest updateRequest = new UpdateRequest().index(indexName).type(mappingName).id(data.getTxncode());
		try {
			updateRequest.doc(XContentFactory.jsonBuilder()
					.startObject()
						.field("txnstatus", data.getTxnstatus())
						.field("txntime", new Date().getTime())
					.endObject());
			elasticsearchConfig.getTransportClient().update(updateRequest).get();
		} catch (IOException e) {
			logger.error("update is error",e);
		} catch (InterruptedException e) {
			logger.error("update is error",e);
		} catch (ExecutionException e) {
			logger.error("update is error",e);
		}
	}
	
	/**
	 * 存在更新不存在插入
	 * @param indexName
	 * @param mappingName
	 * @param list
	 */
	public void upsertTrafficdata(String indexName,String mappingName,List<Trafficdata> list){
		try {
			for(int i=0;i<list.size();i++){  
				IndexRequest indexRequest = new IndexRequest(indexName, mappingName, list.get(i).getTxncode())
				        .source(XContentFactory.jsonBuilder()
				            .startObject()
				                .field("txncode", list.get(i).getTxncode())
				                .field("txnstatus", list.get(i).getTxnstatus())
				                .field("txntime", new Date().getTime())
				                .field("text1", "深圳南山区")
				            .endObject());
				UpdateRequest updateRequest = new UpdateRequest(indexName, mappingName, list.get(i).getTxncode())
				        .doc(XContentFactory.jsonBuilder()
				            .startObject()
				                .field("txntime", new Date().getTime())
				                .field("txnstatus", list.get(i).getTxnstatus())
				            .endObject())
				        .upsert(indexRequest);
				try {
					elasticsearchConfig.getTransportClient().update(updateRequest).get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("upsert trafficdata is error",e);
				}
			}
		} catch (IOException e) {
			logger.error("add data to trafficdata is error",e);
		}
	}
	
	public <T> void bulkRequestBuilderTrafficdata(String indexName,String mappingName,List<T> dataList){
		BulkRequestBuilder bulkRequest = elasticsearchConfig.getTransportClient().prepareBulk();
		try {
			if("trafficdata".equals(mappingName)){
				for(int i = 0 ; i < dataList.size() ; i++ ){
					JSONObject jsonObject = (JSONObject) JSONObject.toJSON(dataList.get(i));
					bulkRequest.add(elasticsearchConfig.getTransportClient().prepareIndex(indexName, mappingName, jsonObject.getString("txncode"))
			        .setSource(jsonObject));
				}
			}
			BulkResponse bulkResponse = bulkRequest.get();
			if (bulkResponse.hasFailures()) {
			   logger.warn("bulkRequestBuilderTrafficdata is exist error");
			}
		} catch (Exception e) {
			logger.error("bulkRequestBuilderTrafficdata is error",e);
		}
	}
	
	/**
	 * 批量添加数据
	 * @param <T>
	 * @param indexName
	 * @param mappingName
	 * @param list
	 */
	public <T> void bulkProcessorTrafficdata(String indexName,String mappingName,List<T> dataList){
		 BulkProcessor bulkProcessor = BulkProcessor.builder(elasticsearchConfig.getTransportClient(),
                new BulkProcessor.Listener() {
                  @Override
                    public void beforeBulk(long executionId,BulkRequest request) {
                	  System.out.println("executionId:"+executionId+",numberOfActions:"+request.numberOfActions());
                    }
                    @Override
                    public void afterBulk(long executionId, BulkRequest request,  BulkResponse response) {
                    	System.out.println("executionId:"+executionId+",是否有失败:"+response.hasFailures());
                    }
                    //设置ConcurrentRequest 为0，Throwable不抛错
                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        System.out.println("happen fail = " + failure.getMessage() + " cause = " +failure.getCause());
                    }
                })
                .setBulkActions(5000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();
		 try {
			for (int i = 0; i < dataList.size(); i++){
				JSONObject jsonObject = (JSONObject) JSONObject.toJSON(dataList.get(i));
				bulkProcessor.add(new IndexRequest(indexName, mappingName, jsonObject.getString("txncode")).source(jsonObject));
			 }
			bulkProcessor.close();
		} catch (Exception e) {
			logger.error("bulkProcessorTrafficdata is error",e);
		}
	}
	
	
	
	 private boolean exists(TransportClient client,String index){ 
		 	IndicesAdminClient adminClient = client.admin().indices();    
	        IndicesExistsRequest request = new IndicesExistsRequest(index);  
	        IndicesExistsResponse response = adminClient.exists(request).actionGet();  
	        if (response.isExists()) {  
	            return true;  
	        }  
	        return false;  
	 }  
	
 	/**
     * 从trafficdata类的字段映射处elasticsearch中的字段 
     * @return
     */
    private XContentBuilder getClassMapping(String mappingName) {
        Field[] fields = Trafficdata.class.getDeclaredFields();
        XContentBuilder builder=null;
        try {
        	builder=XContentFactory.jsonBuilder()
					.startObject()
					.startObject(mappingName)
					.startObject("properties");
			for (int i = 0; i < fields.length; i++) {
				builder.startObject(fields[i].getName()).field("type", 
						getElasticSearchMappingType(fields[i].getType().getSimpleName())).endObject();
			}
			builder.endObject()
			.endObject()
			.endObject();
		} catch (IOException e) {
			logger.error("create XContentBuilder is error",e);
		}
        return builder;
    }
    
    /**
     * 实体类型与字段类型映射
     * @param varType
     * @return
     */
    private String getElasticSearchMappingType(String varType) {
        String es = "String";
        switch (varType) {
        case "String":
        	es = "keyword";
            break;
        case "Integer":
        	es = "integer";
            break;
        case "Float":
            es = "float";
            break;
        case "Long":
            es = "long";
            break;
        default:
            es = "keyword";
            break;
        }
        return es;
    }  
	
	
}
