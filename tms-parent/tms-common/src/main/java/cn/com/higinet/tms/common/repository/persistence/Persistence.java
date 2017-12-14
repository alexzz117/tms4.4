/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Persistence.java   
 * @Package cn.com.higinet.tms.common.repository.persistence   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:30:32   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.persistence;

import cn.com.higinet.tms.common.lang.Initializable;
import cn.com.higinet.tms.common.repository.Metadata;
import cn.com.higinet.tms.common.repository.StructuredData;

/**
 * 持久化接口
 *
 * @ClassName:  Persistence
 * @author: 王兴
 * @date:   2017-9-20 11:30:32
 * @since:  v4.3
 */
public interface Persistence extends Initializable {
	public StructuredData get(StructuredData structure) throws Exception;

	public void insert(StructuredData structure) throws Exception;

	public void update(StructuredData structure) throws Exception;

	public void delete(StructuredData structure) throws Exception;
}
