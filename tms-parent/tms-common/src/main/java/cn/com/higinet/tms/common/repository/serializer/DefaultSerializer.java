/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  DefaultSerializer.java   
 * @Package cn.com.higinet.tms.common.repository.serializer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-21 17:34:37   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.serializer;

import cn.com.higinet.tms.common.repository.StructuredData;
import cn.com.higinet.tms.common.util.ByteUtils;

/**
 * 系统默认序列化器，采用kryo序列化
 *
 * @ClassName:  DefaultSerializer
 * @author: 王兴
 * @date:   2017-9-21 17:34:37
 * @since:  v4.3
 */
public class DefaultSerializer implements Serializer {

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.serializer.Serializer#structureToBytes(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public byte[] structureToBytes(StructuredData structure) throws Exception {
		return ByteUtils.object2KryoBytes(structure);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.serializer.Serializer#bytesToStructure(byte[])
	 */
	@Override
	public StructuredData bytesToStructure(byte[] data) throws Exception {
		return ByteUtils.KryoBytes2Object(StructuredData.class, data);
	}

}
