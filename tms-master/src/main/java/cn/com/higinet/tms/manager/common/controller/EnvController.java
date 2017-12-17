/*
 * Copyright © 2011 Beijing HiGiNet Technology Co.,Ltd.
 * All right reserved.
 *
 */
package cn.com.higinet.tms.manager.common.controller;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.com.higinet.tms.base.entity.common.DictCategory;
import cn.com.higinet.tms.manager.common.CodeDict;
import cn.com.higinet.tms.manager.common.ManagerConstants;

/**
 * 公用环境参数Controller类
 * @author zhang.lei
 */
@RestController
@RequestMapping(ManagerConstants.URI_PREFIX + "/common")
public class EnvController {

	private static final Logger logger = LoggerFactory.getLogger( EnvController.class );

	@Value("${tms.manager.dictcache.loadinterval:10}")
	private int loadinterval; //字典缓存更新间隔时间，单位：分钟

	@Autowired
	CodeDict codeDict;

	private long lastmodify = -1L;
	private Map<String, DictCategory> codes;

	@RequestMapping(value = "/dict", method = RequestMethod.GET)
	public Map<String, DictCategory> message() {

		if( isOverdue() || codes == null ) {
			synchronized (EnvController.class) {
				codes = codeDict.getAllCodes();
				this.lastmodify = System.currentTimeMillis();
				logger.info( "dict load" );
			}

		}
		return codes;
	}

	/**
	 * 判断缓存是否过期
	 * 当前时间 - 上次更新时间 > 过期阈值
	 */
	private boolean isOverdue() {
		long now = new Date().getTime();
		long interval = loadinterval * 60 * 1000;
		if( (now - lastmodify) > interval ) return true;
		else return false;
	}
}
