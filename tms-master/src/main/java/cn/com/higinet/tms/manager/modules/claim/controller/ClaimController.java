/*
 * Copyright © 2000 Shanghai XXX Co. Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.modules.claim.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.modules.ac.service.AcService;

/**
 * 功能/模块:理赔
 * @author 张立群
 * @version 1.0  Jan 26, 2016
 * 类描述:
 * 修订历史:
 * 日期  作者  参考  描述
 *
 */
@Controller("claimController")
@RequestMapping("/tms/claim")
public class ClaimController {

	@Autowired
	AcService actionService35;

	/**
	* 方法描述:理赔信息列表页面
	* @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listClaimView() {
		return "tms35/claim/claim_list";
	}

	/**
	* 方法描述:理赔信息页面
	* @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addClaimView() {
		return "tms35/claim/claim_add";
	}

	/**
	* 方法描述:动作查询列表
	* @param reqs
	* @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listAction( @RequestBody Map<String, Object> reqs ) {
		Model model = new Model();
		model.setRow( actionService35.listAction( reqs ) );
		return model;
	}

}
