package cn.com.higinet.tms.manager.modules.query.common.model;

/**
 * 查询结果导出
 * @author lining
 */
public class Export {
	/**
	 * 导出按钮ID
	 */
	public String expBtn;
	/**
	 * 导出文件类型
	 */
	public String expType;
	/**
	 * 导出文件名
	 * 支持时间表达式
	 */
	public String expName;
	
	public String getExpBtn() {
		return expBtn;
	}
	public void setExpBtn(String expBtn) {
		this.expBtn = expBtn;
	}
	public String getExpType() {
		return expType;
	}
	public void setExpType(String expType) {
		this.expType = expType;
	}
	public String getExpName() {
		return expName;
	}
	public void setExpName(String expName) {
		this.expName = expName;
	}
}