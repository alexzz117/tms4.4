package cn.com.higinet.tms.manager.modules.tmsreport.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.com.higinet.tms.manager.modules.common.util.MapUtil;
import cn.com.higinet.tms35.comm.comp_tool;

public class ReportCompare {

	/**
	 * @param list	需要排序的list
	 * @param key	每个MAP需要拍下的KEY
	 * @param isDesc	是否为降序 true:降序,false:升序
	 */
	public static void sort( List<Map<String, Object>> list, final String key, final boolean isDesc ) {
		Collections.sort( list, new Comparator<Map<String, Object>>() {
			@Override
			public int compare( Map<String, Object> o1, Map<String, Object> o2 ) {
				if( isDesc ) {
					return comp_tool.comp( MapUtil.getLong( o2, key ), MapUtil.getLong( o1, key ) );
				}
				else {
					return comp_tool.comp( MapUtil.getLong( o1, key ), MapUtil.getLong( o2, key ) );
				}
			}
		} );
	}
}
