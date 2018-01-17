package cn.com.higinet.tms.common.elasticsearch;

import java.util.List;

public class SuccProcess<T> {

	public void onSuccess( String indexName, Class<T> classType, List<T> dataList ) {
		System.out.println( "++++提交成功的数据为:" + dataList.size() );
	}

	public void onError( String indexName, Class<T> classType, List<T> dataList ) {
		System.out.println( "++++提交失败的数据为:" + dataList.size() );
	}

}
