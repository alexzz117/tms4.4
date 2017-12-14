package cn.com.higinet.tms.manager.modules.query.service.process.impl.extend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.common.DBConstant;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;
import cn.com.higinet.tms.manager.modules.tran.TransCommon;

@Service("txnStatQuery")
public class TxnStatServiceProcessImpl implements QueryDataProcess {
	private static final String PARAM_NAME = "txncode";
	@Autowired
	private SimpleDao tmsSimpleDao;

	@Autowired
	private SimpleDao officialSimpleDao;

	@Override
	public Object dataProcess(Object... args) {
		HttpServletRequest request = (HttpServletRequest) args[1];
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put(PARAM_NAME, request.getParameter(PARAM_NAME));
		Map<String, Object> txnMap = tmsSimpleDao.retrieve(DBConstant.TMS_RUN_TRAFFICDATA, conds);
		if (!CmcMapUtil.isEmpty(txnMap)) {
			String txnType = CmcMapUtil.getString(txnMap,
					DBConstant.TMS_RUN_TRAFFICDATA_TXNTYPE);
			String sql = "select " + DBConstant.TMS_COM_STAT_STAT_DESC + ", "
					+ DBConstant.TMS_COM_STAT_STORECOLUMN
					+ " from TMS_COM_STAT where "
					+ DBConstant.TMS_COM_STAT_STAT_TXN + " in ("
					+ TransCommon.arr2str(TransCommon.cutToIds(txnType)) + ")"
					+ " and (" + DBConstant.TMS_COM_STAT_STORECOLUMN + " is not null"
					+ " or " + DBConstant.TMS_COM_STAT_STORECOLUMN + " <> '')"
					+ " order by " + DBConstant.TMS_COM_STAT_STAT_ID;
			List<Map<String, Object>> statList = officialSimpleDao
					.queryForList(sql);
			for (Map<String, Object> statMap : statList) {
				String column = CmcMapUtil.getString(statMap, DBConstant.TMS_COM_STAT_STORECOLUMN);
				Object value = CmcMapUtil.getObject(txnMap, column);
				statMap.put("STOREVALUE", value);
			}
			return statList;
		}
		return null;
	}

}
