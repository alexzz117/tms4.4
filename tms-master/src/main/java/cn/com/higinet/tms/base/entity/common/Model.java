/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.base.entity.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ui.ModelMap;
import cn.com.higinet.tms.manager.dao.Page;

/**
 * 
 * 前端交互使用的模型对象
 * @author chenr
 * @version 2.0.0, 2011-6-23
 * 
 */
public class Model extends ModelMap {

	private static final long serialVersionUID = 1L;

	/**
	 * 页面模型，提供给Grid组件使用
	 */
	public static final String PAGE = "page";

	/**
	 * 业务错误
	 */
	public static final String ERROR = "error";

	/**
	 * 处理成功
	 */
	public static final String SUCCESS = "success";

	/**
	 * 单行数据记录
	 */
	public static final String ROW = "row";

	/**
	 * 列表数据记录
	 */
	public static final String LIST = "list";

	public Model() {
		set( SUCCESS, true );
	}

	/**
	 * 设置分页模型数据
	 * @param <T>
	 * @param page
	 */
	public <T> Model setPage( Page<T> page ) {
		set( PAGE, page );
		return this;
	}

	/**
	 * 设置列表模型数据
	 * @param <T>
	 * @param list
	 */
	public <T> Model setList( List<T> list ) {
		set( LIST, list );
		return this;
	}

	/**
	 * 设置单行模型数据
	 * @param <T>
	 * @param row
	 */
	public <T> Model setRow( T row ) {
		set( ROW, row );
		return this;
	}

	/**
	 * 添加一个错误信息
	 * @param message
	 */
	@SuppressWarnings({
			"unchecked", "rawtypes"
	})
	public Model addError( String message ) {
		if( this.containsKey( ERROR ) ) {
			List list = (List) this.get( ERROR );
			list.add( message );
		}
		else {
			List list = new ArrayList();
			list.add( message );
			set( ERROR, list );
		}
		set( SUCCESS, false );
		return this;
	}

	/**
	 * 模型中是否有某属性值
	 * @param attributeName
	 * @return
	 */
	public boolean has( String attributeName ) {
		return this.containsKey( attributeName );
	}

	/**
	 * 设置属性
	 * @param attributeName
	 * @param o
	 */
	public Model set( String attributeName, Object o ) {
		this.put( attributeName, o );
		return this;
	}

	/**
	 * 从模型中取出某属性的值
	 * @param attributeName
	 * @return
	 */
	public Object get( String attributeName ) {
		return this.get( attributeName );
	}

	/**
	 * 返回一个空的模型对象，不可设值
	 * @return
	 */
	public static Model emptyModel() {
		//TODO 需要返回一个空的对象，该对象不可修改
		//参考 Conllections.emptyMap();
		return new Model();
	}
}
