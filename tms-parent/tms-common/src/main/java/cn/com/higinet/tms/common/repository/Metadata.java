/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  Metadata.java   
 * @Package cn.com.higinet.tms.common.repository   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-9-20 11:26:34   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms.common.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结构化数据的元数据
 *
 * @ClassName:  Metadata
 * @author: 王兴
 * @date:   2017-9-20 11:26:34
 * @since:  v4.3
 */
public class Metadata {

	/** 持久化时候的表名，可以有多种用途，也可以表示一个集合名，根据持久化的方式不同而不同，起名为表名，方便大家理解. */
	private String structureName;

	/** 描述. */
	private String description;

	private Attribute primaryAttribute;

	private Map<String, Attribute> attributes;

	public Metadata() {
		attributes = new HashMap<String, Attribute>();
	}

	public String getStructureName() {
		return structureName;
	}

	public void setStructureName(String structureName) {
		this.structureName = structureName;
	}

	public Map<String, Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Attribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttributes(Attribute attr) {
		attributes.put(attr.getName(), attr);
	}

	public void addAttributes(String name, Attribute attr) {
		attributes.put(name, attr);
	}

	public Attribute getAttribute(String name) {
		return attributes.get(name);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Attribute> listAttributes() {
		return new ArrayList<Attribute>(attributes.values());
	}

	public Attribute getPrimaryAttribute() throws NonePrimaryException {
		if (primaryAttribute == null) {
			synchronized (Metadata.class) {
				if (primaryAttribute == null) {
					List<Attribute> attrs = listAttributes();
					for (int i = 0, len = attrs.size(); i < len; i++) {
						Attribute attr = attrs.get(i);
						if (attr.isPrimary()) {
							primaryAttribute = attr;
							break;
						}
					}
				}
			}
		}
		if (primaryAttribute == null) {
			throw new NonePrimaryException("Structure \"%s\" has no primary key definition.", structureName);
		}
		return primaryAttribute;
	}

}
