/*
 ***************************************************************************************
 * All rights Reserved, Designed By www.higinet.com.cn
 * @Title:  ShowStat.java   
 * @Package cn.com.higinet.tms35   
 * @Description: (用一句话描述该文件做什么)   
 * @author: 王兴
 * @date:   2017-8-25 11:08:11   
 * @version V1.0 
 * @Copyright: 2017 北京宏基恒信科技有限责任公司. All rights reserved. 
 * 注意：本内容仅限于公司内部使用，禁止外泄以及用于其他的商业目
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package cn.com.higinet.tms35;

import cn.com.higinet.tms.common.cache.CacheManager;
import cn.com.higinet.tms.common.cache.KV;
import cn.com.higinet.tms.common.cache.ProviderNotFoundException;
import cn.com.higinet.tms35.core.bean;
import cn.com.higinet.tms35.stat.net.net_main;

/**
 * 临时给测试用的打印统计数据的类
 *
 * @ClassName:  ShowStat
 * @author: 王兴
 * @date:   2017-8-25 11:08:11
 * @since:  v4.3
 */
public class ShowCache {

	/**
	 * 主函数.
	 * ./show.sh ${cacheName} ${group} ${id}
	 *${cacheName}可选，如果没有默认为main
	 * @param args the arguments
	 * @throws Exception 
	 * @throws ProviderNotFoundException 
	 */
	public static void main(String[] args) throws ProviderNotFoundException, Exception {
		net_main.init();
		CacheManager cacheManager = (CacheManager) bean.get(CacheManager.class);
		try {
			boolean success = cacheManager.start();
			if (!success) {
				cacheManager.stop();
				System.exit(0);
			}
			String id = null;
			String group = null;
			String cache = "main";
			if (args.length == 2) {
				group = args[0];
				id = args[1];
			} else if (args.length == 3) {
				cache = args[0];
				group = args[1];
				id = args[2];
			} else {
				System.out.println("统计打印的格式为./show.sh ${cacheName} ${group} ${id}。cacheName为空的话，默认为main");
				return;
			}
			KV k = cacheManager.getProvider(cache).getCache().get(new KV(group, id));
			System.out.println(k.getKey() + ":" + k.getString());
		} finally {
			cacheManager.stop();
		}
	}
}
