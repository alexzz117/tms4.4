package cn.com.higinet.tms.engine.core.cond.func_impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.higinet.tms.engine.core.cond.func;
import cn.com.higinet.tms.engine.core.cond.func_map;

/**
 * @description 判断一个字符串是否包含阿拉伯数字
 * @author wuruiqi@higinet.com.cn
 * @version 4.0
 * @since 2017-8-14 
 * 
 * n:str
 * 
 */
public class func_has_num implements func
{
	static final Logger log = LoggerFactory.getLogger(func_has_num.class);
	
	public boolean HasDigit(String content) {
	    boolean flag = false;
	    Pattern p = Pattern.compile(".*\\d+.*");
	    Matcher m = p.matcher(content);
	    if (m.matches()) {
	        flag = true;
	    }
	    return flag;
	}

	public Object exec(Object[] p, int n)
	{
		
		if(func_map.has_nothing(p, n, 1))
			return func_map.NOTHING;
		String param = (String) p[n];
		boolean flag = HasDigit(param);
		
		if (log.isDebugEnabled())
			log.debug(String.format("param=%s,flag=%b", p[n], flag));
		if(flag){
			return 1;
		} else {
			return 0;
		}
	}
	
//	public static void main(String []a)
//	{
//		System.out.print(new func_has_num().HasDigit("收货人收货地址是否含有数字"));
//	}
}
