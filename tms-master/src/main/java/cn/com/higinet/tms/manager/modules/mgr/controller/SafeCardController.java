package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.mgr.service.SafeCardService;

@RestController("safeCardController")
@RequestMapping("/tms/safeCard")
public class SafeCardController {

	@Autowired
	private SafeCardService safeCardService;

	@Autowired
	private CacheRefresh commonCacheRefresh;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String safeCardLisView() {
		return "tms/mgr/safeCard_list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model safeCardLisAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( safeCardService.getSafeCardList( reqs ) );
		return model;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addSafeCardAction( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		if( !safeCardService.getTableList( "TMS_MGR_SAFE_CARD", "ACCOUNTID", reqs.get( "ACCOUNTID" ) ).isEmpty() ) {
			model.addError( "银行卡号已存在，请重新填写一个银行卡号" );
			return model;
		}
		safeCardService.addSafeCardAction( reqs, request );
		return model;
	}

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model getSafeCardAction( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Map<String, Object> List = safeCardService.getTableList( "TMS_MGR_SAFE_CARD", "ACCOUNTID", reqs.get( "ACCOUNTID" ) );
		model.setRow( List );
		return model;
	}

	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model modSafeCardAction( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		safeCardService.updateSafeCardByUuid( reqs, request );
		refresh( "TMS_MGR_SAFE_CARD," + reqs.get( "ACCOUNTID" ) );
		return model;
	}

	public String refresh( String refreshMsg ) {
		while (commonCacheRefresh.refresh( refreshMsg ).isEmpty())
			break;
		return "";
	}
}
