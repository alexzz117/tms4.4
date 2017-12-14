/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  AbstractRDBMSPersistence.java   
 * @Package cn.com.higinet.tms.common.repository.persistence   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-22 14:23:30   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import cn.com.higinet.tms.common.exception.UnsupportedParameterException;
import cn.com.higinet.tms.common.repository.Attribute;
import cn.com.higinet.tms.common.repository.Metadata;
import cn.com.higinet.tms.common.repository.StructuredData;

/**
 * 关系型数据的抽象基类，目前此类中可以把structure对象转换成sql和参数集合，具体的sql执行还得看使用者自己
 *
 * @ClassName:  AbstractRDBMSPersistence
 * @author: 王兴
 * @date:   2017-9-22 14:23:32
 * @since:  v4.3
 */
public abstract class AbstractSimpleRDBMSPersistence implements Persistence {
	@Override
	public StructuredData get(StructuredData structure) throws Exception {
		Metadata meta = structure.getMetadata();
		Attribute k = meta.getPrimaryAttribute();
		String sql = new StringBuilder("select * from ").append(meta.getStructureName()).append(" where ").append(k.getPersistenceName()).append("= ?").toString();
		List<Object> params = new ArrayList<Object>();
		params.add(structure.getValueForPersist(k.getName()));
		Map<String, String> res = executeQuery(sql, params, new int[] { k.getPersistedType() });
		if (res == null || res.isEmpty()) {
			return null;
		}
		structure.setData(res);
		return structure;
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.persistence.Persistence#insert(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public void insert(StructuredData structure) throws Exception {
		Metadata meta = structure.getMetadata();
		List<Attribute> attrs = meta.listAttributes();
		int size = attrs.size();
		List<Object> params = new ArrayList<Object>(size);
		int[] types = new int[size];
		String[] cloumns = new String[size];
		String[] ends = new String[size];
		//这块由于没有做严格判断，因此还是走安全的方式
		for (int i = 0, len = attrs.size(); i < len; i++) {
			Attribute attr = attrs.get(i);
			if (!attr.isPersistable()) {//不参与持久化
				continue;
			}
			String field = attr.getPersistenceName();
			cloumns[i] = field;
			ends[i] = "?";
			Object value = structure.getValueForPersist(field);
			if (value == null && !attr.isNullable()) {//非空字段为空
				throw new UnsupportedParameterException("Attribue \"%s\" for structure \"\" can not be empty.", field, meta.getStructureName());
			}
			params.add(value);
			types[i] = attr.getPersistedType();
		}
		StringBuilder sql = new StringBuilder("insert into ").append(meta.getStructureName()).append(" (").append(StringUtils.join(cloumns, ",")).append(")").append(" values (").append(StringUtils.join(ends, ",")).append(")");
		executeSql(sql.toString(), params, types);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.persistence.Persistence#update(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public void update(StructuredData structure) throws Exception {
		Metadata meta = structure.getMetadata();
		Map<String, String> values = structure.getData();//这个是初始化过后的值，有的才更新，没有的字段不更新
		int size = values.size();
		List<Object> params = new ArrayList<Object>(size + 1);//+1是因为条件中有key参数
		int[] types = new int[size + 1];//+1是因为条件中有key参数
		String[] cloumns = new String[size];
		String[] ends = new String[size];
		int i = 0;
		//这块由于没有做严格判断，因此还是走安全的方式
		Set<String> attrs = values.keySet();
		for (String _attr : attrs) {
			Attribute attr = meta.getAttribute(_attr);
			if (attr == null || !attr.isPersistable()) {
				continue;
			}
			String field = attr.getPersistenceName();
			cloumns[i] = field;
			ends[i] = "?";
			Object value = structure.getValueForPersist(field);
			if (value == null && !attr.isNullable()) {//非空字段为空
				throw new UnsupportedParameterException("Attribue \"%s\" for structure \"\" can not be empty.", field, meta.getStructureName());
			}
			params.add(value);
			types[i++] = attr.getPersistedType();
		}
		Attribute primary = meta.getPrimaryAttribute();
		String primaryField = primary.getPersistenceName();
		StringBuilder sql = new StringBuilder("update ").append(meta.getStructureName()).append(" set ").append(StringUtils.join(cloumns, "=?,")).append("=? where ").append(primaryField).append("=?");
		params.add(structure.getPrimaryValueForPersist());
		types[i] = primary.getPersistedType();
		executeSql(sql.toString(), params, types);
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.repository.persistence.Persistence#delete(cn.com.higinet.tms.common.repository.StructuredData)
	 */
	@Override
	public void delete(StructuredData structure) throws Exception {
		Metadata meta = structure.getMetadata();
		Attribute primary = meta.getPrimaryAttribute();
		List<Object> params = new ArrayList<Object>(1);
		int[] types = new int[1];
		int i = 0;
		String field = primary.getPersistenceName();
		//这块由于没有做严格判断，因此还是走安全的方式
		StringBuilder sql = new StringBuilder("delete from ").append(meta.getStructureName()).append(" where ").append(field).append("=?");
		params.add(structure.getValueForPersist(field));
		types[0] = primary.getPersistedType();
		executeSql(sql.toString(), params, types);
	}

	/**
	 * 具体执行sql，由子类自己去实现
	 *
	 * @param sql the sql
	 * @param params the params
	 * @param paramTypes the param types
	 * @return the int
	 * @throws Exception the exception
	 */
	protected abstract int executeSql(String sql, List<Object> params, int[] paramTypes) throws Exception;

	/**
	 * 具体执行sql，由子类自己去实现
	 *
	 * @param sql the sql
	 * @param params the params
	 * @param paramTypes the param types
	 * @return the int
	 * @throws Exception the exception
	 */
	protected abstract Map<String, String> executeQuery(String sql, List<Object> params, int[] paramTypes) throws Exception;

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
	}

	/* (non-Javadoc)
	 * @see cn.com.higinet.tms.common.lang.Initializable#destroy()
	 */
	@Override
	public void destroy() throws Exception {
	}

}
