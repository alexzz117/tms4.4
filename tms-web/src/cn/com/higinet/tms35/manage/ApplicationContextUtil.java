package cn.com.higinet.tms35.manage;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("apCtxUtil")
public class ApplicationContextUtil implements ApplicationContextAware {
	
	private ApplicationContext apCtx;

	public ApplicationContext getApCtx() {
		return apCtx;
	}

	public void setApplicationContext(ApplicationContext apCtx)
			throws BeansException {
		this.apCtx = apCtx;
	}

}
