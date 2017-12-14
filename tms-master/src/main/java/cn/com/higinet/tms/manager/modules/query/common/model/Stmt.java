package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

/**
 * 自定义查询语句
 * @author liining
 *
 */
public class Stmt {
	 /**
     * 语句产生方式
     * auto:自动生成
     * manu:手动生成
     * 
     */
    private Stmt.Mode mode;
    /**
     * 语句类型
     * sql:普通sql语句
     * sp:存储过程查询
     * ctm:自定义
     * 
     */
    private Stmt.Type type;
    /**
     * 执行SQL语句的数据源名称
     */
    private String dsName;
    /**
     * 语句内容
     * 
     */
    private String content;

    /**
     * 语句产生方式
     * auto:自动生成
     * manu:手动生成
     * 
     */
    public Stmt.Mode getMode() {
        return mode;
    }

    /**
     * 语句产生方式
     * auto:自动生成
     * manu:手动生成
     * 
     */
    public void setMode(String mode) {
        this.mode = Stmt.Mode.fromValue(mode);
        if(this.mode == null){
        	throw new TmsMgrWebException("自定义查询JSON中[custom]->[stmt]->[mode]节点，设置错误或为空");
        }
    }
    /**
     * 语句类型
     * sql:普通sql语句
     * sp:存储过程查询
     * ctm:自定义
     * 
     */
    public Stmt.Type getType() {
        return type;
    }

    /**
     * 语句类型
     * sql:普通sql语句
     * sp:存储过程查询
     * ctm:自定义
     * 
     */    
    public void setType(String type) {
        this.type = Stmt.Type.fromValue(type);
        if(this.type == null){
        	throw new TmsMgrWebException("自定义查询JSON中[custom]->[stmt]->[type]节点，设置错误或为空");
        }
    }

    public String getDsName() {
		return dsName;
	}

	public void setDsName(String dsName) {
		this.dsName = dsName;
	}

	/**
     * 语句内容
     * 
     */
    public String getContent() {
        return content;
    }

    /**
     * 语句内容
     * 
     */
    public void setContent(String content) {
    	if(CmcStringUtil.isBlank(content)){
    		throw new TmsMgrWebException("自定义查询JSON中[custom]->[stmt]->[content]节点不能为空");
    	}
        this.content = content;
    }
    
    public static enum Mode {
        AUTO("auto", "自动生成"), MANU("manu", "手动生成");
        private final String value;
        private final String text;
        private static Map<String, Stmt.Mode> constants = new HashMap<String, Stmt.Mode>();

        static {
            for (Stmt.Mode c: Stmt.Mode.values()) {
                constants.put(c.value, c);
            }
        }

        private Mode(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return this.value;
        }
        
        public String getText() {
            return this.text;
        }

        public static Stmt.Mode fromValue(String value) {
            Stmt.Mode constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
            return constant;
        }

    }

    public static enum Type {
        SQL("sql", "普通SQL语句"), SP("sp", "存储过程"), ACT("act", "功能类"), CTM("ctm", "自定义的");
        private final String value;
        private final String text;
        private static Map<String, Stmt.Type> constants = new HashMap<String, Stmt.Type>();

        static {
            for (Stmt.Type c: Stmt.Type.values()) {
                constants.put(c.value, c);
            }
        }

        private Type(String value, String text) {
            this.value = value;
            this.text = text;
        }

        public String getValue() {
            return this.value;
        }
        
        public String getText() {
            return this.text;
        }

        public static Stmt.Type fromValue(String value) {
            Stmt.Type constant = constants.get(value);
            /*if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }*/
            return constant;
        }

    }
}
