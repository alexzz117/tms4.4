package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

/**
 * 查询子条件组成员
 * @author liining
 *
 */
public class Member {
	/**
	 * 数据字段名
	 */
	private String fdName;
	/**
	 * 生成查询语句时的字段名
	 */
	private String csName;
	/**
	 * 页面元素名称
	 */
	private String name;
	/**
	 * 页面元素提示
	 */
	private String prompt;
	/**
     * 当前查询条件的类型
     * scp:区间;gt:大于;lt:小于;eq:等于
     * noteq:不等于;gteq:大于等于;lteq:小于等于;cbt:组合
     * in:包含;notin:不包含;like:类似;notlike:不类似
     */
	private Member.Type type;
	/**
	 * 前台jcl设置，覆盖自动组织的
	 */
	private String item;
	/**
	 * 处理
	 */
	private Handle handle;
	/**
	 * 默认值
	 */
	private DefValue defValue;

	public String getFdName() {
		return fdName;
	}

	public void setFdName(String fdName) {
		this.fdName = fdName;
	}

	public String getCsName() {
		return csName;
	}

	public void setCsName(String csName) {
		this.csName = csName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Member.Type getType() {
		return type;
	}

	public void setType(String type) {
		String _node = this.getClass().getSimpleName();
		Member.Type _type = Member.Type.fromValue(type);
		if(_type == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[cond]->[group]"
					+(_node.equalsIgnoreCase("member") ? "->[member]":"")+"节点下的[type]设置不正确或为空");
		}
		this.type = _type;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Handle getHandle() {
		return handle;
	}

	public void setHandle(Handle handle) {
		this.handle = handle;
	}

	public DefValue getDefValue() {
		return defValue;
	}

	public void setDefValue(DefValue defValue) {
		this.defValue = defValue;
	}
	
	public static enum Type {
		SCP("scp", "区间", ""), GT("gt", "大于", " > ?"), LT("lt", "小于", " < ?"), EQ("eq", "等于", " = ?"), NOTEQ("noteq", "不等于", " <> ?"), GTEQ("gteq", "大于等于", " >= ?"), LTEQ("lteq", "小于等于"," <= ?"), 
        IN("in", "包含", " in (?)"), NOTIN("notin", "不包含", " not in (?)"), LIKE("like", "类似", " like '%?%'"), NOTLIKE("notlike", "不类似", " not like '%?%'"), CBT("cbt", "组合", "");
        private final String value;
        private final String text;
        private final String operator;
        private static Map<String, Member.Type> constants = new HashMap<String, Member.Type>();

        static {
            for (Member.Type c: Member.Type.values()) {
                constants.put(c.value, c);
            }
        }

        private Type(String value, String text, String operator) {
            this.value = value;
            this.text = text;
            this.operator = operator;
        }

        public String getValue() {
            return this.value;
        }
        
        public String getText() {
        	return this.text;
        }
        
        public String getOperator(){
        	return this.operator;
        }

        public static Member.Type fromValue(String value) {
        	Member.Type constant = constants.get(value);
            return constant;
        }
    }
}
