package cn.com.higinet.tms.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.com.higinet.tms.base.entity.common.Model;

@RestController
@RequestMapping("/system")
public class SystemController {
	
	private static final Logger logger = LoggerFactory.getLogger( SystemController.class );

	@Value("${spring.application.instance:unknow}")
	String instance;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Model instance() {
		Model model = new Model();
		model.addAttribute( "instance", instance );
		
		logger.info( JSON.toJSONString( model ) );
		
		return model;
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Model delCodeAction() {
		Model model = new Model();
		model.addAttribute( "sss", "sss" );
		return model;
	}
}
