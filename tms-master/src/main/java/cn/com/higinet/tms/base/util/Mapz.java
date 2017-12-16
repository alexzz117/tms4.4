package cn.com.higinet.tms.base.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;

@SuppressWarnings({
		"unchecked", "rawtypes"
})
public class Mapz {

	public static boolean objectIsEmpty( Map map, String key ) {
		if( map.isEmpty() ) return true;
		if( !map.containsKey( key ) ) return true;
		if( map.get( key ) == null ) return true;
		return false;
	}

	public static String getString( Map map, String key ) {
		if( map.isEmpty() ) return "";
		if( !map.containsKey( key ) ) return "";
		if( map.get( key ) == null ) return "";
		return map.get( key ).toString();
	}

	public static Object getObject( Map map, String key ) {
		if( map.isEmpty() ) return null;
		if( !map.containsKey( key ) ) return null;
		if( map.get( key ) == null ) return null;
		return map.get( key );
	}

	public static float getFloat( Map map, String key ) {
		if( map == null || map.isEmpty() ) return 0;
		if( !map.containsKey( key ) ) return 0;
		if( map.get( key ) == null ) return 0;
		String val = String.valueOf( map.get( key ) );
		if( val.length() == 0 ) return 0;
		return Float.parseFloat( val );
	}

	public static int getInteger( Map map, String key ) {
		if( map == null || map.isEmpty() ) return 0;
		if( !map.containsKey( key ) ) return 0;
		if( map.get( key ) == null ) return 0;
		return Integer.parseInt( map.get( key ).toString() );
	}

	public static Map<String, Object> getMap( Map map, String key ) {
		if( map == null || map.isEmpty() || Stringz.isEmpty( key ) ) return null;
		if( !map.containsKey( key ) ) return null;
		if( map.get( key ) == null ) return null;

		return (Map<String, Object>) map.get( key );
	}

	public static List<Map<String, Object>> getList( Map map, String key ) {
		if( map == null || map.isEmpty() || Stringz.isEmpty( key ) ) return null;
		if( !map.containsKey( key ) ) return null;
		if( map.get( key ) == null ) return null;

		return (List<Map<String, Object>>) map.get( key );
	}

	public static boolean getBoolean( Map map, String key ) {
		if( map == null || map.isEmpty() || Stringz.isEmpty( key ) ) return false;
		if( !map.containsKey( key ) ) return false;
		if( map.get( key ) == null ) return false;
		return (Boolean) map.get( key );
	}

	public static <S> S toEntity( Map<String, Object> map, Class<S> beanClass ) throws Exception {
		if( map == null ) return null;

		Object entity = beanClass.newInstance();
		Field[] fields = entity.getClass().getDeclaredFields();
		for( Field field : fields ) {
			int mod = field.getModifiers();
			if( Modifier.isStatic( mod ) || Modifier.isFinal( mod ) ) continue;
			field.setAccessible( true );

			String fieldName = null;
			Column annotation = field.getAnnotation( Column.class );
			if( null != annotation && !Stringz.isEmpty( annotation.name() ) ) fieldName = annotation.name();
			else fieldName = field.getName();

			//大小写适配
			Object value = map.get( fieldName.toLowerCase() );
			if( value == null ) value = map.get( fieldName.toUpperCase() );

			//类型转换
			if( value != null ) {
				if( field.getType() == String.class ) {
					field.set( entity, Stringz.valueOf( value ) );
				}
				else if( field.getType() == Integer.class ) {
					field.set( entity, (Integer) value );
				}
				else if( field.getType() == Long.class ) {
					field.set( entity, Longz.valueOf( value ) );
				}
				else if( field.getType() == BigDecimal.class ) {
					field.set( entity, new BigDecimal( (String) value ) );
				}
				else {
					field.set( entity, value );
				}
			}
			else {
				field.set( entity, null );
			}

		}
		return (S) entity;
	}

}
