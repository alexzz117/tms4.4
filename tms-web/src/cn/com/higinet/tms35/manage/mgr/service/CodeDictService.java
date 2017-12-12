package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

public interface CodeDictService {
	
	/**
	 * 查询代码
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listCode(Map<String, String> conds);
	/**
	 * 根据类别(支持多个)查询代码集
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listCodes(Map<String, String> conds) ;
	/**
	 * 根据类别查询代码分类集
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>> listCodeCateGorp(Map<String, String> conds);
}
