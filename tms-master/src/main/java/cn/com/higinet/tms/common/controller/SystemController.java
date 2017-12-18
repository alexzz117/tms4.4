package cn.com.higinet.tms.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;

@RestController
@RequestMapping("/system")
public class SystemController {

	@Value("${spring.application.instance:unknow}")
	String instance;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Model instance() {
		Model model = new Model();
		model.addAttribute( "instance", instance );
		return model;
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public Model delCodeAction() {
		Model model = new Model();
		model.addAttribute( "sss", "sss" );
		return model;
	}
}
