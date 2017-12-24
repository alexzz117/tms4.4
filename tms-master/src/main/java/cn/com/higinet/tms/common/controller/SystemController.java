package cn.com.higinet.tms.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;

@RestController
@RequestMapping("/system")
@RefreshScope
public class SystemController {

	//private static final Logger log = LoggerFactory.getLogger( SystemController.class );
	
	@Autowired
	Environment env;

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

}
