package cn.com.higinet.tms35.manage.query.service.process.impl.extend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.cmc.util.MapUtil;
import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.rapid.base.dao.SimpleDao;
import cn.com.higinet.tms35.manage.query.service.process.QueryDataProcess;

@Service("deviceQuery")
public class DfpServiceProcessImpl implements QueryDataProcess {
	private static final String PARAM_NAME = "device_id";
	private static final String APP_PROP_SQL = "SELECT P.PROP_NAME,p.PROP_TYPE,P.PROP_COMMENT,AP.STORECOLUMN" +
			" FROM TMS_DFP_APP_PROPERTIES AP,TMS_DFP_PROPERTY P WHERE AP.PROP_ID=P.PROP_ID AND AP.APP_ID = ?";
	@Autowired
	private SimpleDao tmsSimpleDao;
	
	@Override
	public Object dataProcess(Object... args) {
		HttpServletRequest request = (HttpServletRequest) args[1];
		Map<String, Object> conds = new HashMap<String, Object>();
		conds.put(PARAM_NAME, request.getParameter(PARAM_NAME));
		Map<String, Object> deviceMap = tmsSimpleDao.retrieve("TMS_DFP_DEVICE", conds);
		if (!MapUtil.isEmpty(deviceMap)) {
			String appId = MapUtil.getString(deviceMap, "APP_ID");
			List<Map<String, Object>> appPropList = tmsSimpleDao.queryForList(APP_PROP_SQL, appId);
			for (Map<String, Object> appPropMap : appPropList) {
				String column = MapUtil.getString(appPropMap, "STORECOLUMN");
				if (!StringUtil.isBlank(column)) {
					appPropMap.put("PROP_VALUE", MapUtil.getString(deviceMap, column));
				}
			}
			return appPropList;
		}
		return null;
	}
}
