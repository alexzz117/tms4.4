package cn.com.higinet.tms.manager.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext( ApplicationContext apCtx ) throws BeansException {
		synchronized (applicationContext) {
			ApplicationContextUtil.applicationContext = apCtx;
		}
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static Object getBean( String name ) {
		return applicationContext.getBean( name );
	}

	public static <T> T getBean( Class<T> classs ) {
		return applicationContext.getBean( classs );
	}

	public static <T> T getBean( String name, Class<T> classs ) {
		return applicationContext.getBean( name, classs );
	}
	
	public static boolean containsBean(String name) {
		return applicationContext.containsBean( name );
	}
}
