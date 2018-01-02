package cn.com.higinet.tms;

import org.elasticsearch.client.transport.TransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan(basePackages = {"cn.com.higinet.tms"})
public class ElasticDemoApplicationTests {
	
	@Autowired//初始化客户端
	private ElasticsearchConfig elasticsearchConfig;
	
	@Autowired//service接口
	private ElasticsearchService elasticsearchService;
	
	TransportClient client;
	
	@Test
	public void testAddIndex() {
		//初始化客户端
		elasticsearchConfig.init();
		//获取client
		client = elasticsearchConfig.getTransportClient();  
		if(client!=null){
			//通过接口调用impl
			boolean flag = elasticsearchService.addIndex(client, "tms", 3, 2);
			System.out.println("flag:"+flag);
		}
	}

}
