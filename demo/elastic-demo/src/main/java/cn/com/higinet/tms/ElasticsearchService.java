package cn.com.higinet.tms;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.context.annotation.Configuration;

//@Configuration
public interface ElasticsearchService {
	
	/**
	 * 创建索引
	 * @param client
	 * @param indexName
	 * @param primanyShard
	 * @param replicaShard
	 * @return
	 */
	public boolean addIndex(TransportClient client, String indexName,Integer primanyShard,Integer replicaShard);
	
	/**
	 * 创建mapping
	 * @param client
	 * @param indexName
	 * @param typeName
	 */
	public boolean createMapping(TransportClient client, String indexName,String typeName);
	
}
