/*
 * Copyright © 2016 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 */
package cn.com.higinet.tms.manager.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import cn.com.higinet.tms.base.entity.common.Model;
import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.dao.SimpleDao;

/**
 * 功能/模块 ：
 * 
 * @author 李宁
 * @version 1.0 2016年7月21日
 */
public class TransModelInterceptor extends HandlerInterceptorAdapter {
	private static Log log = LogFactory.getLog(TransModelInterceptor.class);

	private static final String TASK_RUNNING_SQL = "select count(1) NUM from TMS_ITR_TASK where STATUS in ('2', '3', '4')";
	private static final String COMBINE_RUNNING_SQL = "select count(1) from TMS_ITR_TASK_COMBINE where STATUS <> '6'";

	private List<String> disableUrlList;
	private SimpleDao iteratorSimpleDao;

	public void setDisableUrlList(List<String> disableUrlList) {
		this.disableUrlList = disableUrlList;
	}
	
	public void setIteratorSimpleDao(SimpleDao iteratorSimpleDao) {
		this.iteratorSimpleDao = iteratorSimpleDao;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException, IOException {
		String self = (String) request.getAttribute("forward");
		if (!CmcStringUtil.isEmpty(self) && "self".equals(self))
			return true;

		String contextPath = request.getContextPath();
		String uri = request.getRequestURI();
		if (contextPath.length() > 0) {
			uri = uri.substring(contextPath.length());
		}

		if (disableUrlList != null && !disableUrlList.isEmpty()) {
			String method = request.getMethod();
			if ("POST".equalsIgnoreCase(method)) {
				if (contains(disableUrlList, uri)) {
					// uri在不能操作的URL的集合中
					// 1、迭代任务中只有"新建"和"关闭"状态，且投产发布任务中不能有未"关闭"状态的任务，才可以修改交易模型
					String status = checkTxnModelStatus();
					if (status != null) {
						Model model = new Model();
						model.addError(status);
						ObjectMapper m = new ObjectMapper();
						m.writeValue(response.getOutputStream(), model.getModel());
						return false;
					}
				}
			}
		}
		return true;
	}

	private String checkTxnModelStatus() {
		try {
			if (iteratorSimpleDao != null) {
				long num = getNum(iteratorSimpleDao.queryForList(TASK_RUNNING_SQL));
				if (num > 0) {
					return "【规则迭代系统】中存在处于\"运行阶段\"的迭代任务, 请\"关闭\"后再操作!";
				}
				num = getNum(iteratorSimpleDao.queryForList(COMBINE_RUNNING_SQL));
				if (num > 0) {
					return "【规则迭代系统】中存在\"未关闭\"的投产发布流程, 请\"关闭\"后再操作!";
				}
			}
		} catch (Exception e) {
			log.error("查询迭代系统的数据库异常.", e);
			return "迭代系统的数据库异常[" + e.getLocalizedMessage() + "]";
		}
		return null;
	}

	private boolean contains(List<String> list, String uri) {
		for (int i = 0, len = list.size(); i < len; i++) {
			if (uri.indexOf(list.get(i)) != -1) {
				return true;
			}
		}
		return false;
	}
	
	private long getNum(List<Map<String, Object>> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}

		Map<String, Object> map = list.get(0);
		Object numObj = map.get("NUM");
		return numObj == null ? 0 : Long.parseLong(String.valueOf(numObj));
	}
}