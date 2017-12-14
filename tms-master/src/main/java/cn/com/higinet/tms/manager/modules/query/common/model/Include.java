package cn.com.higinet.tms.manager.modules.query.common.model;

import java.util.HashMap;
import java.util.Map;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

public class Include {

	/**
	 * 引入类型
	 * file:文件
	 * code:代码
	 */
	private Include.Type type;
	
	/**
	 * 源文件或源代码
	 */
	private String source;

	public Include.Type getType() {
		return type;
	}

	public void setType(String type) {
		this.type = Include.Type.fromValue(type);
		if(this.type == null){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[script]->[includes]节点下的[type]不能为空");
		}
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		if(CmcStringUtil.isBlank(source)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[script]->[includes]节点下的[source]不能为空");
		}
		this.source = source;
	}
	
	public static enum Type {
		FILE("file", "文件"), CODE("code", "代码");

		private final String value;
		private final String text;
		private static Map<String, Include.Type> constants = new HashMap<String, Include.Type>();

		static {
			for (Include.Type c : Include.Type.values()) {
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

		public static Include.Type fromValue(String value) {
			Include.Type constant = constants.get(value);
			return constant;
		}
	}

}
