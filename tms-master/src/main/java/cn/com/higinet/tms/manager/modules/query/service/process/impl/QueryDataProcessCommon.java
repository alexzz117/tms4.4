package cn.com.higinet.tms.manager.modules.query.service.process.impl;

import java.util.List;
import java.util.Map;

import org.springframework.context.support.ApplicationObjectSupport;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms.manager.modules.query.common.function.Function;
import cn.com.higinet.tms.manager.modules.query.common.model.Column;
import cn.com.higinet.tms.manager.modules.query.common.model.Custom;
import cn.com.higinet.tms.manager.modules.query.common.model.DefValue;
import cn.com.higinet.tms.manager.modules.query.common.model.Handle;
import cn.com.higinet.tms.manager.modules.query.common.model.JsonDataProcess;
import cn.com.higinet.tms.manager.modules.query.common.model.Result;
import cn.com.higinet.tms.manager.modules.query.service.process.QueryDataProcess;

public class QueryDataProcessCommon extends ApplicationObjectSupport implements QueryDataProcess {

	/**
	 * 获取默认值处理方法
	 * @param defValue  默认值
	 * @param stage		处理阶段{db、method}
	 * @return
	 */
	public DefValue defaultValueProcess(DefValue defValue){
		if(defValue == null) return null;
		//if(!StringUtil.isBlank(defValue.getValue())) return defValue;
		
		/*if("db".equals(stage)){//数据库处理
			defValue.setValue(defValue.getHandle().getDb());
		}else if("method".equals(stage)){//方法处理
			defValue.setValue(getValueByFunction(defValue.getHandle().getMethod()));
		}*/
		Handle handle = defValue.getHandle();
		if(handle != null && !CmcStringUtil.isBlank(handle.getMethod())){
			defValue.setValue(getValueByFunction(handle.getMethod()));
		}
		return defValue;
	}
	
	/**
	 * 获取db转换后sql部分
	 * @param paramName
	 * @param dbHandle
	 * @return
	 */
	public String transformValueByHandleForDb(String paramName, Handle handle) {
		if(handle == null || CmcStringUtil.isBlank(handle.getDb())) return paramName;
		return handle.getDb().replaceAll("\\?", paramName);
	}
	
	/**
	 * 获取函数转换后的数据值
	 * @param paramValue
	 * @param methodHandle
	 * @return
	 */
	public Object transformValueByHandleForMethod(String paramValue, Handle handle) {
		if(handle == null || CmcStringUtil.isBlank(handle.getMethod())) return paramValue;
		return transformValueByFunction(paramValue, handle.getMethod());
	}
	
	/**
	 * 根据转换函数对源数据字段值转换
	 * @param fdValue
	 * @param function
	 * @return
	 */
	public Object transformValueByFunction(String fdValue, String function) {
		Object value = "";
		//判断转换函数是否带有参数
		if (function.indexOf("(") != -1) {
			String funcName = function.substring(0, function.indexOf("("));
			String temp = function.substring(function.indexOf("(") + 1, function.indexOf(")"));
			String[] params = temp.split("\\,");
			for (int i = 0; i < params.length; i++) {
				params[i] = params[i].trim();
			}
			value = getApplicationContext().getBean(funcName.trim(), Function.class).execute(fdValue, params);
		} else {
			value = getApplicationContext().getBean(function.trim(), Function.class).execute(fdValue);
		}
		return value;
	}
	
	/**
	 * 根据函数获取默认值
	 * @param function
	 * @return
	 */
	public Object getValueByFunction(String function) {
		return transformValueByFunction(null, function);
	}

	@SuppressWarnings("unchecked")
	public Object dataProcess(Object... args) {
		List<Map<String, Object>> list = (List<Map<String, Object>>) args[1];
		if(list == null || list.size() == 0){
			return list;
		}
		JsonDataProcess process = (JsonDataProcess) args[0];
		Custom custom = process.getCustom();
		Result result = custom.getResult();
		for(Column column: result.getColumns()){
			Handle handle = column.getHandle();
			if(handle != null && !CmcStringUtil.isBlank(handle.getMethod())){
				for(Map<String, Object> map : list){
					String value = MapUtil.getString(map, column.getCsName());
					map.put(column.getCsName(), transformValueByFunction(value, handle.getMethod()));
				}
			}
		}
		return list;
	}
}
