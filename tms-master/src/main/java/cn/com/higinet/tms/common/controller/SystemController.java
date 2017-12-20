package cn.com.higinet.tms.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.offline.cmc_code;
import lombok.SneakyThrows;

@RestController
@RequestMapping("/system")
public class SystemController {
	
	private static final Logger log = LoggerFactory.getLogger( SystemController.class );
	
	@Value("${spring.application.instance}")
	String instance;
	
	@Value("${spring.application.name}")
	String appName;
	
	@Autowired  
    LoadBalancerClient loadBalance;

	@SneakyThrows
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Model instance() {
		Model model = new Model();
		model.addAttribute( "instance", instance );

		cmc_code code = new cmc_code();
		code.setCategoryId( "111111111111" );
		code.setCodeKey( "asdfasdfsaf" );
		model.setRow( code );
		cmc_code code2 = code.cloneEntity();
		model.put( "cmc_code", code2 );
		
		ServiceInstance serviceInstance = loadBalance.choose( appName );
		model.put( "serviceInstance", serviceInstance );
		

		return model;
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Model delCodeAction() {
		Model model = new Model();
		model.addAttribute( "sss", "sss" );
		return model;
	}
}
