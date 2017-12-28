package cn.com.higinet.tms.base.entity.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * @author zhang.lei
 * 
 * */
public interface EntityBase<T> extends Serializable {

	/**
	 * 实体转JSON String
	 * */
	public default String toJSONString() {
		return JSON.toJSONString( this );
	}

	/**
	 * 实体转Map
	 * */
	public default Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		for( Field field : this.getClass().getDeclaredFields() ) {
			try {
				if( field.getModifiers() >= 16 ) continue;
				Column annotation = field.getAnnotation( Column.class );
				if( annotation == null ) continue;

				String key = field.getName();
				field.setAccessible( true );
				Object value = field.get( this );

				map.put( key, value );
			}
			catch( Exception e ) {
				e.printStackTrace();
				return null;
			}

		}
		return map;
	}

	/**
	 * 将实体内的空字符的String替换成null
	 * */
	@SuppressWarnings("unchecked")
	public default T removeEmpty() {
		try {
			for( Field field : this.getClass().getDeclaredFields() ) {
				if( field.getModifiers() >= 16 ) continue;
				field.setAccessible( true );
				if( field.getType() == String.class && StringUtils.isEmpty( field.get( this ) ) ) {
					field.set( this, null );
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return (T) this;
	}

	/**
	 * 合并实体
	 * */
	@SuppressWarnings("unchecked")
	public default T combine( T entity ) {
		T self = (T) this;

		try {
			for( Field field : self.getClass().getDeclaredFields() ) {
				if( field.getModifiers() >= 16 ) continue;
				field.setAccessible( true );
				if( field.get( self ) == null ) field.set( self, field.get( entity ) );
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}

		return self;
	}

	/**
	 * 克隆实体
	 * 注意，此方法只适合成员都为值类型的实体，如果存在引用类型，需要@Override
	 * */
	@SuppressWarnings("unchecked")
	public default T cloneEntity() {
		try {
			T self = (T) this;
			T copy = (T) this.getClass().newInstance();

			for( Field field : copy.getClass().getDeclaredFields() ) {
				if( field.getModifiers() >= 16 ) continue;
				field.setAccessible( true );
				field.set( copy, field.get( self ) );
			}
			return copy;

		}
		catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
}