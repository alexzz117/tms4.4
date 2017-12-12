package cn.com.higinet.tms35.manage.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertiesUtil {
	private static final Log log = LogFactory.getLog(PropertiesUtil.class);

//	private String path = File.separator + "resources" + File.separator + "config" + File.separator + "conf.properties";
	private String path = "/resources/config/conf.properties";
	private static PropertiesUtil instance = null;
	private Properties prop = null;
	public PropertiesUtil(){}
	public static synchronized PropertiesUtil getPropInstance(){
		if(instance==null){
			instance = new PropertiesUtil() ;
		}
		return instance;
	}

	public synchronized void load(){
		if(prop !=null){
			return;
		}
		prop = new Properties();
		try {
//			InputStream in = ClassLoader.getSystemResourceAsStream(path);
//			URL url = Thread.currentThread().getContextClassLoader().getResource(path);
//			InputStream in = new BufferedInputStream(new FileInputStream(url.getFile()));
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);

			prop.load(in);
			in.close();
		} catch (IOException e) {
			log.error("PropertiesUtil load error",e);
		}
	}

	public String get(String key){
		if(prop==null){
			this.load();
		}
		String value = "";
		try{
			value = prop.getProperty(key);
		}catch(Exception e){
			log.error("PropertiesUtil get error",e);
		}
		return value;
	}
	
	public String get(String key, String default_value) {
		String value = get(key);
		if (value == null || value.length() == 0) {
			value = default_value;
		}
		return value;
	}
	
	public static void main(String[] args) {
		log.debug(PropertiesUtil.getPropInstance().get("createPattern"));
	}
}
