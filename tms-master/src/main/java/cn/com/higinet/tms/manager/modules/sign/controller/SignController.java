package cn.com.higinet.tms.manager.modules.sign.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.sign.service.SignService;

/**
 * 签约数据业务类
 * @author lining
 * @author zhang.lei
 */
@Controller("signController")
@RequestMapping("/tms/sign")
public class SignController {
	@Autowired
	private SignService signService;

	@RequestMapping(value = "/ubmobiles", method = RequestMethod.POST)
	public Model getUserMobileAndBankMobile( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setList( signService.getUserAndBankMobileList( MapUtil.getString( reqs, "userId" ) ) );
		return model;
	}
}