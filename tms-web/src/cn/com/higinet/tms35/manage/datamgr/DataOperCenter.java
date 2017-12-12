package cn.com.higinet.tms35.manage.datamgr;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

import cn.com.higinet.cmc.util.StringUtil;
import cn.com.higinet.tms35.manage.datamgr.common.ConfigAttrabute;
import cn.com.higinet.tms35.manage.datamgr.common.DataConfig;
import cn.com.higinet.tms35.manage.datamgr.service.DataOperService;

/**
 * 数据操作类
 * @author zhangfg
 * @version 1.0.0 2012-09-19
 *
 */
@Service("dataOperCenter")
public class DataOperCenter extends ApplicationObjectSupport{

	private final static Log logger = LogFactory.getLog(DataOperCenter.class);

	public Map<String,Object> dataConfigs=null;
	/**
	 * 对数据进行操作		
	 * @param modelId	要操作的模块ID
	 * @param method	要进行的操作类型(数据同步，打包，解包)
	 * @param pkValue	要操作的模块的主表主键值
	 * @param authFlag	授权状态，是通过还是没通过
	 * @return 			返回操作结果状态
	 */
	public boolean operData(String modelId,String method,String pkValue,String authFlag){
		boolean flag = true;
		if(dataConfigs==null || dataConfigs.isEmpty()){
			dataConfigs=ParseDataSyncConfigXml.getInstance().getDataConfigs();
		}
		
		DataConfig dataconfig = null;
		if(!StringUtil.isBlank(modelId)){
			dataconfig = (DataConfig)dataConfigs.get(modelId);
		}
		if(dataconfig==null){
			throw new RuntimeException("没有相应的数据同步配置！");
		}
		String serviceName = dataconfig.getAttrabute(ConfigAttrabute.DATAOPER_SERVICE);
		if(ConfigAttrabute.METHOD_DATASYNC.equals(method)){
			getApplicationContext().getBean(serviceName, DataOperService.class).dataSync(dataconfig,pkValue,authFlag);
		}else if(ConfigAttrabute.METHOD_PACK.equals(method)){
			getApplicationContext().getBean(serviceName, DataOperService.class).pack(dataconfig);
		}else if(ConfigAttrabute.METHOD_UNPACK.equals(method)){
			getApplicationContext().getBean(serviceName, DataOperService.class).unPack(dataconfig);
		}else{
			logger.info("没有要调用的方法");
			flag = false;
		}
		return flag;
	}
}
