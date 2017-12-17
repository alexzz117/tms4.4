package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.BankSwitchService;

/**
 * 通道开关控制类
 * @author tlh
 */

@RestController("bankSwitchController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/bankSwitch")
public class BankSwitchController {

	@Autowired
	private BankSwitchService bankSwitchService;

	@Autowired
	private CacheRefresh commonCacheRefresh;

	@Autowired
	private ObjectMapper objectMapper = null;

	/**
	 * 通道开关列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listBankSwitchListView() {
		return "tms/mgr/bankSwitch_list";
	}

	/**
	 * 通道开关列表查询交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listBankSwitchListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( bankSwitchService.listBankSwitchPage( reqs ) );
		return model;
	}

	/**
	 * 通道开关删除交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public Model delBankSwitchListActoin( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		String json = MapUtil.getString( reqs, "postData" );
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue( json, Map.class );
		}
		catch( Exception e ) {
			e.printStackTrace();
			throw new TmsMgrWebException( "删除名单Json数据解析异常" );
		}
		bankSwitchService.delBankSwitchList( formList );
		refresh( "TMS_BANK_SWITCH" );
		refresh( "TMS_BANK_SWITCH_HIS" );
		return model;
	}

	/**
	 * 通道开关添加交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addBankSwitchListActoin( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();

		if( bankSwitchService.listBankSwitchPage( reqs ).getTotal() > 0 ) {
			model.addError( "此记录已存在" );
			return model;
		}
		String id = Stringz.randomUUID();
		while (!(bankSwitchService.getBankSwitchById( id ).isEmpty())) {
			id = Stringz.randomUUID();
		}
		reqs.put( "ID", id );
		bankSwitchService.addBankSwitch( reqs, request );
		refresh( "TMS_BANK_SWITCH" );
		return model;
	}

	/**
	 * 通道开关单个查询交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model getBankSwitchById( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		Map<String, Object> list = bankSwitchService.getBankSwitchById( reqs.get( "ID" ) );
		model.setRow( list );
		//model.set("list", list);
		return model;
	}

	/**
	 * 通道开关状态修改交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model updateBankSwitchById( @RequestBody Map<String, String> reqs, HttpServletRequest request ) {
		Model model = new Model();
		bankSwitchService.updateBankSwitchById( reqs, request );
		refresh( "TMS_BANK_SWITCH" );
		return model;
	}

	/**
	 * 通道开关状态修改历史页面
	 * @return
	 */
	@RequestMapping(value = "/hisList", method = RequestMethod.GET)
	public String listBankSwitchHisListView() {
		return "tms/mgr/bankSwitchHis_list";
	}

	/**
	 * 根据ID查询通道开关状态修改历史
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/hisList", method = RequestMethod.POST)
	public Model listBankSwitchHisById( @RequestBody Map<String, String> reqs ) {
		Model model = new Model();
		model.setPage( bankSwitchService.listBankSwitchHisByIdPage( reqs ) );
		return model;
	}

	public String refresh( String refreshMsg ) {
		while (commonCacheRefresh.refresh( refreshMsg ).isEmpty())
			break;
		return "";
	}
}
