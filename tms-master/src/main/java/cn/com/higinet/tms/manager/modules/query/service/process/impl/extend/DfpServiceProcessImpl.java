package cn.com.higinet.tms.manager.modules.query.service.process.impl.extend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.common.util.CmcMapUtil;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;

@Service("deviceQuery")
public class DfpServiceProcessImpl implements QueryDataProcess {
	private static final String PARAM_NAME = "device_id";
	private static final String APP_PROP_SQL = "SELECT P.PROP_NAME,p.PROP_TYPE,P.PROP_COMMENT,AP.STORECOLUMN" +
			" FROM TMS_DFP_APP_PROPERTIES AP,TMS_DFP_PROPERTY P WHERE AP.PROP_ID=P.PROP_ID AND AP.APP_ID = ?";
	
	@Autowired
	@Qualifier("dynamicSimpleDao")
	private SimpleDao dynamicSimpleDao;
	
	@Override
	public Object dataProcess(Object... args) {
		HttpServletRequest request = (HttpServletRequest) args[1];
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put(PARAM_NAME, request.getParameter(PARAM_NAME));
		Map<String, Object> deviceMap = dynamicSimpleDao.retrieve("TMS_DFP_DEVICE", conds);
		if (!CmcMapUtil.isEmpty(deviceMap)) {
			String appId = CmcMapUtil.getString(deviceMap, "APP_ID");
			List<Map<String, Object>> appPropList = dynamicSimpleDao.queryForList(APP_PROP_SQL, appId);
			for (Map<String, Object> appPropMap : appPropList) {
				String column = CmcMapUtil.getString(appPropMap, "STORECOLUMN");
				if (!CmcStringUtil.isBlank(column)) {
					appPropMap.put("PROP_VALUE", CmcMapUtil.getString(deviceMap, column));
				}
			}
			return appPropList;
		}
		return null;
	}
}
