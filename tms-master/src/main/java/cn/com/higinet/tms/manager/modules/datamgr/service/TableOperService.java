package cn.com.higinet.tms.manager.modules.datamgr.service;

import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.manager.modules.datamgr.common.DataConfig;

/**
 * 表操作服务接口
 * @author zhangfg
 *
 */
public interface TableOperService {
	public List<Map<String,Object>> queryDataList(DataConfig tableConfig,DataConfig dataconfig,String authFlag);
	
}
