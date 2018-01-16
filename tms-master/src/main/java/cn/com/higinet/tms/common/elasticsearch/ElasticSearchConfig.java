package cn.com.higinet.tms.common.elasticsearch;

import java.net.InetAddress;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.xpack.client.PreBuiltXPackTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * ES配置类
 * */
public class ElasticSearchConfig {

	private static final Logger logger = LoggerFactory.getLogger( ElasticSearchConfig.class );

	@Value("${elasticsearch.cluster-nodes}")
	private String clusterNodes;

	@Value("${elasticsearch.cluster.name}")
	private String clusterName;

	@Value("${elasticsearch.username}")
	private String username;

	@Value("${elasticsearch.password}")
	private String password;

	private TransportClient transportClient = null;

	private PreBuiltXPackTransportClient preBuiltXPackTransportClient;

	@PostConstruct
	public void init() {
		if( transportClient != null ) {
			return;
		}
		try {
			Settings esSetting = Settings.builder().put( "cluster.name", clusterName ).put( "xpack.security.user", username + ":" + password )//用户名：密码
					.put( "client.transport.sniff", true )//增加嗅探机制，找到ES集群
					.build();
			String[] cluster = clusterNodes.split( "," );
			TransportAddress[] address = new TransportAddress[cluster.length];
			for( int i = 0; i < cluster.length; i++ ) {
				String[] ipPort = cluster[i].split( ":" );
				address[i] = new TransportAddress( InetAddress.getByName( ipPort[0].replaceAll( " ", "" ) ), Integer.valueOf( ipPort[1].replaceAll( " ", "" ) ) );
			}
			preBuiltXPackTransportClient = new PreBuiltXPackTransportClient( esSetting );
			transportClient = preBuiltXPackTransportClient.addTransportAddresses( address );
		}
		catch( Exception e ) {
			logger.error( "elasticsearch TransportClient create error!!!", e );
		}
	}

	public TransportClient getTransportClient() {
		return transportClient;
	}

	public void setTransportClient( TransportClient transportClient ) {
		this.transportClient = transportClient;
	}

	public static void main( String[] args ) {
		ElasticSearchConfig config = new ElasticSearchConfig();
		config.init();

	}
}
