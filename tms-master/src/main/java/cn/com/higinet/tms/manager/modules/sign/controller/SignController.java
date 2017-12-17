package cn.com.higinet.tms.manager.modules.sign.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.sign.service.SignService;

/**
 * 签约数据业务类
 * @author lining
 * @author zhang.lei
 */

@RestController("signController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/sign")
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