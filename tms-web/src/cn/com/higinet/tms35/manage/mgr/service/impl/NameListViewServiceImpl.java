package cn.com.higinet.tms35.manage.mgr.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.higinet.rapid.base.dao.SimpleDao;


/**
 * 
 * @author zhangfg
 * 
 */
@Service("nameListViewService")
public class NameListViewServiceImpl extends NameListServiceImpl {
	
	@Autowired
	private SimpleDao officialSimpleDao;
	
	public SimpleDao getSimpleDao() {
		return officialSimpleDao;
	}
}
