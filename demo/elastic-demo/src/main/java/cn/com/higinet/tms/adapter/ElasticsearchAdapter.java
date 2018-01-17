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

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.EsBulkProcess;
import cn.com.higinet.tms.FailProcess;
import cn.com.higinet.tms.Pagination;
import cn.com.higinet.tms.SuccProcess;
import cn.com.higinet.tms.provider.ElasticsearchConfig;

@Component
public class ElasticsearchAdapter<T> {
	private static final Logger logger = LoggerFactory.getLogger( ElasticsearchAdapter.class );
	
	@Autowired
	private ElasticsearchConfig elasticsearchConfig;
	
	private Map<String,T> dataMap;

	/**
	 * @param indexName
	 * @param pageSize
	 * @param pageNo
	 * @param dataList
	 * @param entityType
	 * @return
	 */
	public Pagination<T> search( String indexName, Integer pageSize, Integer pageNo, List<QueryConditionEntity> dataList, Class<T> entityType ) {
		Pagination<T> page = new Pagination<T>();
		pageNo = null == pageNo ? 1 : pageNo;
		page.setPageNo( pageNo );
		page.setPageSize( pageSize );
		SearchRequestBuilder searchRequestBuilder = queryCondition( indexName, page, dataList );
		SearchHits hits = searchRequestBuilder.execute().actionGet().getHits();
		List<T> list = new ArrayList<T>();
		for( SearchHit searchHit : hits ) {
			Map<String, Object> source = searchHit.getSourceAsMap();
			String json = searchHit.getSourceAsString();
			if( !source.isEmpty() ) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					list.add( mapper.readValue( json, entityType ) );
				}
				catch( JsonParseException e ) {
					logger.error( "convert json to entity is error", e );
				}
				catch( JsonMappingException e ) {
					logger.error( "convert json to entity is error", e );
				}
				catch( IOException e ) {
					logger.error( "convert json to entity is error", e );
				}
			}
		}
		page.setDataList( list );
		return page;
	}

	@SuppressWarnings("rawtypes")
	private SearchRequestBuilder queryCondition( String indexName, Pagination page, List<QueryConditionEntity> dataList ) {
		SearchRequestBuilder searchRequestBuilder = elasticsearchConfig.getTransportClient().prepareSearch( indexName ).setTypes( indexName )
				.setSearchType( SearchType.DFS_QUERY_THEN_FETCH );
		searchRequestBuilder.setFrom( (page.getPageNo() - 1) * page.getPageSize() ).setSize( page.getPageSize() ).setExplain( true );
		for( int i = 0; i < dataList.size(); i++ ) {
			QueryConditionEntity condition = dataList.get( i );
			if( ConditionMarkEnum.BETWEEN.equals( condition.getQueryType() ) ) {
				RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery( condition.getName() );
				if( ConditionMarkEnum.GT.equals( condition.getStartMark() ) ) {
					rangeQuery.gt( condition.getStartValue() );
				}
				else if( ConditionMarkEnum.GTE.equals( condition.getStartMark() ) ) {
					rangeQuery.gte( condition.getStartValue() );
				}
				if( ConditionMarkEnum.LT.equals( condition.getEndMark() ) ) {
					rangeQuery.lt( condition.getEndValue() );
				}
				else if( ConditionMarkEnum.LTE.equals( condition.getEndMark() ) ) {
					rangeQuery.lte( condition.getEndValue() );
				}
				searchRequestBuilder.setQuery( rangeQuery );
			}
			else if( ConditionMarkEnum.OTHER.equals( condition.getQueryType() ) ) {
				if( ConditionMarkEnum.EQ.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.termQuery( condition.getName(), condition.getStartValue() ) );
				}
				else if( ConditionMarkEnum.LIKE_.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.prefixQuery( condition.getName(), String.valueOf( condition.getStartValue() ) ) );
				}
				else if( ConditionMarkEnum.GT.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.rangeQuery( condition.getName() ).gt( condition.getStartValue() ) );
				}
				else if( ConditionMarkEnum.GTE.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.rangeQuery( condition.getName() ).gte( condition.getStartValue() ) );
				}
				else if( ConditionMarkEnum.LT.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.rangeQuery( condition.getName() ).lt( condition.getStartValue() ) );
				}
				else if( ConditionMarkEnum.LTE.equals( condition.getStartMark() ) ) {
					searchRequestBuilder.setQuery( QueryBuilders.rangeQuery( condition.getName() ).lte( condition.getStartValue() ) );
				}
			}
			else if( ConditionMarkEnum.ORDERBY.equals( condition.getQueryType() ) ) {
				if( String.valueOf( ConditionMarkEnum.ASC ).equals( condition.getStartValue() ) ) {
					searchRequestBuilder.addSort( condition.getName(), SortOrder.ASC );
				}
				else if( String.valueOf( ConditionMarkEnum.DESC ).equals( condition.getStartValue() ) ) {
					searchRequestBuilder.addSort( condition.getName(), SortOrder.DESC );
				}
			}
		}
		return searchRequestBuilder;
	}

	/**
	 * 根据ID获取数据
	 * @param indexName
	 * @param primaryKeyValue
	 * @param obj
	 * @return
	 */
	public Object getById( String indexName, String primaryKeyValue, Class<T> entityType ) {
		GetResponse response = elasticsearchConfig.getTransportClient().prepareGet( indexName, indexName, primaryKeyValue ).get();
		String json = response.getSourceAsString();
		Object data = null;
		if( !response.getSourceAsMap().isEmpty() ) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				data = mapper.readValue( json, entityType );
			}
			catch( JsonParseException e ) {
				logger.error( "convert json to entity is error", e );
			}
			catch( JsonMappingException e ) {
				logger.error( "convert json to entity is error", e );
			}
			catch( IOException e ) {
				logger.error( "convert json to entity is error", e );
			}
		}
		return data;
	}

	/**
	 * 根据ID删除数据
	 * @param indexName
	 * @param primaryKeyValue
	 */
	public void deleteById( String indexName, String primaryKeyValue ) {
		elasticsearchConfig.getTransportClient().prepareDelete( indexName, indexName, primaryKeyValue ).get();
	}

	/**
	 * 存在更新不存在插入
	 * @param <T>
	 * @param indexName
	 * @param obj
	 */
	public void upsertData( String indexName, T t ) {
		try {
			Map<String, String> map = getPrimaryKeyValue( t );
			String primaryKeyName = map.get( "primaryKeyName" );
			String primaryKeyValue = map.get( "primaryKeyValue" );
			if( map.isEmpty() || StringUtils.isEmpty( primaryKeyValue ) ) {
				logger.warn( "primaryKeyValue is empty" );
				return;
			}
			JSONObject jsonObject = (JSONObject) JSONObject.toJSON( t );
			IndexRequest indexRequest = new IndexRequest( indexName, indexName, primaryKeyValue )
					.source( XContentFactory.jsonBuilder().startObject().field( primaryKeyName, primaryKeyValue ).endObject() );
			UpdateRequest updateRequest = new UpdateRequest( indexName, indexName, primaryKeyValue ).doc( jsonObject ).upsert( indexRequest );
			try {
				elasticsearchConfig.getTransportClient().update( updateRequest ).get();
			}
			catch( InterruptedException e ) {
				logger.error( "upsert is error", e );
			}
			catch( ExecutionException e ) {
				logger.error( "upsert is error", e );
			}
		}
		catch( IOException e ) {
			logger.error( "upsert is error", e );
		}
	}

	/**
	 * 根据字段进行更新
	 * @param indexName
	 * @param obj
	 */
	public void updateData( String indexName, T t ) {
		Map<String, String> map = getPrimaryKeyValue( t );
		if( map.isEmpty() ) {
			return;
		}
		String primaryKeyValue = map.get( "primaryKeyValue" );
		if( StringUtils.isEmpty( primaryKeyValue ) ) {
			logger.warn( "primaryKeyValue is empty" );
			return;
		}
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON( t );
		UpdateRequest updateRequest = new UpdateRequest().index( indexName ).type( indexName ).id( primaryKeyValue );
		try {
			updateRequest.doc( jsonObject );
			elasticsearchConfig.getTransportClient().update( updateRequest ).get();
		}
		catch( InterruptedException e ) {
			logger.error( "update is error", e );
		}
		catch( ExecutionException e ) {
			logger.error( "update is error", e );
		}
	}
	
	/**
	 * 批量添加数据
	 * @param <T>
	 * @param <T>
	 * @param indexName
	 * @param mappingName
	 * @param list
	 */
	@SuppressWarnings({ "rawtypes" })
	public FailProcess batchUpdate(String indexName, List<T> dataList, Class<T> entityType ) {
		FailProcess<T> failData = null;
		try {
			dataMap = new HashMap<String,T>();
			String primaryKeyName = getPrimaryKeyName(entityType);
			if( StringUtils.isEmpty( primaryKeyName ) ) {
				logger.warn( "primaryKeyName is empty" );
				return failData;
			}
			EsBulkProcess processor = new EsBulkProcess();
			BulkProcessor bulkProcessor = processor.getBulkProcessor(elasticsearchConfig,indexName,entityType,dataList);
			long s = System.currentTimeMillis();
			for( int i = 0; i < dataList.size(); i++ ) {
				JSONObject jsonObject = (JSONObject) JSONObject.toJSON( dataList.get( i ) );
				dataMap.put(jsonObject.get(primaryKeyName).toString(),dataList.get( i ));
				bulkProcessor.add( new IndexRequest( indexName, indexName, jsonObject.getString( primaryKeyName ) ).source( jsonObject ) );
			}
			System.out.println( System.currentTimeMillis() - s );
			bulkProcessor.close();
		}
		catch( Exception e ) {
			logger.error( "batchUpdate is error", e );
		}
		return failData;
	}
	
	/**
	 * 保存完毕后做一些事情
	 * @param map
	 * @param dataList
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void callback(String indexName,Class<T> classType,Map<MarkerEnum,List<String>> map,List<T> dataList){
		SuccProcess succ = new SuccProcess();
		FailProcess fail = new FailProcess();
		for (MarkerEnum key : map.keySet()) {
			 if(MarkerEnum.ALL_SUCCESS.equals(key)){
				 succ.callback(indexName,classType,dataList);
			 }else if(MarkerEnum.ALL_FAIL.equals(key)){
				 fail.callback(indexName,classType,dataList);
			 }else{
				 if(dataMap.isEmpty()){
					 logger.error("adpter中Map为空");
				 }else{
					 List<String> failIdList = map.get(key);
					 List<T> failObjectList = new ArrayList<T>();
					 for (String failId : failIdList) {
						 failObjectList.add(dataMap.get(failId));
						 dataMap.remove(failId);
					}
					fail.callback(indexName,classType,dataList);
					succ.callback(indexName,classType,(List<T>)map.values());
				 }
			 }
		 }
	}
	
	/**
	 * 获取主键名称
	 * @param clazz
	 * @param obj
	 * @return
	 */
	private String getPrimaryKeyName( Class<T> entityType ) {
		String primaryKeyName = "";
		if( entityType == null ) {
			return primaryKeyName;
		}
		Field[] field = entityType.getDeclaredFields();
		for( int i = 0; i < field.length; i++ ) {
			if( field[i].isAnnotationPresent( Id.class ) ) {
				primaryKeyName = field[i].getName();
				break;
			}
		}
		return primaryKeyName;
	}

	private Map<String, String> getPrimaryKeyValue( T t ) {
		Class<?> clazz = t.getClass();
		Map<String, String> map = new HashMap<String, String>();
		String value = "";
		Field[] field = clazz.getDeclaredFields();
		try {
			for( int i = 0; i < field.length; i++ ) {
				if( field[i].isAnnotationPresent( Id.class ) ) {
					String key = field[i].getName();
					PropertyDescriptor pd = new PropertyDescriptor( key, clazz );
					Method getMethod = pd.getReadMethod();
					Object obj = getMethod.invoke( t );
					if( obj != null ) {
						value = String.valueOf( obj );
					}
					map.put( "primaryKeyName", field[i].getName() );
					map.put( "primaryKeyValue", value );
					break;
				}
			}
		}
		catch( IllegalAccessException e ) {
			logger.error( "get primary key value is error", e );
		}
		catch( IllegalArgumentException e ) {
			logger.error( "get primary key value is error", e );
		}
		catch( InvocationTargetException e ) {
			logger.error( "get primary key value is error", e );
		}
		catch( IntrospectionException e ) {
			logger.error( "get primary key value is error", e );
		}
		return map;
	}

	public boolean createMapping( String indexName, Class<T> entityType ) {
		if( !exists( elasticsearchConfig.getTransportClient(), indexName ) ) {
			logger.warn( "index is not exist" );
			return false;
		}
		PutMappingRequest mapping = Requests.putMappingRequest( indexName ).type( indexName ).source( getClassMapping( indexName, entityType ) );
		PutMappingResponse response = elasticsearchConfig.getTransportClient().admin().indices().putMapping( mapping ).actionGet();
		return response.isAcknowledged();
	}

	private boolean exists( TransportClient client, String index ) {
		IndicesAdminClient adminClient = client.admin().indices();
		IndicesExistsRequest request = new IndicesExistsRequest( index );
		IndicesExistsResponse response = adminClient.exists( request ).actionGet();
		if( response.isExists() ) {
			return true;
		}
		return false;
	}

	private XContentBuilder getClassMapping( String mappingName, Class<T> entityType ) {
		Field[] fields = entityType.getDeclaredFields();
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject().startObject( mappingName ).startObject( "properties" );
			for( int i = 0; i < fields.length; i++ ) {
				builder.startObject( fields[i].getName() ).field( "type", getElasticSearchMappingType( fields[i].getType().getSimpleName() ) ).endObject();
			}
			builder.endObject().endObject().endObject();
		}
		catch( IOException e ) {
			logger.error( "create XContentBuilder is error", e );
		}
		return builder;
	}

	/**
	 * 实体类型与字段类型映射
	 * @param varType
	 * @return
	 */
	private String getElasticSearchMappingType( String varType ) {
		String es = "String";
		switch ( varType ) {
			case "String" :
				es = "keyword";
				break;
			case "Integer" :
				es = "integer";
				break;
			case "Float" :
				es = "float";
				break;
			case "Long" :
				es = "long";
				break;
			default :
				es = "keyword";
				break;
		}
		return es;
	}
}
