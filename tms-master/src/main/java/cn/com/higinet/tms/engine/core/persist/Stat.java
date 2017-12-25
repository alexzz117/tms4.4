/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Stat.java   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-21 14:12:13   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.engine.core.persist;

import java.util.List;

import cn.com.higinet.tms.engine.stat.stat_row;

/**
 * 统计值的持久化接口
 *
 * @ClassName:  Stat
 * @author: 王兴
 * @date:   2017-8-21 14:12:13
 * @since:  v4.3
 */
public interface Stat {

	/**
	 * 根据list中的row的persistenceId，获取对应的统计值
	 *
	 * @param rows the rows
	 * @return the list
	 * @throws Exception the exception
	 */
	public List<stat_row> queryList(List<stat_row> rows) throws Exception;

	/**
	 * 保存一批统计值
	 *
	 * @param rows the rows
	 * @throws Exception the exception
	 */
	public void saveList(List<stat_row> rows) throws Exception;

	/**
	 * 备份一批统计值.
	 *
	 * @param rows the rows
	 * @throws Exception the exception
	 */
	public void backup(List<stat_row> rows) throws Exception;

	/**
	 * 直接删除一个统计.
	 *
	 * @param row the row
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean delete(stat_row row) throws Exception;
}
