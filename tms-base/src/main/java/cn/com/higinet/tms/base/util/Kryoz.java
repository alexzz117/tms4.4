/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ByteUtils.java   
 * @Package com.ydtf.bdp.base.utils   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-5-7 16:02:33   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */

package cn.com.higinet.tms.base.util;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * Byte工具类.
 *
 * @author 王兴
 * @since v1.0
 * @date Aug 7, 2015
 */
public class Kryoz {

	/** 常量 KRYOPOOL. */
	public static final KryoPool KRYOPOOL = new KryoPool.Builder( new KryoFactory() {
		public Kryo create() {
			Kryo kryo = new Kryo();
			//kryo.setReferences(false); //对于list和map对象来说，这个会提高很多效率
			kryo.setRegistrationRequired( false );
			return kryo;
		}
	} ).build();

	/**
	 * 将对象序列化成kryo的byte数组
	 *
	 * @param obj the obj
	 * @return the byte[]
	 */
	public static byte[] object2KryoBytes( Object obj ) {
		return object2KryoBytes( obj, false );
	}

	/**
	 * 将对象序列化成kryo的byte数组
	 *
	 * @param obj the obj
	 * @param reference the reference
	 * @return the byte[]
	 */
	public static byte[] object2KryoBytes( Object obj, boolean reference ) {
		Kryo kryo = KRYOPOOL.borrow();
		kryo.setReferences( reference );
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			Output _output = new Output( bo );
			kryo.writeObject( _output, obj );
			return _output.toBytes();
		}
		finally {
			KRYOPOOL.release( kryo );
		}
	}

	/**
	 *  将kryo序列化后的byte数组范序列化成指定class的对象
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param data the data
	 * @return the t
	 */
	public static <T> T KryoBytes2Object( Class<T> type, byte[] data ) {
		return KryoBytes2Object( type, data, false );
	}

	/**
	 * 将kryo序列化后的byte数组范序列化成指定class的对象
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @param data the data
	 * @param reference the reference
	 * @return the t
	 */
	public static <T> T KryoBytes2Object( Class<T> type, byte[] data, boolean reference ) {
		Kryo kryo = KRYOPOOL.borrow();
		kryo.setReferences( reference );
		try {
			Input input = new Input( data );
			return kryo.readObject( input, type );
		}
		finally {
			KRYOPOOL.release( kryo );
		}
	}
}
