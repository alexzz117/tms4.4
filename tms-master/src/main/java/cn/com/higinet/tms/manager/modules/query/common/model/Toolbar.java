package cn.com.higinet.tms.manager.modules.query.common.model;

import cn.com.higinet.tms.manager.common.util.CmcStringUtil;
import cn.com.higinet.tms.manager.modules.exception.TmsMgrWebException;

public class Toolbar {
	public String id;
	
	public String icon;
	
	public String text;
	
	public String enable;
	
	public String action;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		/*if(StringUtil.isBlank(id)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[toolbar]节点下的[id]不能为空");
		}*/
		this.id = id;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if(CmcStringUtil.isBlank(text)){
			throw new TmsMgrWebException("自定义查询JSON中[custom]->[toolbar]节点下的[text]不能为空");
		}
		this.text = text;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
