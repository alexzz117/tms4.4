package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

/**
 * 数据字段
 * @author liining
 *
 */
public class Field {
	 /**
     * 字段名
     * 
     */
    private String fdName;
    /**
     * 动态获取字段信息{可多条}
     */
    private String dynamic;
    /**
     * 字段中文名
     * 
     */
    private String name;
    /**
     * 字段描述
     * 
     */
    private String fdDesc;
    /**
     * 字段对应TMS类型
     * 默认为string
     */
    private Field.Type type = Field.Type.STRING;
    /**
     * 对应的数据库字段类型
     * 
     */
    private String dbType;
    /**
     * 对应的数据库字段长度
     * 
     */
    private Integer dbLen;
    /**
     * 引用代码集
     * 
     */
    private String code;
    /**
     * 显示排序
     * 
     */
    private Integer orderby;
    /**
     * 定义链接
     * 
     */
    private String link;
    
	public String getFdName() {
		return fdName;
	}
	public void setFdName(String fdName) {
		if(CmcStringUtil.isBlank(fdName)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[dbData]节点下所有[fields]节点中的[fdName]不能为空");
		}
		this.fdName = fdName;
	}
	public String getDynamic() {
		return dynamic;
	}
	public void setDynamic(String dynamic) {
		this.dynamic = dynamic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFdDesc() {
		return fdDesc;
	}
	public void setFdDesc(String fdDesc) {
		this.fdDesc = fdDesc;
	}
	public Field.Type getType() {
		return type;
	}
	public void setType(String type) {
		this.type = Field.Type.fromValue(type);
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public Integer getDbLen() {
		return dbLen;
	}
	public void setDbLen(Integer dbLen) {
		this.dbLen = dbLen;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Integer getOrderby() {
		return orderby;
	}
	public void setOrderby(Integer orderby) {
		this.orderby = orderby;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	public static enum Type {
		LONG("long", "整数类型"), DOUBLE("double", "小数类型"), STRING("string", "字符类型"), MONEY("money", "货币类型"), 
		TIME("time", "时间类型"), DATETIME("datetime", "日期时间类型"), DEVID("devid", "设备标识"), IP("ip", "IP地址"), 
		USERID("userid", "用户标识"), ACC("acc", "账号标识"), CODE("code", "代码类型");
		
		private final String value;
		private final String text;
        private static Map<String, Field.Type> constants = new HashMap<String, Field.Type>();

        static {
            for (Field.Type c: Field.Type.values()) {
                constants.put(c.value, c);
            }
        }

        private Type(String value, String text){
			this.value = value;
			this.text = text;
		}

        public String getValue(){
        	return this.value;
        }
        
        public String getText(){
        	return this.text;
        }

        public static Field.Type fromValue(String value) {
        	Field.Type constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
        	return constant;
        }
	}
}
