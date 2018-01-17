/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  KryoSerializer.java   
 * @Package cn.com.higinet.tms.event.modules.kafka.serializer   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2018-1-17 11:34:33   
 * @version V1.0 
 * @Copyright: 2018 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.event.modules.kafka.serializer;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import cn.com.higinet.tms.common.util.ByteUtils;

/**
 * 支持krryo的kafka序列化器
 *
 * @ClassName:  KryoSerializer
 * @author: 王兴
 * @date:   2018-1-17 11:34:33
 * @since:  v4.4
 */
public class KryoSerializer implements Serializer<Object> {

	/* (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#configure(java.util.Map, boolean)
	 */
	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	/* (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#serialize(java.lang.String, java.lang.Object)
	 */
	@Override
	public byte[] serialize(String topic, Object data) {
		try {
			return ByteUtils.object2KryoBytes(data, true);
		} catch (Exception e) {
			throw new SerializationException("Error when KryoSerializer serializing Object to byte[].");
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.kafka.common.serialization.Serializer#close()
	 */
	@Override
	public void close() {
	}

}
