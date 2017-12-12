package cn.com.higinet.tms35.manage.dataanalysic.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cn.com.higinet.rapid.base.dao.Page;

public interface DateValueService {
	//查询统计值
	public List<Map<String,Object>> queryDateAnnaList(String userId,String deviceId,String thingType,String transBeginTime,String transEndTime,HttpServletRequest request
);
}
