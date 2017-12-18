package cn.com.higinet.tms.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.entity.offline.cmc_code;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/system")
@Slf4j
public class SystemController {

	@Value("${spring.application.instance:unknow}")
	String instance;

	@SneakyThrows
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Model instance() {
		Model model = new Model();
		model.addAttribute( "instance", instance );
		log.info( JSON.toJSONString( model ) );
		log.info( "" );

		cmc_code code = new cmc_code();
		code.setCategoryId( "111111111111" );
		code.setCodeKey( "asdfasdfsaf" );
		model.setRow( code );
		
		cmc_code code2 = code.cloneEntity();
		model.put( "sss", code2 );

		return model;
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Model delCodeAction() {
		Model model = new Model();
		model.addAttribute( "sss", "sss" );
		return model;
	}
}
