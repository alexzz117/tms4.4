package cn.com.higinet.tms.manager.modules.datamgr.service;

import cn.com.higinet.tms.manager.modules.datamgr.common.DataConfig;

/**
 * 数据操作服务接口
 * @author zhangfg
 * @version 1.0.0 2012-09-19
 *
 */
public interface DataOperService {
	/**
	 * 数据打包
	 * @return
	 */
	public boolean pack(DataConfig dataconfig);
	/**
	 * 数据解包
	 * @return
	 */
	public boolean unPack(DataConfig dataconfig);
	/**
	 * 数据同步更新
	 * @return
	 */
	public void dataSync(DataConfig dataconfig,String pkValue,String authFlag);
}
