package cn.com.higinet.tms.plus.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;

@RestController
@RequestMapping("/system")
@RefreshScope
public class SystemController {

	//private static final Logger log = LoggerFactory.getLogger( SystemController.class );

	@Value("${spring.application.instance}")
	String instance;

	@Value("${spring.application.name}")
	String appName;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Model instance() {
		Model model = new Model();
		model.addAttribute( "instance", instance );
		return model;
	}

	@Value("${test}")
	String zookeeperTest;

	@RequestMapping(value = "/zookeeperTest", method = RequestMethod.GET)
	public Model zookeeperTest() {
		Model model = new Model();
		model.put( "zookeeperTest", zookeeperTest );
		return model;
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
}
