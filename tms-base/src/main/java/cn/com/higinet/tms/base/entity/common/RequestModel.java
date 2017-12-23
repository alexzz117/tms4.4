package cn.com.higinet.tms.base.entity.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

import cn.com.higinet.tms.base.util.Mapz;
import cn.com.higinet.tms.base.util.Stringz;

public class RequestModel extends ModelMap {
	private static final long serialVersionUID = 1L;

	private Integer pageindex = 1;
	private Integer pagesize = 25;

	/**
	 * 将Map转换为实体
	 * */
	public <S> S getEntity( Class<S> beanClass ) {
		try {
			return (S) Mapz.toEntity( this, beanClass );
		}
		catch( Exception e ) {
			e.printStackTrace();
			try {
				return beanClass.newInstance();
			}
			catch( Exception e1 ) {
				e1.printStackTrace();
				return null;
			}
		}
	}

	public PageRequest getPageRequest( Order... order ) {
		if( super.get( "pageindex" ) != null && Stringz.isNotEmpty( super.get( "pageindex" ).toString() ) ) {
			pageindex = Integer.valueOf( super.get( "pageindex" ).toString() );
		}
		if( super.get( "pagesize" ) != null && Stringz.isNotEmpty( super.get( "pagesize" ).toString() ) ) {
			pagesize = Integer.valueOf( super.get( "pagesize" ).toString() );
		}

		Sort sort = new Sort( order );
		PageRequest pageRequest = new PageRequest( pageindex - 1, pagesize, sort );
		return pageRequest;
	}

	public PageRequest getPageRequest() {
		if( super.get( "pageindex" ) != null && !Stringz.isNotEmpty( super.get( "pageindex" ).toString() ) ) {
			pageindex = Integer.valueOf( super.get( "pageindex" ).toString() );
		}
		if( super.get( "pagesize" ) != null && Stringz.isNotEmpty( super.get( "pagesize" ).toString() ) ) {
			pagesize = Integer.valueOf( super.get( "pagesize" ).toString() );
		}

		PageRequest pageRequest = new PageRequest( pageindex - 1, pagesize );
		return pageRequest;
	}

	public Object getPagePageHelper() {
		if( super.get( "pageindex" ) != null && Stringz.isNotEmpty( super.get( "pageindex" ).toString() ) ) {
			pageindex = Integer.valueOf( super.get( "pageindex" ).toString() );
		}
		if( super.get( "pagesize" ) != null && Stringz.isNotEmpty( super.get( "pagesize" ).toString() ) ) {
			pagesize = Integer.valueOf( super.get( "pagesize" ).toString() );
		}

		Map<String, Object> pageMap = new HashMap<String, Object>();
		pageMap.put( "pageNum", pageindex );
		pageMap.put( "pageSize", pagesize );

		return pageMap;
	}

	public String getString( String key ) {
		if( this.isEmpty() || !this.containsKey( key ) || this.get( key ) == null ) return null;
		return this.get( key ).toString();
	}

	public Object getObject( String key ) {
		if( this.isEmpty() || !this.containsKey( key ) || this.get( key ) == null ) return null;
		return this.get( key );
	}

	public Float getFloat( String key ) {
		if( this == null || this.isEmpty() || !this.containsKey( key ) || this.get( key ) == null ) return null;
		String val = String.valueOf( this.get( key ) );
		if( val.length() == 0 ) return null;
		return Float.parseFloat( val );
	}

	public Integer getInteger( String key ) {
		if( this == null || this.isEmpty() ) return null;
		if( !this.containsKey( key ) ) return null;
		if( this.get( key ) == null ) return null;
		return Integer.parseInt( this.get( key ).toString() );
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMap( String key ) {
		if( this.isEmpty() || Stringz.isEmpty( key ) || !this.containsKey( key ) || this.get( key ) == null ) return null;
		return (Map<String, Object>) this.get( key );
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getList( String key ) {
		if( this == null || this.isEmpty() || StringUtils.isEmpty( key ) ) return null;
		if( !this.containsKey( key ) ) return null;
		if( this.get( key ) == null ) return null;

		return (List<Map<String, Object>>) this.get( key );
	}

	public boolean getBoolean( String key ) {
		if( this == null || this.isEmpty() || Stringz.isEmpty( key ) ) return false;
		if( !this.containsKey( key ) ) return false;
		if( this.get( key ) == null ) return false;
		return (Boolean) this.get( key );
	}

	/**
	 * 验证指定属性
	 * */
	/*public Model isValid( String... keys ) {
		Model model = new Model();
		for( String key : keys ) {
			if( this.get( key ) == null ) {
				model.addError( key + " is null" );
			}
			else if( this.get( key ) instanceof String ) {
				if( Stringz.isEmpty( this.get( key ) ) ) {
					model.addError( key + " is empty" );
				}
			}
		}
		return model;
	}*/
}
