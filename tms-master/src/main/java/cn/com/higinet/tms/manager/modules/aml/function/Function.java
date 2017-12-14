package cn.com.higinet.tms.manager.modules.aml.function;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

/**
 * 处理函数接口
 * @author lining
 * @author zhang.lei
 */
@Service("dateFormat")
public class Function {

	/**
	 * 处理函数执行方法
	 * @param srcData	原数据值
	 * @param args		函数参数
	 * @return
	 */
	public Object execute(Object srcData, Object... args) {
		SimpleDateFormat sdf = new SimpleDateFormat(String.valueOf(args[0]).trim());
		return sdf.format(new Date(Long.parseLong(String.valueOf(srcData))));
	}
}
