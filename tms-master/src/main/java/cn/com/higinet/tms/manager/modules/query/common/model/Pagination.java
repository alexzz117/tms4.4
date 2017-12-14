package cn.com.higinet.tms.manager.modules.query.common.model;

/**
 * 分页设置
 * @author liining
 *
 */
public class Pagination {
	/**
     * 是否分页
     * 默认true
     */
    private Boolean pageable = true;
    /**
     * 是否显示分页工具条
     * pageable=true时，pagebar=true不可变
     * pageable=false时，可设置
     */
    private Boolean pagebar = true;
    /**
     * 分页条数
     * 默认使用系统参数
     */
    private Integer pagesize;
    
	public Boolean getPageable() {
		return pageable;
	}
	public void setPageable(Boolean pageable) {
		this.pageable = pageable;
	}
	public Boolean getPagebar() {
		return pagebar;
	}
	public void setPagebar(Boolean pagebar) {
		this.pagebar = pagebar;
	}
	public Integer getPagesize() {
		return pagesize;
	}
	public void setPagesize(Integer pagesize) {
		this.pagesize = pagesize;
	}
}
