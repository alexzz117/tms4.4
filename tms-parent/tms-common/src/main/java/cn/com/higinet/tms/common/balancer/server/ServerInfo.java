/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ServerInfo.java   
 * @Package cn.com.higinet.tms.common.balancer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-19 15:27:56   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.balancer.server;

/**
 * 服务器信息，所有服务器信息应该都是可用的，这里不会去维护哪个服务器可用哪个不可用
 *
 * @ClassName:  ServerInfo
 * @author: 王兴
 * @date:   2018-1-19 15:27:56
 * @since:  v4.4
 */
public class ServerInfo {
	private String addr;

	private int port;

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String toString() {
		return addr + ":" + port;
	}

}
