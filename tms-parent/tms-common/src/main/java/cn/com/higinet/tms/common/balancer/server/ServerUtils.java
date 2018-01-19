/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ServerUtils.java   
 * @Package cn.com.higinet.tms.common.balancer.server   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 19:33:02   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer.server;

import cn.com.higinet.tms.common.exception.NullParamterException;
import cn.com.higinet.tms.common.exception.UnsupportedFormatException;
import cn.com.higinet.tms.common.util.StringUtils;

/**
 * 服务器信息负载均衡辅助工具类
 *
 * @ClassName:  ServerUtils
 * @author: 王兴
 * @date:   2018-1-19 19:33:02
 * @since:  v4.4
 */
public class ServerUtils {

	/**
	 * 入参格式为ip:port[,ip:port]
	 *
	 * @param servers the servers
	 * @return the server info
	 * @throws UnsupportedFormatException the unsupported format exception
	 */
	public static ServerInfo[] stringToServerArray(String servers) throws UnsupportedFormatException {
		if (StringUtils.isNull(servers)) {
			throw new NullParamterException("Parameter can not be null for this method.");
		}
		try {
			String[] _servers = StringUtils.split(servers, ",");
			ServerInfo[] availableServers = new ServerInfo[_servers.length];
			int i = 0;
			for (String s : _servers) {
				ServerInfo serverInfo = new ServerInfo();
				String[] info = StringUtils.split(s, ":");
				serverInfo.setAddr(info[0]);
				serverInfo.setPort(Integer.valueOf(info[1]));
				availableServers[i++] = serverInfo;
			}
			return availableServers;
		} catch (Throwable e) {
			throw new UnsupportedFormatException("Please check the format of input parameter \"%s\"", e, servers);
		}
	}
}
