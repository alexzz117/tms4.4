package cn.com.higinet.tms.common.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		if( ApplicationContextUtil.applicationContext == null ) {
			ApplicationContextUtil.applicationContext = applicationContext;
		}
	}

	public static Object getBean( String name ) {
		return ApplicationContextUtil.applicationContext.getBean( name );
	}

	public static <T> T getBean( Class<T> classs ) {
		return ApplicationContextUtil.applicationContext.getBean( classs );
	}

	public static <T> T getBean( String name, Class<T> classs ) {
		return ApplicationContextUtil.applicationContext.getBean( name, classs );
	}

	public static boolean containsBean( String name ) {
		return ApplicationContextUtil.applicationContext.containsBean( name );
	}

}
