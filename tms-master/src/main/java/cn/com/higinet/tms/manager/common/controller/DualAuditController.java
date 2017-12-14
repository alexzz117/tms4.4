package cn.com.higinet.tms.manager.common.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 双审
 * @author yangk
 */
@Controller
public class DualAuditController {
	
	/**
	 * 切换数据源 跳转到交易初始
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@RequestMapping(value = "/dualaudit/**")
	public void forward(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {

		String forward = req.getRequestURI().substring((req.getContextPath()+"/dualaudit").length());

		req.setAttribute("forward", "self");
		req.getRequestDispatcher(forward).forward(req, resp);
		req.removeAttribute("forward");
	}
}
