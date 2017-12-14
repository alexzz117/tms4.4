package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import cn.com.higinet.tms.manager.dao.Page;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;

@Service("actionProcess")
public class ActionProcessImpl extends QueryDataProcessCommon {
	
	@SuppressWarnings("unchecked")
	public Object dataProcess(Object... args) {
		JsonDataProcess process = (JsonDataProcess) args[0];
		HttpServletRequest request = (HttpServletRequest) args[1];
		String action = process.getCustom().getStmt().getContent();
		QueryDataProcess actionProcess = getApplicationContext().getBean(action, QueryDataProcess.class);
		Object result = actionProcess.dataProcess(process, request);
		if (result == null)
			return null;
		if (result instanceof Page) {
			super.dataProcess(process, ((Page<Map<String, Object>>) result).getList());
			return result;
		}
		if (result instanceof List) {
			List<Map<String, Object>> list = (List<Map<String,Object>>) result;
			super.dataProcess(process, list);
			Page<Map<String, Object>> page = new Page<Map<String,Object>>();
			page.setIndex(1);
			page.setSize(list.size());
			page.setTotal(list.size());
			page.setList(list);
			return page;
		}
		return result;
	}
}
