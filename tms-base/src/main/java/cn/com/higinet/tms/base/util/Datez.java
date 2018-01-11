package cn.com.higinet.tms.base.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Datez {

	/**
	 * 返回XX天后的时间字符串
	 * 
	 * @param date 时间字符串
	 * @param format 输入与输出时间格式
	 * @param day 增加的天数
	 * 
	 * */
	public static String addDay( String date, String format, int day ) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		Date _date = sdf.parse( date );
		Calendar c = Calendar.getInstance();
		c.setTime( _date );
		c.add( Calendar.DATE, day );
		return sdf.format( c.getTime() );
	}

	public static String getDateTime( String format, long time ) {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		return sdf.format( time );
	}

	public static long getDateTime( String format, String date ) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		try {
			Date _date = sdf.parse( date );
			return _date.getTime();
		}
		catch( ParseException e ) {
			throw new Exception( e );
		}
	}

	public static String getTimestampString( Date date ) {
		if( date == null ) return null;
		return String.valueOf( date.getTime() );
	}

	public static void formatTimestampForEntityList( List<?> list, String... fieldNames ) {
		formatTimestampForEntityList( "yyyy-MM-dd HH:mm:ss", list, fieldNames );
	}

	public static void formatTimestampForEntityList( String format, List<?> list, String... fieldNames ) {
		if( list == null ) return;
		for( Object entity : list ) {
			formatTimestampForEntity( format, entity, fieldNames );
		}
	}

	public static void formatTimestampForEntity( Object entity, String... fieldNames ) {
		formatTimestampForEntity( "yyyy-MM-dd HH:mm:ss", entity, fieldNames );
	}

	public static void formatTimestampForEntity( String format, Object entity, String... fieldNames ) {
		if( entity == null || Stringz.isEmpty( format ) ) return;

		for( Field field : entity.getClass().getDeclaredFields() ) {
			int mod = field.getModifiers();
			if( Modifier.isStatic( mod ) || Modifier.isFinal( mod ) ) {
				continue;
			}

			for( String fieldName : fieldNames ) {
				if( field.getName().toLowerCase().equals( fieldName.toLowerCase() ) ) {
					try {
						field.setAccessible( true );
						if( field.get( entity ) == null ) continue;
						String value = field.get( entity ).toString();
						if( !Stringz.isNumeric( value ) ) continue;
						long date = Long.parseLong( value );
						field.set( entity, getDateTime( format, date ) );
					}
					catch( Exception e ) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	public static void formatTimestampForMap( Map<String, Object> map, String... fieldNames ) {
		formatTimestampForMap( "yyyy-MM-dd HH:mm:ss", map, fieldNames );
	}

	public static void formatTimestampForMapList( List<?> list, String... fieldNames ) {
		formatTimestampForMapList( "yyyy-MM-dd HH:mm:ss", list, fieldNames );
	}

	@SuppressWarnings("unchecked")
	public static void formatTimestampForMapList( String format, List<?> list, String... fieldNames ) {
		if( list == null ) return;
		for( Object obj : list ) {
			formatTimestampForMap( format, (Map<String, Object>) obj, fieldNames );
		}
	}

	public static void formatTimestampForMap( String format, Map<String, Object> map, String... fieldNames ) {
		if( map == null || Stringz.isEmpty( format ) ) return;

		for( Entry<String, Object> entry : map.entrySet() ) {
			for( String fieldName : fieldNames ) {
				try {
					if( Stringz.isEmpty( fieldName ) || Stringz.isEmpty( entry.getKey() ) ) continue;

					if( entry.getKey().toLowerCase().equals( fieldName.toLowerCase() ) ) {
						if( entry.getValue() == null || Stringz.isEmpty( entry.getValue().toString() ) ) continue;

						long date = Long.valueOf( entry.getValue().toString() );
						map.put( entry.getKey(), getDateTime( format, date ) );
					}
				}
				catch( Exception e ) {
					e.printStackTrace();
				}

			}
		}
	}
}
