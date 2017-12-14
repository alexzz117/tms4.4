/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  SystemUtils.java   
 * @Package cn.com.higinet.tms.common.util   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-11-9 13:57:44   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.util;

public class SystemUtils {
	
	/** 操作系统处理器数，看CPU有多少core，1个core多少线程数. */
	public static final int SYS_PROCESSORS = Runtime.getRuntime().availableProcessors();
}
