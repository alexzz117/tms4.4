package cn.com.higinet.tms.common.demo.zookeeper;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.zookeeper.ZookeeperProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;

@RestController
@RequestMapping("/demo/zookeeper")
@RefreshScope
public class ZookeeperDemoController {
	
	@Value("${spring.application.name}")
	String appName;

	@Autowired
	ZookeeperProperties zookeeperProperties;

	@Autowired
	CuratorFramework curator;

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public Model get() throws Exception {
		Model model = new Model();
		String test = new String( curator.getData().forPath( "/config/tms/master/test" ) );

		model.put( "aaaaaaa", zookeeperProperties );
		model.put( "bbb", curator );
		model.put( "ccc", test );

		return model;
	}

	@RequestMapping(value = "/set", method = RequestMethod.GET)
	public Model set() throws Exception {
		
		if(curator.checkExists().forPath( "/config/higinet-tms" ) == null ) {
			curator.create().forPath( "/config/higinet-tms" );
		}
		curator.getChildren().forPath( "/config/tms-master" ).iterator();
		
		return new Model();
	}
 
	@Autowired
	LoadBalancerClient loadBalance;

	@RequestMapping(value = "/serviceInstance", method = RequestMethod.GET)
	public Model serviceInstance() {
		Model model = new Model();
		ServiceInstance serviceInstance = loadBalance.choose( appName );
		model.put( "serviceInstance", serviceInstance );
		return model;
	}

	@Autowired
	DiscoveryClient discoveryClient;

	@RequestMapping(value = "/discoveryClient", method = RequestMethod.GET)
	public Model discoveryClient() {
		Model model = new Model();
		List<ServiceInstance> instanceList = discoveryClient.getInstances( appName );
		model.put( "instanceList", instanceList );
		return model;
	}

	@Value("${test:unkown}")
	String zookeeperTest;

	@RequestMapping(value = "/zookeeperTest", method = RequestMethod.GET)
	public Model zookeeperTest() {
		Model model = new Model();
		model.put( "zookeeperTest", zookeeperTest );
		return model;
	}
}
