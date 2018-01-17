package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public class SuccProcess<T>{
	
	public void callback(String indexName,Class<T> classType,List<T> dataList){
		System.out.println("++++提交成功的数据为:" + dataList.size());    
	}
	
}
