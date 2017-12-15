package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.base.util.Stringz;
import cn.com.higinet.tms.manager.modules.aop.cache.CacheRefresh;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.mgr.service.BankQuotaService;

/**
 * 通道限额控制类
 * @author tlh
 */

@Controller("bankQuotaController")
@RequestMapping("/tms/bankQuota")
public class BankQuotaController {

	@Autowired
	private BankQuotaService bankQuotaService;

	@Autowired
	private ObjectMapper objectMapper = null;

	@Autowired
	private CacheRefresh commonCacheRefresh;

	/**
	 * 通道限额列表查询页面
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listBankQuotaListView() {
		return "tms/mgr/bankQuota_list";
	}

	/**
	 * 通道限额列表查询交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model listBankQuotaListActoin(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		model.setPage(bankQuotaService.listBankQuotaPage(reqs));
		return model;
	}

	/**
	 * 通道限额删除交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public Model delBankQuotaListActoin(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		String json = MapUtil.getString(reqs, "postData");
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TmsMgrWebException("删除名单Json数据解析异常");
		}
		bankQuotaService.delBankQuotaList(formList);
		refresh("TMS_BANK_QUOTA");
		return model;
	}

	/**
	 * 通道限额添加交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addBankQuotaListActoin(@RequestParam Map<String, String> reqs) {
		Model model = new Model();

		if (bankQuotaService.listBankQuotaPage(reqs).getTotal() > 0) {
			model.addError("此记录已存在");
			return model;
		}
		String id = Stringz.randomUUID();
		while (!(bankQuotaService.getBankQuotaById(id).isEmpty())) {
			id = Stringz.randomUUID();
		}
		reqs.put("ID", id);
		bankQuotaService.addBankQuota(reqs);
		refresh("TMS_BANK_QUOTA");
		return model;
	}

	/**
	 * 通道限额单个查询交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model getBankQuotaById(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		model.setRow(bankQuotaService.getBankQuotaById(reqs.get("ID")));
		return model;
	}

	/**
	 * 通道限额信息修改交易
	 * @param reqs 请求参数
	 * @return
	 */
	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model updateBankQuotaById(@RequestParam Map<String, String> reqs) {
		Model model = new Model();
		bankQuotaService.updateBankQuotaById(reqs);
		refresh("TMS_BANK_QUOTA");
		return model;
	}

	public String refresh(String refreshMsg) {
		while (commonCacheRefresh.refresh(refreshMsg).isEmpty())
			break;
		return "";
	}
}