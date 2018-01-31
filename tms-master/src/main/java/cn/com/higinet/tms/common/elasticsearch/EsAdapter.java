package cn.com.higinet.tms.common.elasticsearch;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.persistence.Id;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.com.higinet.tms.base.util.Clockz;
import cn.com.higinet.tms.base.util.Stringz;

@Component
@Scope("prototype") //使用多实例模式
public class EsAdapter<T> implements DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger( EsAdapter.class );

	//实体T的主键名称
	private String primaryKeyName;

	//批量大小
	@Value("${elasticsearch.commit.number}")
	private int commitNum;

	//批量提交数据量大小，单位为M
	@Value("${elasticsearch.commit.bytesize}")
	private int byteSizeValue;

	//并发请求允许同时积累新的批量执行请求,0代表着只有一个单一的请求将被允许执行 默认为1
	private int concurrentRequests = 1;

	//数据刷新时间
	@Value("${elasticsearch.flush.second}")
	private int flushTime;

	@Autowired
	private EsConfig esConfig;

	//用于存储近期时间内的提交数据
	private Map<String, T> dataMap = new LinkedHashMap<String, T>( 50000 );

	Map<Long, StringBuffer> logMap = new LinkedHashMap<Long, StringBuffer>();

	//批量处理器
	private BulkProcessor bulkProcessor;

	//执行侦听器
	private EsListener<T> listener;

	public void setListener( EsListener<T> listener ) {
		this.listener = listener;
	}

	@PostConstruct
	private void init() {
		//初始化执行侦听器
		bulkProcessor = BulkProcessor.builder( esConfig.getTransportClient(), new Listener() {
			@Override
			public void beforeBulk( long executionId, BulkRequest request ) {
				List<T> list = new ArrayList<T>();
				for( DocWriteRequest<?> docWriteRequest : request.requests() ) {
					list.add( dataMap.get( docWriteRequest.id() ) );
				}

				Clockz.start( "Listener-" + executionId );
				StringBuffer logs = logMap.get( executionId );
				if( logs == null ) {
					logs = new StringBuffer();
					logMap.put( executionId, logs );
				}
				logs.append( request.requests().get( 0 ).index() + "数据开始提交：" + list.size() );

				listener.before( executionId, list );
			}

			/**
			 * 提交成功时调用，但有可能部分失败
			 * */
			@Override
			public void afterBulk( long executionId, BulkRequest request, BulkResponse response ) {
				List<T> sucList = new ArrayList<T>();
				List<T> failList = new ArrayList<T>();
				List<T> allList = new ArrayList<T>();
				//部分提交成功
				if( response.hasFailures() ) {
					for( BulkItemResponse bulkItemResponse : response ) {
						T t = dataMap.get( bulkItemResponse.getId() );
						if( bulkItemResponse.isFailed() ) failList.add( t );
						else sucList.add( t );
						allList.add( t );
					}
				}
				//全部成功
				else {
					for( BulkItemResponse bulkItemResponse : response ) {
						T t = dataMap.get( bulkItemResponse.getId() );
						sucList.add( t );
						allList.add( t );
					}
				}

				StringBuffer logs = logMap.get( executionId );
				if( sucList.size() > 0 ) {
					logs.append( "，成功：" + sucList.size() );
					listener.onSuccess( executionId, sucList );
				}
				if( failList.size() > 0 ) {
					logs.append( "，失败：" + failList.size() );
					listener.onError( executionId, failList );
				}

				listener.after( executionId, allList );//完毕时
				logs.append( "，耗时：" + Clockz.stop( "Listener-" + executionId ) );
				logger.info( logs.toString() );
			}

			/**
			 * 所有提交失败时调用
			 * */
			@Override
			public void afterBulk( long executionId, BulkRequest request, Throwable failure ) {
				List<T> allList = new ArrayList<T>();
				for( DocWriteRequest<?> docWriteRequest : request.requests() ) {
					allList.add( dataMap.get( docWriteRequest.id() ) );
				}
				listener.onError( executionId, allList ); //错误时
				listener.after( executionId, allList );//完毕时

				StringBuffer logs = logMap.get( executionId );
				logs.append( "，失败：" + allList.size() );
				logs.append( "，耗时：" + Clockz.stop( "Listener-" + executionId ) );
				logger.info( logs.toString() );

			}

		} ).setBulkActions( commitNum ).setBulkSize( new ByteSizeValue( byteSizeValue, ByteSizeUnit.MB ) ).setFlushInterval( TimeValue.timeValueSeconds( flushTime ) )
				//设置回退策略，当请求执行错误时，可进行回退操作,执行错误后延迟100MS，重试三次后执行回退
				.setBackoffPolicy( BackoffPolicy.exponentialBackoff( TimeValue.timeValueMillis( 100 ), 3 ) ).setConcurrentRequests( concurrentRequests ).build();
	}

	/**
	 * @param indexName
	 * @param pageSize
	 * @param pageNo
	 * @param dataList
	 * @param classz
	 * @return
	 */
	public Pagination<T> search( String indexName, Integer pageSize, Integer pageNo, List<QueryConditionEntity> dataList, Class<T> classz ) {
		Pagination<T> page = new Pagination<T>();
		pageNo = null == pageNo ? 1 : pageNo;
		page.setPageNo( pageNo );
		page.setPageSize( pageSize );
		SearchRequestBuilder searchRequestBuilder = queryCondition( indexName, page, dataList );
		SearchHits hits = searchRequestBuilder.execute().actionGet().getHits();
		List<T> list = new ArrayList<T>();
		for( SearchHit searchHit : hits ) {
			Map<String, Object> source = searchHit.getSourceAsMap();
			if( !source.isEmpty() ) {
				String json = searchHit.getSourceAsString();
				list.add( JSON.parseObject( json, classz ) );
			}
		}
		page.setTotalRecord( hits.getTotalHits() );
		page.setTotalPage( (hits.getTotalHits() - 1) / pageSize + 1 );
		page.setDataList( list );
		return page;
	}

	@SuppressWarnings("rawtypes")
	private SearchRequestBuilder queryCondition( String indexName, Pagination page, List<QueryConditionEntity> dataList ) {
		SearchRequestBuilder searchRequestBuilder = esConfig.getTransportClient().prepareSearch( indexName ).setTypes( indexName ).setSearchType( SearchType.DFS_QUERY_THEN_FETCH );
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
	public T getById( String indexName, String primaryKeyValue, Class<T> classz ) {
		GetResponse response = esConfig.getTransportClient().prepareGet( indexName, indexName, primaryKeyValue ).get();
		T data = null;
		if( !response.getSourceAsMap().isEmpty() ) {
			String json = response.getSourceAsString();
			data = JSON.parseObject( json, classz );
		}
		return data;
	}

	/**
	 * 根据ID删除数据
	 * @param indexName
	 * @param primaryKeyValue
	 */
	public void deleteById( String indexName, String primaryKeyValue ) {
		esConfig.getTransportClient().prepareDelete( indexName, indexName, primaryKeyValue ).get();
	}

	/**
	 * 存在更新不存在插入
	 * @param <T>
	 * @param indexName
	 * @param obj
	 */
	public void upsertData( String indexName, T t ) {
		if( Stringz.isEmpty( indexName ) || t == null ) return;
		try {
			Map<String, String> map = getPrimaryKeyValue( t );
			String primaryKeyName = map.get( "primaryKeyName" );
			String primaryKeyValue = map.get( "primaryKeyValue" );
			if( map.isEmpty() || StringUtils.isEmpty( primaryKeyValue ) ) {
				logger.warn( "primaryKeyValue is empty" );
				return;
			}
			JSONObject jsonObject = (JSONObject) JSONObject.toJSON( t );
			IndexRequest indexRequest = new IndexRequest( indexName, indexName, primaryKeyValue ).source( XContentFactory.jsonBuilder().startObject().field( primaryKeyName, primaryKeyValue ).endObject() );
			UpdateRequest updateRequest = new UpdateRequest( indexName, indexName, primaryKeyValue ).doc( jsonObject ).upsert( indexRequest );
			try {
				esConfig.getTransportClient().update( updateRequest ).get();
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
			esConfig.getTransportClient().update( updateRequest ).get();
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
	 * @param indexName
	 * @param list
	 */
	public void batchSubmit( String indexName, List<T> dataList ) {
		if( Stringz.isEmpty( indexName ) || dataList == null || dataList.isEmpty() ) return;

		try {
			String primaryKeyName = getPrimaryKeyName( dataList.get( 0 ) );
			if( StringUtils.isEmpty( primaryKeyName ) ) {
				logger.warn( "primaryKeyName is empty" );
				return;
			}

			for( T data : dataList ) {
				JSONObject jsonObject = (JSONObject) JSONObject.toJSON( data );
				dataMap.put( jsonObject.getString( primaryKeyName ), data );
				bulkProcessor.add( new IndexRequest( indexName, indexName, jsonObject.getString( primaryKeyName ) ).source( jsonObject ) );
			}
		}
		catch( Exception e ) {
			logger.error( "batchUpdate is error", e );
		}
	}

	public void batchSubmit( String indexName, T data ) {
		if( Stringz.isEmpty( indexName ) || data == null ) return;
		try {
			String primaryKeyName = getPrimaryKeyName( data );
			if( StringUtils.isEmpty( primaryKeyName ) ) {
				logger.warn( "primaryKeyName is empty" );
				return;
			}
			JSONObject jsonObject = (JSONObject) JSONObject.toJSON( data );
			String primaryKeyValue = jsonObject.getString( primaryKeyName );
			dataMap.put( primaryKeyValue, data );
			bulkProcessor.add( new IndexRequest( indexName, indexName, primaryKeyValue ).source( jsonObject ) );
		}
		catch( Exception e ) {
			logger.error( "batchSubmit is error", e );
		}
	}

	/**
	 * 获取主键名称
	 */
	public String getPrimaryKeyName( T t ) {
		if( Stringz.isNotEmpty( primaryKeyName ) ) return primaryKeyName;

		if( t.getClass() == null ) return null;
		Field[] field = t.getClass().getDeclaredFields();
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
		if( !exists( esConfig.getTransportClient(), indexName ) ) {
			logger.warn( "index is not exist" );
			return false;
		}
		PutMappingRequest mapping = Requests.putMappingRequest( indexName ).type( indexName ).source( getClassMapping( indexName, entityType ) );
		PutMappingResponse response = esConfig.getTransportClient().admin().indices().putMapping( mapping ).actionGet();
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
		switch( varType ) {
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

	/**
	 * 应用关闭时调用
	 * */
	@Override
	public void destroy() throws Exception {
		logger.info( "ElasticSearchAdapter shutdown" );
	}
}
