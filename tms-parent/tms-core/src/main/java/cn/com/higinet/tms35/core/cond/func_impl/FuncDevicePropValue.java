package cn.com.higinet.tms35.core.cond.func_impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms35.comm.DeviceUtil;
import cn.com.higinet.tms35.core.cond.func;
/**
 * 截取属性值
 * @author Tiver
 *
 */
public class FuncDevicePropValue implements func{
	
	private static Logger log = LoggerFactory.getLogger(FuncDevicePropValue.class);

	public Object exec(Object[] p, int n)
	{
		String devFinger = obj2String(p[n]);
		String[] ids = obj2String(p[n+1]).split(";");
		if (devFinger.length() < 3){
			return "";
		}
		devFinger = DeviceUtil.getDeviceInfo(devFinger);
		for (int i = 0; i < ids.length; i++) {
			String value = DeviceUtil.getPropById(ids[i], devFinger);
			if(value != null && value.length() > 0){
				return value;
			}
		}
		return "";
	}
	
	private String obj2String(Object obj){
		return obj == null ? "" : String.valueOf(obj);
	}

}
