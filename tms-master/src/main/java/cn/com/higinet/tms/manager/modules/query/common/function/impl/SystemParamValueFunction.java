package cn.com.higinet.tms.manager.modules.query.common.function.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.SimpleDao;
import cn.com.higinet.tms.manager.modules.query.common.function.Function;

@Service("getSystemParamValue")
public class SystemParamValueFunction implements Function {
	private static final Log log = LogFactory.getLog(SystemParamValueFunction.class);
	
	private static final String SQL = "select DATATYPE, VALUETYPE, STARTVALUE, " +
			"ENDVALUE from TMS_MGR_SYSPARAM where SYSPARAMNAME = ?";
	
	@Autowired
	private SimpleDao officialSimpleDao;
	
	@Override
	public Object execute(Object... args) {
		String value = (String) args[0];
		try {
			String[] params = (String[]) args[1];
			String paramName = params[0];
			List<Map<String, Object>> list = officialSimpleDao.queryForList(SQL, paramName);
			if (list == null || list.isEmpty())
				return null;
			return list.get(0).get("STARTVALUE");
		} catch (Exception e) {
			log.error("Date Format error.", e);
			return value;
		}
	}
}