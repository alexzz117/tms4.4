/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Sortable.java   
 * @Package cn.com.higinet.tms.common.lifecycle   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-7 19:37:34   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.lifecycle;

/**
 * 提供一个优先级的接口，后面可以通过这个接口来进行排序。
 *
 * @author: 王兴
 * @date:   2017-5-7 19:37:34
 * @since:  v4.3
 */
public interface Sortable {
	
	/**
	 * 返回 priority 属性，优先级值越小，越优先，0是最优先的，负数也当0.
	 *
	 * @return priority 属性
	 */
	public int getPriority();
}
