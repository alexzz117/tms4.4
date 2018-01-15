package cn.com.higinet.tms.loadrunner;

import cn.com.higinet.tms.core.common.StaticParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class AppBase.
 */
public class AppBase {

	/**
	 * 指定API配置文件目录，默认classpath下面的properties目录.
	 */
	public static void init() {
		System.setProperty(StaticParameter.TMS_API_PATH, "classpath:properties");
		System.setProperty("servermonitor.updateServerList", "false");
	}

	/**
	 * 外部手动指定API配置文件目录.
	 * @param path the path
	 */
	public static void init(String path) {
		System.setProperty(StaticParameter.TMS_API_PATH, path);
	}
}
