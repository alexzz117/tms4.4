package cn.com.higinet.tms.manager.modules.mgr.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.ManagerConstants;
import cn.com.higinet.tms.manager.modules.common.FileUpLoadUtil;
import cn.com.higinet.tms.manager.modules.common.exception.TmsMgrWebException;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.mgr.service.RiskCaseService;

@RestController("riskCaseController")
@RequestMapping(ManagerConstants.URI_PREFIX + "/riskCase")
public class RiskCaseController {
	
	@Autowired
	private RiskCaseService riskCaseService;

	@Autowired
	private ObjectMapper objectMapper;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String riskCaseLisView() {
		return "tms/mgr/riskcase/riskCase_list";
	}

	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public Model riskCaseLisAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		model.setPage(riskCaseService.getRiskCaseList(reqs));
		return model;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Model addRiskCaseAction(@RequestBody Map<String, String> reqs, HttpServletRequest request) {
		Model model = new Model();
		String userId = reqs.get("USERID");
		// 校验用户是否存在
		if (null != userId && userId.trim().length() > 0) {
			if (riskCaseService.getTableMap("TMS_RUN_USER", "USERID", reqs.get("USERID")).isEmpty()) {
				model.addError("客户不存在，请重新填写正确的客户号");
				return model;
			}
		}

		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		reqs.put("OPERATOR_ID", (String) operator.get("OPERATOR_ID"));
		riskCaseService.addRiskCase(reqs);
		return model;
	}

	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public Model delRiskCaseListAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		String json = MapUtil.getString(reqs, "postData");
		Map<String, List<Map<String, String>>> formList = null;
		try {
			formList = objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TmsMgrWebException("删除名单Json数据解析异常");
		}
		riskCaseService.delRiskCaseBatch(formList);
		return model;
	}

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	public Model getRiskCaseAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		Map<String, Object> caseMap = riskCaseService.getTableMap("TMS_MGR_RISK_CASE", "UUID", reqs.get("UUID"));
		Timestamp curDate = (Timestamp) caseMap.get("CUR_DATE");
		caseMap.put("CUR_DATE", null != curDate && curDate.toString().length() > 10 ? curDate.toString().substring(0, 10) : curDate);
		// model.setRow(List);
		model.put("caseMap", caseMap);

		Map<String, Object> txnMap = riskCaseService.getTableMap("TMS_MGR_RISK_CASE_TXN", "CASE_UUID", reqs.get("UUID"));
		caseMap.put("USERID", txnMap.get("USERID"));
		caseMap.put("TXNCODE", txnMap.get("TXNCODE"));
		caseMap.put("ACCOUNTID", txnMap.get("ACCOUNTID"));
		caseMap.put("AMOUNT", txnMap.get("AMOUNT"));

		model.put("caseMap", caseMap);
		return model;
	}

	@RequestMapping(value = "/mod", method = RequestMethod.POST)
	public Model modRiskCaseAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		String userId = reqs.get("USERID");
		// 校验用户是否存在
		if (null != userId && userId.trim().length() > 0) {
			if (riskCaseService.getTableMap("TMS_RUN_USER", "USERID", reqs.get("USERID")).isEmpty()) {
				model.addError("客户不存在，请重新填写正确的客户号");
				return model;
			}
		}

		riskCaseService.updateRiskCase(reqs);
		return model;
	}

	@RequestMapping(value = "/modStatus", method = RequestMethod.POST)
	public Model modStatusRiskCaseAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		String uuid = reqs.get("CASE_UUID");
		riskCaseService.resetRiskCaseStatus(uuid);
		return model;
	}

	/* 因文件保存及文件下载的问题，暂时不用 */
	// @RequestMapping(value = "/addInfo", method = RequestMethod.POST)
	public String addInfoRiskCaseAction1(@RequestParam("FILE_UUID") MultipartFile file, @RequestParam Map<String, String> reqs, HttpServletRequest request) {

		if (reqs.get("IS_BANK_PROVE").equals("0")) {
			reqs.put("FILE_UUID", "");
		} else {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			long time = System.currentTimeMillis();
			String filePath = "/upload/RiskCase/" + sdf.format(time);
			String path = request.getSession().getServletContext().getRealPath("/www/res" + filePath);
			String fileName = String.valueOf(time) + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

			FileUpLoadUtil.fileUp(file, fileName, path);

			reqs.put("FILE_UUID", "/tms-web/s" + filePath + "/" + fileName);
		}

		if (reqs.get("DISPOSAL") == null) {
			reqs.put("DISPOSAL", "");
		}

		riskCaseService.addRiskCaseInvst(reqs);
		return "tms/mgr/riskcase/riskCase_list";
	}

	@RequestMapping(value = "/addInfo", method = RequestMethod.POST)
	public String addInfoRiskCaseAction(@RequestBody Map<String, String> reqs, HttpServletRequest request) {
		String[] strArray = request.getParameterValues("DISPOSAL");
		String disposal = "";
		if (null != strArray && strArray.length > 0) {
			for (int i = 0; i < strArray.length; i++) {
				if (i != 0) {// 第1个不加
					disposal = disposal + "|" + strArray[i];
				} else {
					disposal = disposal + strArray[i];
				}
			}
			reqs.put("DISPOSAL", disposal);
		}

		Map<String, Object> operator = (Map<String, Object>) request.getSession().getAttribute("OPERATOR");
		reqs.put("OPERATOR_ID", (String) operator.get("OPERATOR_ID"));
		riskCaseService.addRiskCaseInvst(reqs);
		return "tms/mgr/riskcase/riskCase_list";
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	public String infoRiskCaseLisView() {
		return "tms/mgr/riskcase/riskCaseDoInfo_list";
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.POST)
	public Model getInfoRiskCaseAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		String caseUuid = reqs.get("caseUuid");

		Map<String, Object> caseMap = riskCaseService.getTableMap("TMS_MGR_RISK_CASE", "UUID", caseUuid);
		String status = (String) caseMap.get("STATUS");

		Map<String, Object> map = riskCaseService.getRiskCaseInvst(caseUuid);
		model.put("caseInvest", map);
		model.put("status", status);
		return model;
	}

	@RequestMapping(value = "/hisList", method = RequestMethod.GET)
	public String riskCaseHisLisView() {
		return "tms/mgr/riskcase/riskCaseHis_list";
	}

	@RequestMapping(value = "/hisList", method = RequestMethod.POST)
	public Model riskCaseHisLisAction(@RequestBody Map<String, String> reqs) {
		Model model = new Model();
		model.setPage(riskCaseService.getRiskCaseHisList(reqs));
		return model;
	}
}
