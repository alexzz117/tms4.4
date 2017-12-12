package cn.com.higinet.tms35.manage.mgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.rapid.base.dao.Page;

/**
 * 名单管理服务类
 * @author zlq
 * @author wangsch modified since 2013-06-04
 */
public interface NameListService {
	/**查询名单列表
	 * @param conds
	 * @return
	 */
	Page<Map<String, Object>> listNameList(Map<String, String> conds);
	
	/**
	 * 新增名单
	 * @param nameList
	 * @return
	 */
	Map<String, Object> createNameList(Map<String, String> nameList);
	
	/**
	 * 通过名单ID获取一条名单信息
	 * @param rosterId
	 * @return
	 */
	Map<String, Object> getOneNameList(String rosterId);
	/**
	 * 根据名单ID查询名单名称
	 * @param  rosterid  名单id	
	 * @return Map<String, Object>
	 */
	public Map<String, Object> selectById(String rosterId);
	
	public Map<String, Object> selectByIdOld(String rosterIdOld);
	
	public List<Map<String, Object>> selectByIds(String[] arrs);
	/**
	 * 根据rosterValueId查询名单值
	 * @param rosterValueId
	 * @return
	 */
	public Map<String, Object> selectByValueId(String rosterValueId);
	
	public List<Map<String, Object>> selectByValueIds(String[] arrs);
	
	/**
	 * 更新一条名单信息
	 * @param nameList
	 */
	void updateOneNameList(Map<String, String> nameList);
	
	/**
	 * 删除名单列表
	 * @param rosterIdMap
	 */
	void deleteNameList(Map<String,List<Map<String,String>>> batchMap);
	
	/**
	 * 新建名单值
	 * @param valueList
	 * @return
	 */
	public Map<String, Object> createValueList(Map<String, Object> valueList);
	
	/**
	 * 新建单个名单值
	 * @param map
	 * @return
	 */
	//public Map<String, Object> addRosterValue(Map<String, Object> map);
	
	/**
	 * 通过名单值表主键获取名单值信息
	 * @param rosterValueId
	 * @return
	 */
	public Map<String, Object> getOneValueList(String rosterValueId);
	
	/**
	 * 通过主键更新名单值
	 * @param nameList
	 */
	public void updateOneValueList(Map<String, Object> nameList);
	
	/**
	 * 删除名单值列表
	 * @param rosterIdMap
	 */
	public void deleteValueList(Map<String,List<Map<String,String>>> batchMap);
	
	/**
	 * 名单值列表查询
	 * @param conds
	 * @return
	 */
	Page<Map<String, Object>> listValueListByPage(Map<String, String> conds);
	
	/**
	 * 获取名单名称、名单值类型、名单数据类型相同，名单类型不同的数据
	 * @param conds
	 * @return
	 */
	public List<Map<String, Object>>  getRosterTypeList(Map<String, String> conds);

	/**名单新增做重复校验的查询
	 * @param conds
	 * @return
	 */
	List<Map<String, Object>> getRosterListForAdd(Map<String, String> conds);
	
	/**是否存在此名单值
	 * @param conds
	 * @return
	 */
	long getRosterListForExist(Map<String, Object> conds);

	/**通过名单名称查询名单信息表，名单名称不能重复
	 * @param rosterName
	 * @return
	 */
	List<Map<String, Object>> getNameListByName(String rosterName);

	/**通过名单主键查询名单信息缓存
	 * @param rosterid
	 * @return
	 */
	Map<String, Object> getOneNameListById(String rosterid);

	/**值转换
	 * @param nameList
	 */
	void updateOneValueListForConvert(Map<String, Object> nameList);
    
    List<Map<String, Object>> listValueListById(Map<String, String> conds);

    public Object importRoster(Map<String,List<Map<String,?>>> batchUpdateMap);
    
	List<Map<String, Object>> getNameListByDesc(String string);
	
	public boolean isDuplicateByRosterAndTransAttr(String rosterName);
}
