package cn.com.higinet.tms35.manage.mgr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.com.higinet.cmc.service.CodeDictService;
import cn.com.higinet.rapid.base.dao.Order;
import cn.com.higinet.rapid.base.dao.Page;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.rapid.base.util.MapWrap;
import cn.com.higinet.tms35.manage.common.DBConstant;
import cn.com.higinet.tms35.manage.common.SequenceService;
import cn.com.higinet.tms35.manage.mgr.service.RiskCaseService;
import cn.com.higinet.tms4.model.common.CalendarUtil;

@Service("riskCaseService")
public class RiskCaseServiceImpl implements RiskCaseService {

	@Autowired
	private SimpleDao tmpSimpleDao;

	@Autowired
	private SequenceService sequenceService;

	@Autowired
	private DataSource tmpTmsDataSource;

	@Autowired
	private CodeDictService codeDictService;

	@Override
	public Page<Map<String, Object>> getRiskCaseList(Map<String, String> reqs) {
		StringBuilder sql = new StringBuilder();
		sql.append("select r.uuid,r.case_no,r.title,r.src_type,r.cur_date,r.is_re_check,r.created_date,o.login_name,o.real_name,u.userid,u.cusname,u.mobile,u.status  from TMS_MGR_RISK_CASE r left join TMS_MGR_RISK_CASE_TXN t on r.uuid=t.case_uuid "
				+ "left join TMS_RUN_USER u on t.userid=u.userid left join CMC_OPERATOR o on o.operator_id=r.operator_id ");
		sql.append("where 1=1");

		String userId = reqs.get("USERID");
		String loginName = reqs.get("LOGIN_NAME");
		String mobile = reqs.get("MOBILE");
		String caseNo = reqs.get("CASE_NO");
		String title = reqs.get("TITLE");
		String userStatus = reqs.get("USER_STATUS");
		String srcType = reqs.get("SRC_TYPE");
		String isReCheck = reqs.get("IS_RE_CHECK");
		String curDateStart = reqs.get("CUR_DATE_START");
		String curDateEnd = reqs.get("CUR_DATE_END");
		String createdDateStart = reqs.get("CREATED_DATE_START");
		String createdDateEnd = reqs.get("CREATED_DATE_END");

		Map<String, Object> map = new HashMap();

		if (null != userId && userId.trim().length() > 0) {
			sql.append(" and u.userid like '%'||:USERID||'%'");
			map.put("USERID", userId);
		}

		if (null != loginName && loginName.trim().length() > 0) {
			sql.append(" and u.cusname like '%'||:LOGIN_NAME||'%'");
			map.put("LOGIN_NAME", loginName);
		}

		if (null != mobile && mobile.trim().length() > 0) {
			sql.append(" and u.mobile like '%'||:MOBILE||'%'");
			map.put("MOBILE", mobile);
		}
		if (null != caseNo && caseNo.trim().length() > 0) {
			sql.append(" and r.case_no like '%'||:CASE_NO||'%'");
			map.put("CASE_NO", caseNo);
		}

		if (null != title && title.trim().length() > 0) {
			sql.append(" and r.title like '%'||:TITLE||'%'");
			map.put("TITLE", title);
		}

		if (null != userStatus && userStatus.trim().length() > 0) {
			sql.append(" and u.status=:USER_STATUS");
			map.put("USER_STATUS", userStatus);
		}
		if (null != srcType && srcType.trim().length() > 0) {
			sql.append(" and r.SRC_TYPE=:SRC_TYPE");
			map.put("SRC_TYPE", srcType);
		}

		if (null != isReCheck && isReCheck.trim().length() > 0) {
			sql.append(" and r.is_re_check=:IS_RE_CHECK");
			map.put("IS_RE_CHECK", isReCheck);
		}

		if (null != curDateStart && curDateStart.trim().length() > 0) {
			sql.append(" and r.cur_date>=:CUR_DATE_START");
			map.put("CUR_DATE_START", CalendarUtil.parseStringToDate(curDateStart));
		}

		if (null != curDateEnd && curDateEnd.trim().length() > 0) {
			sql.append(" and r.cur_date<=:CUR_DATE_END");
			map.put("CUR_DATE_END", CalendarUtil.parseStringToDate(curDateEnd));
		}

		if (null != createdDateStart && createdDateStart.trim().length() > 0) {
			sql.append(" and r.created_date>=:CREATED_DATE_START");
			map.put("CREATED_DATE_START", CalendarUtil.parseStringToDate(createdDateStart).getTime());
		}

		if (null != createdDateEnd && createdDateEnd.trim().length() > 0) {
			sql.append(" and r.created_date<=:CREATED_DATE_END");
			map.put("CREATED_DATE_END", CalendarUtil.parseStringToDate(createdDateEnd).getTime());
		}

		map.put("pagesize", reqs.get("pagesize"));
		map.put("pageindex", reqs.get("pageindex"));

		Page<Map<String, Object>> riskCasePage = tmpSimpleDao.pageQuery(sql.toString(), map, new Order().desc("CREATED_DATE"));
		for (Map<String, Object> operMap : riskCasePage.getList()) {
			operMap.put("OPERATOR", operMap.get("REAL_NAME") + "(" + operMap.get("login_name") + ")");
			operMap.put("OPER_HISTORY", "<a href=\"#'+(new Date().getTime())+'\" onclick=\"jcl.go('/tms/riskCase/getInfo?risk_case_id=" + operMap.get("uuid") + "')\">处理历史</a>");
			String status = (String) operMap.get("STATUS");
			if (null != status && status.trim().length() > 0) {
				if ("NORMAL".equals(status)) {
					status = "正常";
				} else if ("CANCEL".equals(status)) {
					status = "不在用";
				}
			}
			operMap.put("STATUS", status);
		}
		return riskCasePage;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addRiskCase(Map<String, String> map) {
		// 从离线库取序列号SEQ_TMS_MGR_RISK_CASE_ID
		String caseNo = sequenceService.getSequenceIdToString(DBConstant.SEQ_TMS_MGR_RISK_CASE_ID, tmpTmsDataSource);
		String caseUuid = UUID.randomUUID().toString();

		Map<String, Object> riskCaseMap = new HashMap();
		riskCaseMap.put("UUID", caseUuid);
		riskCaseMap.put("IS_RE_CHECK", "0");
		riskCaseMap.put("STATUS", "0");
		riskCaseMap.put("CASE_NO", caseNo);
		riskCaseMap.put("TITLE", map.get("TITLE"));
		riskCaseMap.put("DESCR", map.get("DESCR"));
		riskCaseMap.put("OPERATOR_ID", map.get("OPERATOR_ID"));
		riskCaseMap.put("CUR_DATE", (null != map.get("CUR_DATE") && map.get("CUR_DATE").length() > 0) ? CalendarUtil.parseStringToDate(map.get("CUR_DATE")) : null);
		riskCaseMap.put("SRC_TYPE", map.get("SRC_TYPE"));
		riskCaseMap.put("CREATED_BY", map.get("OPERATOR_ID"));
		riskCaseMap.put("CREATED_DATE", Long.toString(System.currentTimeMillis()));
		tmpSimpleDao.create("TMS_MGR_RISK_CASE", riskCaseMap);

		Map<String, Object> caseTxnMap = new HashMap();
		caseTxnMap.put("UUID", UUID.randomUUID().toString());
		caseTxnMap.put("CASE_UUID", caseUuid);
		caseTxnMap.put("TXNCODE", map.get("TXNCODE"));
		caseTxnMap.put("USERID", map.get("USERID"));
		caseTxnMap.put("ACCOUNTID", map.get("ACCOUNTID"));
		caseTxnMap.put("MOBILE", map.get("MOBILE"));
		caseTxnMap.put("DESCR", map.get("DESCR"));
		caseTxnMap.put("AMOUNT", map.get("AMOUNT"));
		tmpSimpleDao.create("TMS_MGR_RISK_CASE_TXN", caseTxnMap);
	}

	@Override
	public Map<String, Object> getTableMap(String tableName, String cond, String value) {
		Map<String, Object> map = tmpSimpleDao.retrieve(tableName, MapWrap.map(cond, value).getMap());
		return map;
	}

	@Override
	public void addRiskCaseTxn(Map<String, String> riskCaseTxnMap) {
		// TODO Auto-generated method stub

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delRiskCase(String uuid) {
		Map<String, String> invstMap = new HashMap<String, String>();
		invstMap.put("CASE_UUID", uuid);
		tmpSimpleDao.delete("TMS_MGR_RISK_CASE_INVST", invstMap);

		Map<String, String> txnMap = new HashMap<String, String>();
		txnMap.put("CASE_UUID", uuid);
		tmpSimpleDao.delete("TMS_MGR_RISK_CASE_TXN", txnMap);

		Map<String, String> caseMap = new HashMap<String, String>();
		caseMap.put("UUID", uuid);
		tmpSimpleDao.delete("TMS_MGR_RISK_CASE", caseMap);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delRiskCaseBatch(Map<String, List<Map<String, String>>> batchMap) {
		List<Map<String, String>> delList = batchMap.get("del");
		for (Map<String, String> delMap : delList) {
			String uuid = delMap.get("UUID");
			delRiskCase(uuid);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateRiskCase(Map<String, String> map) {
		// StringBuffer sql = new StringBuffer();
		// sql.append("update TMS_MGR_RISK_CASE r set r.SRC_TYPE=:SRC_TYPE,r.CUR_DATE=to_date(:CUR_DATE,'YYYY-MM-DD'),r.AMOUNT=:AMOUNT,r.DESCR=:DESCR  where r.UUID=:UUID");
		// tmpSimpleDao.executeUpdate(sql.toString(), riskCaseMap);

		String caseUuid = map.get("UUID");
		Map<String, Object> riskCaseMap = new HashMap();
		riskCaseMap.put("TITLE", map.get("TITLE"));
		riskCaseMap.put("DESCR", map.get("DESCR"));
		riskCaseMap.put("CUR_DATE", CalendarUtil.parseStringToDate(map.get("CUR_DATE")));
		riskCaseMap.put("SRC_TYPE", map.get("SRC_TYPE"));
		riskCaseMap.put("UPDATED_BY", map.get("OPERATOR_ID"));
		riskCaseMap.put("UPDATED_DATE", Long.toString(System.currentTimeMillis()));
		Map<String, Object> riskCaseCondMap = new HashMap();
		riskCaseCondMap.put("UUID", caseUuid);
		tmpSimpleDao.update("TMS_MGR_RISK_CASE", riskCaseMap, riskCaseCondMap);

		Map<String, Object> caseTxnMap = new HashMap();
		caseTxnMap.put("TXNCODE", map.get("TXNCODE"));
		caseTxnMap.put("USERID", map.get("USERID"));
		caseTxnMap.put("ACCOUNTID", map.get("ACCOUNTID"));
		caseTxnMap.put("MOBILE", map.get("MOBILE"));
		caseTxnMap.put("DESCR", map.get("DESCR"));
		caseTxnMap.put("AMOUNT", map.get("AMOUNT"));

		Map<String, Object> caseTxnCondMap = new HashMap();
		caseTxnCondMap.put("CASE_UUID", caseUuid);
		tmpSimpleDao.update("TMS_MGR_RISK_CASE_TXN", caseTxnMap, caseTxnCondMap);
	}

	@Override
	public void updateRiskCaseTxn(Map<String, String> riskCaseMapTxn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetRiskCaseStatus(String uuid) {

		// TODO 要补充冻结/禁用/暂停结算的功能

		StringBuffer sql = new StringBuffer();
		sql.append("update TMS_MGR_RISK_CASE r set r.STATUS='1'  where r.UUID=:RISK_CASE_ID");
		tmpSimpleDao.executeUpdate(sql.toString(), uuid);
	}

	@Override
	public void addRiskCaseInvst(Map<String, String> map) {

		Map<String, Object> riskCaseInvstMap = new HashMap();
		riskCaseInvstMap.put("UUID", UUID.randomUUID().toString());
		riskCaseInvstMap.put("CASE_UUID", map.get("CASE_UUID"));
		riskCaseInvstMap.put("IS_AMOUNT_CORRECT", map.get("IS_AMOUNT_CORRECT"));
		riskCaseInvstMap.put("IS_BANK_PROVE", map.get("IS_BANK_PROVE"));
		riskCaseInvstMap.put("IS_PHONE_CHECK", map.get("IS_PHONE_CHECK"));
		riskCaseInvstMap.put("IS_OWNER", map.get("IS_OWNER"));
		riskCaseInvstMap.put("FINAL_DECISION", map.get("FINAL_DECISION"));
		riskCaseInvstMap.put("DISPOSAL", map.get("DISPOSAL"));
		riskCaseInvstMap.put("DESCR", map.get("DESCR"));
		riskCaseInvstMap.put("CREATED_BY", map.get("OPERATOR_ID"));
		riskCaseInvstMap.put("CREATED_DATE", Long.toString(System.currentTimeMillis()));
		riskCaseInvstMap.put("UPDATED_BY", map.get("OPERATOR_ID"));
		riskCaseInvstMap.put("UPDATED_DATE", Long.toString(System.currentTimeMillis()));
		riskCaseInvstMap.put("FILE_UUID", null);// 没有上传文件
		// StringBuilder sql = new StringBuilder();
		// sql.append("insert into TMS_MGR_RISK_CASE_INVST(UUID,CASE_UUID,IS_AMOUNT_CORRECT,IS_BANK_PROVE,FILE_UUID,IS_PHONE_CHECK,IS_OWNER,FINAL_DECISION,DISPOSAL,DESCR,CREATED_BY,CREATED_DATE) values(:UUID,:RISK_CASE_ID,:IS_AMOUNT_CORRECT,:IS_BANK_PROVE,:FILE_UUID,:IS_PHONE_CHECK,:IS_OWNER,:FINAL_DECISION,:DISPOSAL,:DESCR,:CREATED_BY,:CREATED_DATE)");
		// tmpSimpleDao.executeUpdate(sql.toString(), riskCaseInvstMap);
		// tmpSimpleDao.create("TMS_MGR_RISK_CASE_INVST", riskCaseInvstMap);
		tmpSimpleDao
				.executeUpdate(
						"MERGE INTO TMS_MGR_RISK_CASE_INVST T USING( SELECT :UUID AS UUID,:CASE_UUID AS CASE_UUID,:IS_AMOUNT_CORRECT AS IS_AMOUNT_CORRECT,:IS_BANK_PROVE AS IS_BANK_PROVE,:FILE_UUID AS FILE_UUID,:IS_PHONE_CHECK AS IS_PHONE_CHECK,:IS_OWNER AS IS_OWNER,:FINAL_DECISION AS FINAL_DECISION,:DISPOSAL AS DISPOSAL,:DESCR AS DESCR,:CREATED_DATE AS CREATED_DATE,:CREATED_BY AS CREATED_BY,:UPDATED_DATE AS UPDATED_DATE,:UPDATED_BY AS UPDATED_BY FROM DUAL) S  "
								+ "ON (T.CASE_UUID=S.CASE_UUID) "
								+ "WHEN MATCHED THEN    UPDATE SET IS_AMOUNT_CORRECT = S.IS_AMOUNT_CORRECT,IS_BANK_PROVE = S.IS_BANK_PROVE,FILE_UUID = S.FILE_UUID,IS_PHONE_CHECK = S.IS_PHONE_CHECK,IS_OWNER = S.IS_OWNER,FINAL_DECISION = S.FINAL_DECISION,DISPOSAL = S.DISPOSAL,DESCR = S.DESCR,UPDATED_DATE = S.UPDATED_DATE,UPDATED_BY = S.UPDATED_BY "
								+ "WHEN NOT MATCHED THEN INSERT(UUID, CASE_UUID, IS_AMOUNT_CORRECT, IS_BANK_PROVE, FILE_UUID, IS_PHONE_CHECK, IS_OWNER, FINAL_DECISION, DISPOSAL, DESCR, CREATED_DATE, CREATED_BY)VALUES(S.UUID, S.CASE_UUID, S.IS_AMOUNT_CORRECT, S.IS_BANK_PROVE, S.FILE_UUID, S.IS_PHONE_CHECK, S.IS_OWNER, S.FINAL_DECISION, S.DISPOSAL, S.DESCR, S.CREATED_DATE, S.CREATED_BY)",
						riskCaseInvstMap);
	}

	@Override
	public Map<String, Object> getRiskCaseInvst(String caseUuid) {
		Map<String, Object> riskCaseInvstMap = new HashMap();
		riskCaseInvstMap.put("CASE_UUID", caseUuid);
		Map<String, Object> map = tmpSimpleDao.retrieve("TMS_MGR_RISK_CASE_INVST", riskCaseInvstMap);
		return map;
	}

	@Override
	public Page<Map<String, Object>> getRiskCaseHisList(Map<String, String> reqs) {
		StringBuilder sql = new StringBuilder();
		sql.append("select r.uuid,r.case_no,r.title,r.src_type,r.cur_date,r.is_re_check,r.created_date,o.login_name,o.real_name,u.userid,u.cusname,u.mobile,u.status,v.is_amount_correct,v.is_bank_prove,v.is_phone_check,v.is_owner,v.final_decision,v.disposal,v.descr,v.updated_by,v.updated_date  from TMS_MGR_RISK_CASE r left join TMS_MGR_RISK_CASE_TXN t on r.uuid=t.case_uuid "
				+ "left join Tms_Mgr_Risk_Case_Invst v on r.uuid = v.case_uuid left join TMS_RUN_USER u on t.userid=u.userid left join CMC_OPERATOR o on o.operator_id=r.operator_id ");
		sql.append("where 1=1");

		String userId = reqs.get("USERID");
		String loginName = reqs.get("LOGIN_NAME");
		String mobile = reqs.get("MOBILE");
		String caseNo = reqs.get("CASE_NO");
		String title = reqs.get("TITLE");
		String userStatus = reqs.get("USER_STATUS");
		String srcType = reqs.get("SRC_TYPE");
		String isReCheck = reqs.get("IS_RE_CHECK");
		String curDateStart = reqs.get("CUR_DATE_START");
		String curDateEnd = reqs.get("CUR_DATE_END");
		String createdDateStart = reqs.get("CREATED_DATE_START");
		String createdDateEnd = reqs.get("CREATED_DATE_END");

		Map<String, Object> map = new HashMap();

		if (null != userId && userId.trim().length() > 0) {
			sql.append(" and u.userid like '%'||:USERID||'%'");
			map.put("USERID", userId);
		}

		if (null != loginName && loginName.trim().length() > 0) {
			sql.append(" and u.cusname like '%'||:LOGIN_NAME||'%'");
			map.put("LOGIN_NAME", loginName);
		}

		if (null != mobile && mobile.trim().length() > 0) {
			sql.append(" and u.mobile like '%'||:MOBILE||'%'");
			map.put("MOBILE", mobile);
		}
		if (null != caseNo && caseNo.trim().length() > 0) {
			sql.append(" and r.case_no like '%'||:CASE_NO||'%'");
			map.put("CASE_NO", caseNo);
		}

		if (null != title && title.trim().length() > 0) {
			sql.append(" and r.title like '%'||:TITLE||'%'");
			map.put("TITLE", title);
		}

		if (null != userStatus && userStatus.trim().length() > 0) {
			sql.append(" and u.status=:USER_STATUS");
			map.put("USER_STATUS", userStatus);
		}
		if (null != srcType && srcType.trim().length() > 0) {
			sql.append(" and r.SRC_TYPE=:SRC_TYPE");
			map.put("SRC_TYPE", srcType);
		}

		if (null != isReCheck && isReCheck.trim().length() > 0) {
			sql.append(" and r.is_re_check=:IS_RE_CHECK");
			map.put("IS_RE_CHECK", isReCheck);
		}

		if (null != curDateStart && curDateStart.trim().length() > 0) {
			sql.append(" and r.cur_date>=:CUR_DATE_START");
			map.put("CUR_DATE_START", CalendarUtil.parseStringToDate(curDateStart));
		}

		if (null != curDateEnd && curDateEnd.trim().length() > 0) {
			sql.append(" and r.cur_date<=:CUR_DATE_END");
			map.put("CUR_DATE_END", CalendarUtil.parseStringToDate(curDateEnd));
		}

		if (null != createdDateStart && createdDateStart.trim().length() > 0) {
			sql.append(" and r.created_date>=:CREATED_DATE_START");
			map.put("CREATED_DATE_START", CalendarUtil.parseStringToDate(createdDateStart).getTime());
		}

		if (null != createdDateEnd && createdDateEnd.trim().length() > 0) {
			sql.append(" and r.created_date<=:CREATED_DATE_END");
			map.put("CREATED_DATE_END", CalendarUtil.parseStringToDate(createdDateEnd).getTime());
		}

		map.put("pagesize", reqs.get("pagesize"));
		map.put("pageindex", reqs.get("pageindex"));

		Page<Map<String, Object>> riskCasePage = tmpSimpleDao.pageQuery(sql.toString(), map, new Order().desc("CREATED_DATE"));
		for (Map<String, Object> operMap : riskCasePage.getList()) {
			// operMap.put("OPERATOR", operMap.get("REAL_NAME") + "(" + operMap.get("login_name") + ")");
			// operMap.put("OPER_HISTORY", "<a href=\"#'+(new Date().getTime())+'\" onclick=\"jcl.go('/tms/riskCase/getInfo?risk_case_id=" + operMap.get("uuid") + "')\">处理历史</a>");
			String status = (String) operMap.get("STATUS");
			if (null != status && status.trim().length() > 0) {
				if ("NORMAL".equals(status)) {
					status = "正常";
				} else if ("CANCEL".equals(status)) {
					status = "不在用";
				}
			}
			operMap.put("STATUS", status);

			
		}
		return riskCasePage;
	}

}
